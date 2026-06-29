package com.example.micalendario.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.micalendario.R;
import com.example.micalendario.database.SQLiteHelper;
import com.example.micalendario.models.Task;
import com.example.micalendario.utils.NotificationHelper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import java.util.Calendar;

import android.app.TimePickerDialog;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.view.View;

import com.google.android.material.textfield.TextInputLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.micalendario.adapters.PictogramAdapter;
import com.example.micalendario.utils.PictogramUtils;

public class AddTaskActivity extends AppCompatActivity {
    TextInputLayout tilTitulo, tilDescripcion, tilHora;
    EditText etTitulo, etDescripcion, etHora;
    Spinner spinnerPeriodo;
    RecyclerView rvPictogramas;
    PictogramAdapter pictogramAdapter;

    Button btnGuardarTask;
    String pictogramaSeleccionado = "";

    SQLiteHelper sqLiteHelper;
    String fecha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_task);

        tilTitulo = findViewById(R.id.tilTitulo);
        tilDescripcion = findViewById(R.id.tilDescripcion);
        tilHora = findViewById(R.id.tilHora);
        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        etHora = findViewById(R.id.etHora);
        spinnerPeriodo = findViewById(R.id.spinnerPeriodo);
        rvPictogramas = findViewById(R.id.rvPictogramas);
        btnGuardarTask = findViewById(R.id.btnGuardarTask);
        findViewById(R.id.btnBackAdd).setOnClickListener(v -> finish());

        sqLiteHelper = new SQLiteHelper(this);
        fecha = getIntent().getStringExtra("fecha");

        configurarPictogramas();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.periodos_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriodo.setAdapter(adapter);

        btnGuardarTask.setOnClickListener(v -> guardarTask());
        etHora.setOnClickListener(v -> mostrarTimePicker());
    }

    private void configurarPictogramas() {
        pictogramAdapter = new PictogramAdapter(PictogramUtils.getPictograms(this), pictogramaSeleccionado, pictogram -> {
            pictogramaSeleccionado = pictogram.getName();
        });
        rvPictogramas.setAdapter(pictogramAdapter);
    }

    private void actualizarSeleccionVisual() {
        if (pictogramAdapter != null) {
            pictogramAdapter.setSelectedPictogram(pictogramaSeleccionado);
        }
    }

    private void mostrarTimePicker(){
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    String amPm = selectedHour >= 12 ? "PM" : "AM";
                    int hourFormat = selectedHour > 12 ? selectedHour - 12 : (selectedHour == 0 ? 12 : selectedHour);
                    String horaStr = String.format("%02d:%02d %s", hourFormat, selectedMinute, amPm);
                    etHora.setText(horaStr);
                },
                hour,
                minute,
                false
        );
        dialog.show();
    }

    private void guardarTask(){
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String hora = etHora.getText().toString().trim();
        String periodo = spinnerPeriodo.getSelectedItem().toString();

        boolean error = false;
        tilTitulo.setError(null);
        tilHora.setError(null);

        if(titulo.isEmpty()){
            tilTitulo.setError(getString(R.string.task_error_fields));
            error = true;
        }
        if(hora.isEmpty()){
            tilHora.setError(getString(R.string.task_error_fields));
            error = true;
        }
        if (pictogramaSeleccionado.isEmpty()) {
            Toast.makeText(this, getString(R.string.profile_select_color_warning), Toast.LENGTH_SHORT).show();
            error = true;
        }

        if (error) return;

        Task task = new Task(0, titulo, descripcion, fecha, hora, periodo, pictogramaSeleccionado, 0, 1);
        sqLiteHelper.insertarTask(task);
        
        // Notificar al widget para que se actualice tras agregar una nueva tarea
        Intent intentUpdate = new Intent(this, com.example.micalendario.widgets.NextTaskWidget.class);
        intentUpdate.setAction(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = android.appwidget.AppWidgetManager.getInstance(getApplication())
                .getAppWidgetIds(new android.content.ComponentName(getApplication(), com.example.micalendario.widgets.NextTaskWidget.class));
        intentUpdate.putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intentUpdate);
        
        NotificationHelper.mostrarNotificacion(this, "Actividad creada", titulo + " - " + hora);
        programarNotificacion(titulo, hora, pictogramaSeleccionado);
        
        Toast.makeText(this, "Actividad guardada", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void programarNotificacion(String titulo, String hora, String pictograma){
        try{
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            
            // 1. Calcular el tiempo base
            Calendar calendar = Calendar.getInstance();
            String[] partes = hora.split(" ");
            String[] tiempo = partes[0].split(":");
            int hour = Integer.parseInt(tiempo[0]);
            int minute = Integer.parseInt(tiempo[1]);
            String amPm = partes[1];

            if(amPm.equalsIgnoreCase("PM") && hour != 12) hour += 12;
            if(amPm.equalsIgnoreCase("AM") && hour == 12) hour = 0;

            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            
            if(calendar.before(Calendar.getInstance())) calendar.add(Calendar.DAY_OF_MONTH, 1);

            // 2. Programar notificación de ANTICIPACIÓN (5 minutos antes) - HU 4
            Calendar calendarAnticipacion = (Calendar) calendar.clone();
            calendarAnticipacion.add(Calendar.MINUTE, -5);
            
            if (calendarAnticipacion.after(Calendar.getInstance())) {
                Intent intentAnt = new Intent(this, com.example.micalendario.notifications.NotificationReceiver.class);
                intentAnt.putExtra("titulo", titulo);
                intentAnt.putExtra("pictograma", pictograma);
                intentAnt.putExtra("esAnticipacion", true);

                PendingIntent pendingAnt = PendingIntent.getBroadcast(
                        this, (int) System.currentTimeMillis() + 1, intentAnt,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendarAnticipacion.getTimeInMillis(), pendingAnt);
            }

            // 3. Programar notificación de MOMENTO EXACTO - HU 5
            Intent intentExacto = new Intent(this, com.example.micalendario.notifications.NotificationReceiver.class);
            intentExacto.putExtra("titulo", titulo);
            intentExacto.putExtra("pictograma", pictograma);
            intentExacto.putExtra("esAnticipacion", false);

            PendingIntent pendingExacto = PendingIntent.getBroadcast(
                    this, (int) System.currentTimeMillis() + 2, intentExacto,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingExacto);

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
