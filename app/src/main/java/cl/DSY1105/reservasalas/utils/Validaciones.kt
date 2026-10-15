package cl.DSY1105.reservasalas.utils

private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

fun esEmailValido(email : String) : Boolean{
    return cl.DSY1105.reservasalas.utils.EMAIL_REGEX.matches(email.trim())
}

fun esPasswordValida(p: String): Boolean = p.length >= 3