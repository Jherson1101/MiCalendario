package com.example.micalendario.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import com.example.micalendario.R;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String titulo = intent.getStringExtra("titulo");
        String pictograma = intent.getStringExtra("pictograma");
        boolean esAnticipacion = intent.getBooleanExtra("esAnticipacion", false);
        
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Usamos el mismo ID que en NotificationHelper para consistencia
        String channelId = "mi_calendario_reminders";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Recordatorios de Actividades",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificaciones para el seguimiento de rutinas");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 250, 150, 250});
            manager.createNotificationChannel(channel);
        }

        int resId = 0;
        if (pictograma != null && !pictograma.isEmpty()) {
            resId = context.getResources().getIdentifier("ic_" + pictograma, "drawable", context.getPackageName());
        }
        if (resId == 0) resId = R.mipmap.ic_launcher;

        android.graphics.Bitmap largeIcon = android.graphics.BitmapFactory.decodeResource(context.getResources(), resId);

        String contenidoTitulo = esAnticipacion ? "Prepárate, pronto sigue..." : "Es momento de...";
        String contenidoTexto = esAnticipacion ? titulo + " (en 5 minutos)" : titulo;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notif_small)
                .setLargeIcon(largeIcon)
                .setContentTitle(contenidoTitulo)
                .setContentText(contenidoTexto)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
