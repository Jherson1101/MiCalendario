package com.example.micalendario.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.RemoteViews;

import com.example.micalendario.R;
import com.example.micalendario.database.SQLiteHelper;
import com.example.micalendario.models.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class NextTaskWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.next_task_widget);
        
        // Consultar el rol actual
        SharedPreferences prefs = context.getSharedPreferences("MiCalendarioPrefs", Context.MODE_PRIVATE);
        String rol = prefs.getString("rol_actual", "");

        if (!"nino".equals(rol)) {
            // Si no es el niño, mostrar mensaje de restricción
            views.setViewVisibility(m_msg_rol(context), View.VISIBLE);
        } else {
            views.setViewVisibility(m_msg_rol(context), View.GONE);
            
            // Obtener la siguiente tarea de hoy
            SQLiteHelper db = new SQLiteHelper(context);
            // El formato en la DB es yyyy-MM-dd
            String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
            List<Task> tasks = db.obtenerTasksPorFecha(hoy);
            
            Task nextTask = null;
            // Lógica simple para encontrar la primera no completada (podría mejorarse con comparación de horas)
            for (Task t : tasks) {
                if (t.getCompletada() == 0) {
                    nextTask = t;
                    break;
                }
            }

            if (nextTask != null) {
                views.setTextViewText(R.id.widget_task_name, nextTask.getTitulo());
                views.setTextViewText(R.id.widget_task_time, nextTask.getHora());
                
                // Intentar cargar imagen si es recurso (simplificado para el ejemplo)
                int resId = context.getResources().getIdentifier(nextTask.getPictograma(), "drawable", context.getPackageName());
                if (resId != 0) {
                    views.setImageViewResource(R.id.widget_image, resId);
                }
            } else {
                views.setTextViewText(R.id.widget_task_name, "¡Todo listo!");
                views.setTextViewText(R.id.widget_task_time, "No hay más tareas hoy");
            }
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static int m_msg_rol(Context context) {
        // Método auxiliar para obtener el ID de forma segura en contexto estático
        return R.id.widget_msg_rol;
    }
}