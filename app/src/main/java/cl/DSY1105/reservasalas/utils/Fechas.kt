package cl.DSY1105.reservasalas.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun hoy(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())