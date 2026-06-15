package com.example.micalendario.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.example.micalendario.R;
public class NotificationReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {

        String titulo = intent.getStringExtra("titulo");
        String pictograma = intent.getStringExtra("pictograma");
        
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "mi_calendario_channel";

        // Obtener el ID del recurso de la imagen dinámicamente
        int resId = 0;
        if (pictograma != null && !pictograma.isEmpty()) {
            resId = context.getResources().getIdentifier("ic_" + pictograma, "drawable", context.getPackageName());
        }
        
        // Si no encuentra la imagen específica, usamos una por defecto
        if (resId == 0) resId = R.mipmap.ic_launcher;

        android.graphics.Bitmap largeIcon = android.graphics.BitmapFactory.decodeResource(context.getResources(), resId);

        if(android.os.Build.VERSION.SDK_INT
                >= android.os.Build.VERSION_CODES.O){

            NotificationChannel channel = new NotificationChannel(
                            channelId,
                            "Recordatorios Suaves",
                            NotificationManager.IMPORTANCE_DEFAULT
                    );
            channel.setDescription("Notificaciones con sonidos suaves para evitar ansiedad");
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.ic_notif_small) // Un icono simple para la barra de estado
                        .setLargeIcon(largeIcon) // ¡Aquí aparece el pictograma grande!
                        .setContentTitle("Es momento de...")
                        .setContentText(titulo)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setVibrate(new long[]{0, 300, 200, 300}) // Vibración suave en dos pulsos
                        .setAutoCancel(true);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
