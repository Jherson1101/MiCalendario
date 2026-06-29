package com.example.micalendario.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import androidx.core.app.NotificationCompat;
import com.example.micalendario.R;

public class NotificationHelper {
    // Cambiamos el ID del canal para forzar la actualización de importancia en el sistema
    private static final String CHANNEL_ID = "mi_calendario_reminders";

    public static void mostrarNotificacion(Context context, String titulo, String mensaje) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Recordatorios de Actividades",
                    NotificationManager.IMPORTANCE_HIGH // Importancia ALTA para que suene y aparezca
            );
            channel.setDescription("Notificaciones para el seguimiento de rutinas");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 250, 150, 250});
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notif_small)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
