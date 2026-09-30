package es.androidtfm.gamevision

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import es.androidtfm.gamevision.data.library.LibraryEntry
import es.androidtfm.gamevision.data.library.LibraryRepository
import es.androidtfm.gamevision.data.library.LibraryStatus
import es.androidtfm.gamevision.data.model.FeedEntry
import es.androidtfm.gamevision.data.repository.SocialRepository
import es.androidtfm.gamevision.data.social.FeedEntryPair
import es.androidtfm.gamevision.data.social.FeedQueryPlanner
import es.androidtfm.gamevision.data.social.MilestonePlanner
import es.androidtfm.gamevision.data.model.MilestoneTypes
import es.androidtfm.gamevision.helpers.QaAccounts
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/*
 * Instrumentados de Social (CIERRE-PRE-F3 / 1.4, deuda 5 de F2).
 *
 * Nivel de integración (app real + Firestore real + reglas reales): autentica
 * con las cuentas QA y ejercita los tres comportamientos que no se podían
 * automatizar con `input tap`:
 *   1. Me gusta idempotente (setLike + contador + hasLiked, sobre post efímero).
 *   2. Bloqueo: block/unblock + filtro de bloqueados del planificador del feed.
 *   3. Hito de reseña: id determinista (MilestonePlanner) + tarjeta "Reseñó".
 * La limpieza final borra lo creado (borrados blindados con runCatching: un
 * permiso denegado de limpieza no debe tumbar una ejecución verde).
 */
@RunWith(AndroidJUnit4::class)
class SocialIntegrationTest {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val social = SocialRepository(db)
    private val library = LibraryRepository(db)

    private var gameId: String = ""

    @Before
    fun setUp() {
        gameId = "qa14_${System.currentTimeMillis() % 10_000_000L}"
    }

    @After
    fun tearDown() {
        runBlocking { cleanup() }
    }

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------

    private fun loginAs(email: String, pass: String) {
        QaAccounts.loginAs(email, pass)
    }

    private fun loginAsC1() = QaAccounts.loginAsC1()
    private fun loginAsC2() = QaAccounts.loginAsC2()

    private suspend fun pairs(vararg uids: String): List<FeedEntryPair> =
        withTimeout(20_000) {
            db.collection("feed").whereIn("authorUid", uids.toList()).get().await()
                .documents.map { FeedEntryPair(it.id, FeedEntry.fromMap(it.id, it.data)) }
        }

    private suspend fun likesCount(entryId: String): Long =
        withTimeout(20_000) {
            db.collection("feed").document(entryId).get().await().getLong("likesCount") ?: 0L
        }

    // ------------------------------------------------------------------
    // 1. Me gusta: like + unlike en un post efímero propio
    // ------------------------------------------------------------------

    @Test
    fun like_y_unlike_actualizan_contador_y_lista() = runBlocking {
        loginAsC1()
        val uid = auth.uid!!
        val entryId = social.publishEntry(
            FeedEntry(type = FeedEntry.TYPE_POST, authorUid = uid, text = "[QA1.4] like test")
        ).getOrThrow()

        social.setLike(entryId, uid, liked = true).getOrThrow()
        assertEquals(1L, likesCount(entryId))
        assertTrue(social.hasLiked(entryId, uid).getOrDefault(false))
        // likedByMe NO vive en Firestore: la UI lo rellena con likedByMe(entryIds, uid);
        // aqui el contrato de repositorio es hasLiked + contador.
        val mapa = social.likedByMe(listOf(entryId), uid).getOrDefault(emptyMap())
        assertTrue("likedByMe(entryIds) debe marcar el like", mapa[entryId] == true)

        social.setLike(entryId, uid, liked = false).getOrThrow()
        assertEquals(0L, likesCount(entryId))
        assertFalse(social.hasLiked(entryId, uid).getOrDefault(false))
        Unit
    }

    // ------------------------------------------------------------------
    // 2. Bloqueo: el bloqueado desaparece del feed del bloqueador
    // ------------------------------------------------------------------

    @Test
    fun bloqueo_filtra_los_posts_del_bloqueado_en_el_feed() = runBlocking {
        // c1 publica; c2 bloquea a c1 y comprueba el filtro del feed.
        loginAsC1()
        social.publishEntry(
            FeedEntry(type = FeedEntry.TYPE_POST, authorUid = QA1_UID, text = "[QA1.4] bloqueo test")
        ).getOrThrow()

        loginAsC2()
        val me = auth.uid!!
        // Auto-saneado: si una ejecución anterior dejó el bloqueo, se retira primero.
        if (QA1_UID in social.blockedUids(me).getOrDefault(emptySet())) {
            social.unblock(me, QA1_UID).getOrThrow()
        }
        assertFalse(QA1_UID in social.blockedUids(me).getOrDefault(emptySet()))

        val visible = pairs(QA1_UID, me)
        assertTrue("c1 debe verse antes del bloqueo", visible.any { it.entry.authorUid == QA1_UID })

        social.block(me, QA1_UID).getOrThrow()
        val blocked = social.blockedUids(me).getOrDefault(emptySet())
        assertTrue("c1 debe aparecer en bloqueados", QA1_UID in blocked)

        val filtrado = FeedQueryPlanner.filterBlocked(visible, blocked)
        assertFalse("c1 no debe verse tras el bloqueo", filtrado.any { it.entry.authorUid == QA1_UID })

        social.unblock(me, QA1_UID).getOrThrow()
        assertFalse(QA1_UID in social.blockedUids(me).getOrDefault(emptySet()))
        Unit
    }

    // ------------------------------------------------------------------
    // 3. Hito de reseña: id determinista + tarjeta "Reseñó {juego}"
    // ------------------------------------------------------------------

    @Test
    fun hito_de_resena_se_publica_con_id_determinista_y_tarjeta_visible() = runBlocking {
        loginAsC1()
        val uid = auth.uid!!

        library.addGame(
            uid,
            LibraryEntry(
                gameId = gameId,
                status = LibraryStatus.PLAYING,
                rating = 4.0,
                review = "Reseña de prueba del 1.4",
                name = "QA Game 1.4"
            )
        ).getOrThrow()

        val milestoneId = MilestonePlanner.milestoneId(uid, MilestoneTypes.REVIEW, gameId)
        social.publishEntry(
            FeedEntry(
                type = FeedEntry.TYPE_MILESTONE,
                authorUid = uid,
                milestoneType = MilestoneTypes.REVIEW,
                gameId = gameId,
                gameName = "QA Game 1.4",
                rating = 4.0f
            )
        ).getOrThrow()

        val doc = withTimeout(20_000) {
            db.collection("feed").document(milestoneId).get().await()
        }
        assertTrue("El hito debe existir con su id determinista", doc.exists())
        assertEquals(MilestoneTypes.REVIEW, doc.getString("milestoneType"))

        // El feed (con re-lectura) debe mostrar la tarjeta del hito.
        var hito: FeedEntryPair? = null
        withTimeout(20_000) {
            while (hito == null) {
                hito = pairs(uid).firstOrNull {
                    it.entry.milestoneType == MilestoneTypes.REVIEW && it.entry.gameId == gameId
                }
                if (hito == null) delay(500)
            }
        }
        assertTrue(hito!!.entry.isMilestone)
        assertTrue(hito!!.entry.gameName == "QA Game 1.4")
        Unit
    }

    // ------------------------------------------------------------------
    // Limpieza: borra lo creado (y cualquier bloqueo QA residual)
    // ------------------------------------------------------------------

    private suspend fun cleanup() {
        loginAsC1()
        cleanBlocks(QA1_UID)
        cleanFeed()
        runCatching {
            db.collection("users").document(QA1_UID).collection("library").document(gameId)
                .delete().await()
        }
        loginAsC2()
        cleanBlocks(QA2_UID)
    }

    private suspend fun cleanBlocks(me: String) {
        QaAccounts.cleanBlocks(db, me)
    }

    private suspend fun cleanFeed() {
        val snap = withTimeout(20_000) {
            db.collection("feed").whereIn("authorUid", listOf(QA1_UID, QA2_UID)).get().await()
        }
        for (doc in snap.documents) {
            val data = doc.data.orEmpty()
            val text = data["text"]?.toString().orEmpty()
            val isQaMilestone = data["type"] == FeedEntry.TYPE_MILESTONE &&
                data["gameId"]?.toString().orEmpty().startsWith("qa14_")
            if (text.startsWith("[QA1.4]") || isQaMilestone) {
                doc.reference.collection("likes").get().await().documents.forEach {
                    runCatching { it.reference.delete().await() }
                }
                runCatching { doc.reference.delete().await() }
            }
        }
    }

    private companion object {
        // Credenciales y UIDs de las cuentas QA: ahora viven en helpers/QaAccounts.kt
        // (bloque A del plan de optimización) para que el próximo instrumentado las reutilice.
        private val QA1_EMAIL get() = QaAccounts.QA1_EMAIL
        private val QA1_UID get() = QaAccounts.QA1_UID
        private val QA2_UID get() = QaAccounts.QA2_UID
    }
}
