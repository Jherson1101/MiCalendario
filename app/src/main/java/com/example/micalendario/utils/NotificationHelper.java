package com.example.micalendario.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import com.example.micalendario.R;

public class NotificationHelper {
    private static final String CHANNEL_ID = "mi_calendario_channel";
    public static void mostrarNotificacion(
            Context context,
            String titulo,
            String mensaje
    ){
        NotificationManager manager = (NotificationManager)
                        context.getSystemService(
                                Context.NOTIFICATION_SERVICE
                        );

        if(android.os.Build.VERSION.SDK_INT
                >= android.os.Build.VERSION_CODES.O){

            NotificationChannel channel = new NotificationChannel(
                            CHANNEL_ID,
                            "Recordatorios Suaves",
                            NotificationManager.IMPORTANCE_DEFAULT
                    );
            channel.setDescription("Notificaciones con sonidos suaves para evitar ansiedad");
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                        context,
                        CHANNEL_ID
                )
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(titulo)
                        .setContentText(mensaje)
                        .setPriority(
                                NotificationCompat.PRIORITY_DEFAULT
                        )
                        .setVibrate(new long[]{0, 500}) // Vibración corta y única
                        .setAutoCancel(true);
        manager.notify(
                (int) System.currentTimeMillis(),
                builder.build()
        );
    }
}
