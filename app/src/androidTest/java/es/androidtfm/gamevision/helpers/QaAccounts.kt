package es.androidtfm.gamevision.helpers

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertTrue

/*
 * Helpers compartidos de los instrumentados (bloque A del plan de
 * optimización del ciclo de verificación; D-OP3: viven en androidTest).
 *
 * Cuentas QA del entorno de pruebas (docs/metodologia/entorno-pruebas.md) y
 * utilidades comunes: login con timeout, saneado de bloqueos residuales.
 * El objetivo es que el próximo instrumentado cueste minutos, no redescubrir
 * el andamiaje.
 */

object QaAccounts {
    const val QA1_EMAIL = "gamevision@gmail.com"
    const val QA1_PASS = "1234561"
    const val QA2_EMAIL = "gamevision2@gmail.com"
    const val QA2_PASS = "1234562"
    const val QA1_UID = "gKfQYvAdiBOezcGnrWo3rH0nUmY2"
    const val QA2_UID = "4nsmmGu6g3R7XGLkDR5trFfB2BF2"

    /** Cierra sesión previa y autentica con la cuenta indicada (Firebase real). */
    fun loginAs(email: String, pass: String): FirebaseAuth {
        val auth = FirebaseAuth.getInstance()
        auth.signOut()
        val res = runBlocking {
            withTimeout(20_000) { auth.signInWithEmailAndPassword(email, pass).await() }
        }
        assertTrue("No se pudo autenticar $email", res.user != null)
        return auth
    }

    fun loginAsC1() = loginAs(QA1_EMAIL, QA1_PASS)
    fun loginAsC2() = loginAs(QA2_EMAIL, QA2_PASS)

    /** Borra bloqueos residuales de ejecuciones anteriores (runCatching: no tumbar). */
    fun cleanBlocks(db: com.google.firebase.firestore.FirebaseFirestore, me: String) {
        for (target in listOf(QA1_UID, QA2_UID)) {
            runCatching {
                runBlocking {
                    db.collection("blocks").document(me).collection("people").document(target)
                        .delete().await()
                }
            }
        }
    }
}
