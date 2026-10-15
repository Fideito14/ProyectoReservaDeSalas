package cl.DSY1105.reservasalas.nativos

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import cl.DSY1105.reservasalas.MainActivity

// Recurso nativo 1: notificaciones locales
object Notificaciones {
    private const val CANAL = "reservas"

    // En Android 13+ hay que pedir permiso; antes de eso no hace falta
    fun tienePermiso(ctx: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED

    fun mostrar(ctx: Context, titulo: String, texto: String) {
        if (!tienePermiso(ctx)) return // sin permiso, no se notifica

        val nm = ctx.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(
                NotificationChannel(CANAL, "Reservas", NotificationManager.IMPORTANCE_DEFAULT))
        }

        // Al tocar la notificación se abre la app
        val abrir = PendingIntent.getActivity(
            ctx, 0, Intent(ctx, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notificacion = NotificationCompat.Builder(ctx, CANAL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setContentIntent(abrir)
            .setAutoCancel(true)
            .build()

        nm.notify(System.currentTimeMillis().toInt(), notificacion)
    }
}