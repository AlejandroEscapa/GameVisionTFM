package es.androidtfm.gamevision

import es.androidtfm.gamevision.data.model.FeedEntry
import es.androidtfm.gamevision.data.model.GameList
import es.androidtfm.gamevision.data.model.MilestoneTypes
import es.androidtfm.gamevision.data.model.UserProfile
import es.androidtfm.gamevision.data.repository.usernameAliasId
import es.androidtfm.gamevision.data.social.FeedEntryPair
import es.androidtfm.gamevision.data.social.FeedQueryPlanner
import es.androidtfm.gamevision.data.social.FollowLogic
import es.androidtfm.gamevision.data.social.ListProgress
import es.androidtfm.gamevision.data.social.MilestonePlanner
import es.androidtfm.gamevision.data.social.PostComposer
import es.androidtfm.gamevision.data.social.VisibilityLogic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/*
 * F2 (30/09): lógica pura social, sin Firestore ni Android.
 */
class SocialLogicTest {

    // ------------------------------------------------------------------ D2.2

    @Test
    fun `sin sesion no se ve nada`() {
        assertFalse(VisibilityLogic.canSee("", "alice", isPrivate = false, blocked = false))
    }

    @Test
    fun `cuenta publica se ve con sesion`() {
        assertTrue(VisibilityLogic.canSee("bob", "alice", isPrivate = false, blocked = false))
    }

    @Test
    fun `cuenta privada oculta al resto`() {
        assertFalse(VisibilityLogic.canSee("bob", "alice", isPrivate = true, blocked = false))
    }

    @Test
    fun `el dueño ve su contenido aunque sea privado`() {
        assertTrue(VisibilityLogic.canSee("alice", "alice", isPrivate = true, blocked = false))
    }

    @Test
    fun `el bloqueo corta incluso cuentas publicas`() {
        assertFalse(VisibilityLogic.canSee("bob", "alice", isPrivate = false, blocked = true))
    }

    @Test
    fun `el motivo de no ver refleja privado y bloqueo`() {
        assertEquals(VisibilityLogic.REASON_PRIVATE, VisibilityLogic.reasonNotVisible("bob", "alice", true, false))
        assertEquals(VisibilityLogic.REASON_BLOCKED, VisibilityLogic.reasonNotVisible("bob", "alice", false, true))
        assertEquals(VisibilityLogic.REASON_PUBLIC, VisibilityLogic.reasonNotVisible("bob", "alice", false, false))
    }

    // ------------------------------------------------------------------ D2.1

    @Test
    fun `la arista de seguimiento es determinista`() {
        assertEquals("alice_bob", FollowLogic.edgeId("alice", "bob"))
        assertEquals(FollowLogic.edgeId("alice", "bob"), FollowLogic.edgeId("alice", "bob"))
        assertTrue(FollowLogic.edgeId("alice", "bob") != FollowLogic.edgeId("bob", "alice"))
    }

    @Test
    fun `isFollowing consulta el conjunto de aristas`() {
        val edges = setOf("bob", "carol")
        assertTrue(FollowLogic.isFollowing(edges, "bob"))
        assertFalse(FollowLogic.isFollowing(edges, "alice"))
    }

    @Test
    fun `toggle alterna el estado`() {
        assertTrue(FollowLogic.afterToggle(false))
        assertFalse(FollowLogic.afterToggle(true))
    }

    // ------------------------------------------------------------ feed D2.3

    @Test
    fun `chunks vacio y menor que max`() {
        assertTrue(FeedQueryPlanner.chunksFor(emptyList()).isEmpty())
        assertEquals(listOf(listOf("a")), FeedQueryPlanner.chunksFor(listOf("a")))
    }

    @Test
    fun `chunks respeta el limite 30 de whereIn`() {
        val uids = (1..75).map { "uid$it" }
        val chunks = FeedQueryPlanner.chunksFor(uids)
        assertEquals(3, chunks.size)
        assertTrue(chunks.all { it.size <= FeedQueryPlanner.MAX_CHUNK })
    }

    @Test(expected = IllegalArgumentException::class)
    fun `chunk de tamano cero es invalido`() {
        FeedQueryPlanner.chunksFor(listOf("a"), maxChunk = 0)
    }

    private fun entry(id: String, author: String, createdAt: Long, type: String = FeedEntry.TYPE_POST) =
        FeedEntry(id = id, type = type, authorUid = author, createdAt = createdAt)

    @Test
    fun `merge deduplica por id`() {
        val page1 = listOf(FeedEntryPair("f1", entry("f1", "a", 10)))
        val page2 = listOf(
            FeedEntryPair("f1", entry("f1", "a", 10)),
            FeedEntryPair("f2", entry("f2", "b", 20))
        )
        val merged = FeedQueryPlanner.merge(page1, page2)
        assertEquals(listOf("f2", "f1"), merged.map { it.id })
    }

    @Test
    fun `merge ordena descendente por createdAt`() {
        val merged = FeedQueryPlanner.merge(
            listOf(FeedEntryPair("a", entry("a", "x", 5))),
            listOf(FeedEntryPair("b", entry("b", "y", 100))),
            listOf(FeedEntryPair("c", entry("c", "z", 50)))
        )
        assertEquals(listOf("b", "c", "a"), merged.map { it.id })
    }

    @Test
    fun `hay mas paginas solo si se lleno la anterior`() {
        assertTrue(FeedQueryPlanner.hasMorePages(loaded = 20))
        assertFalse(FeedQueryPlanner.hasMorePages(loaded = 12))
    }

    @Test
    fun `hitos solo de acciones soportadas y con juego`() {
        assertEquals(MilestoneTypes.COMPLETED, MilestonePlanner.forAction("game_completed", "28589"))
        assertEquals(MilestoneTypes.REVIEW, MilestonePlanner.forAction("review_published", "28589"))
        assertEquals(MilestoneTypes.LIST_PUBLIC, MilestonePlanner.forAction("list_published", "42"))
        assertEquals(null, MilestonePlanner.forAction("session_logged", "28589"))
        assertEquals(null, MilestonePlanner.forAction("game_completed", ""))
    }

    @Test
    fun `el id de hito es determinista y no duplica`() {
        assertEquals(
            MilestonePlanner.milestoneId("alice", MilestoneTypes.COMPLETED, "28589"),
            MilestonePlanner.milestoneId("alice", MilestoneTypes.COMPLETED, "28589")
        )
        assertTrue(
            MilestonePlanner.milestoneId("alice", MilestoneTypes.COMPLETED, "28589") !=
                MilestonePlanner.milestoneId("alice", MilestoneTypes.REVIEW, "28589")
        )
    }

    // ------------------------------------------------------------- T2.6

    private val list = GameList(
        id = "l1",
        ownerUid = "alice",
        name = "JRPGs pendientes",
        isPublic = true,
        gameIds = listOf("1", "2", "3", "4")
    )

    @Test
    fun `progreso de lista contra biblioteca`() {
        val p = ListProgress.of(list, setOf("1", "3"))
        assertEquals(4, p.total)
        assertEquals(2, p.inLibrary)
        assertEquals(0.5f, p.fraction)
    }

    @Test
    fun `progreso de lista vacia no divide por cero`() {
        val empty = list.copy(gameIds = emptyList())
        assertEquals(0f, ListProgress.of(empty, setOf("1")).fraction)
    }

    @Test
    fun `lista privada solo la ve su dueno`() {
        val privateList = list.copy(isPublic = false)
        assertTrue(ListProgress.canSee(privateList, viewerUid = "alice", blocked = false))
        assertFalse(ListProgress.canSee(privateList, viewerUid = "bob", blocked = false))
        assertFalse(ListProgress.canSee(privateList, viewerUid = "", blocked = false))
    }

    // ------------------------------------------------------------- posts D2.3

    @Test
    fun `post valido pasa`() {
        assertTrue(PostComposer.canPublish("Mi primer post en GameVision"))
    }

    @Test
    fun `post vacio o en blanco no pasa`() {
        assertFalse(PostComposer.canPublish(""))
        assertFalse(PostComposer.canPublish("   "))
    }

    @Test
    fun `post de 281 caracteres no pasa y el de 280 si`() {
        assertFalse(PostComposer.canPublish("a".repeat(281)))
        assertTrue(PostComposer.canPublish("a".repeat(280)))
    }

    // ------------------------------------------- alias de búsqueda (D2.4)

    @Test
    fun `alias de username lleva prefijo y minusculas`() {
        assertEquals("~javi12", usernameAliasId("Javi12"))
        assertEquals("~javi12", usernameAliasId("javi12"))
        assertTrue(usernameAliasId("zeta") > usernameAliasId("abba"))
    }

    @Test
    fun `perfil parsea isPrivate y topGameIds`() {
        val profile = UserProfile.fromMap(
            mapOf(
                "nameSurname" to "QA",
                "isPrivate" to true,
                "topGameIds" to listOf("28589", "42")
            )
        )
        assertTrue(profile.isPrivate)
        assertEquals(listOf("28589", "42"), profile.topGameIds)
        // Default: sin campo = público (no cegar perfiles antiguos).
        assertFalse(UserProfile.fromMap(mapOf("nameSurname" to "x")).isPrivate)
    }

    @Test
    fun `feed entry parsea likes y tipo`() {
        val e = FeedEntry.fromMap(
            "f1",
            mapOf("type" to "post", "authorUid" to "a", "likesCount" to 3L, "createdAt" to 99L, "text" to "hola")
        )
        assertEquals(3L, e.likesCount)
        assertFalse(e.isMilestone)
        assertTrue(FeedEntry.fromMap("f2", mapOf("type" to "milestone")).isMilestone)
    }
}
