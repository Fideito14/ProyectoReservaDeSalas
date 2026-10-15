package cl.DSY1105.reservasalas.utils

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

// Las contraseñas nunca se guardan en texto plano: se guarda un hash PBKDF2 con sal aleatoria.
object Seguridad {

    // Texto aleatorio único por usuario, para que dos contraseñas iguales no den el mismo hash
    fun nuevaSal(): String {
        val b = ByteArray(16)
        SecureRandom().nextBytes(b)
        return Base64.encodeToString(b, Base64.NO_WRAP)
    }

    // Convierte la contraseña en un código irreversible
    fun hash(password: String, sal: String): String {
        val spec = PBEKeySpec(password.toCharArray(), Base64.decode(sal, Base64.NO_WRAP), 65_536, 256)
        val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    // Al hacer login: recalcula el hash y lo compara con el guardado
    fun verificar(password: String, sal: String, hashGuardado: String): Boolean =
        MessageDigest.isEqual(hash(password, sal).toByteArray(), hashGuardado.toByteArray())
}
