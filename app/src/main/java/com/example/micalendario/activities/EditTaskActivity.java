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

import android.app.TimePickerDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import com.google.android.material.textfield.TextInputLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.example.micalendario.adapters.PictogramAdapter;
import com.example.micalendario.utils.PictogramUtils;

public class EditTaskActivity extends AppCompatActivity {

    TextInputLayout tilTituloEdit, tilDescripcionEdit, tilHoraEdit;
    EditText etTituloEdit, etDescripcionEdit, etHoraEdit;
    Spinner spinnerPeriodoEdit;
    RecyclerView rvPictogramasEdit;
    PictogramAdapter pictogramAdapter;

    Button btnActualizarTask;
    String pictogramaSeleccionado = "";
    SQLiteHelper sqLiteHelper;

    int taskId;
    String fecha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_task);

        tilTituloEdit = findViewById(R.id.tilTituloEdit);
        tilDescripcionEdit = findViewById(R.id.tilDescripcionEdit);
        tilHoraEdit = findViewById(R.id.tilHoraEdit);
        etTituloEdit = findViewById(R.id.etTituloEdit);
        etDescripcionEdit = findViewById(R.id.etDescripcionEdit);
        etHoraEdit = findViewById(R.id.etHoraEdit);
        spinnerPeriodoEdit = findViewById(R.id.spinnerPeriodoEdit);
        rvPictogramasEdit = findViewById(R.id.rvPictogramasEdit);
        btnActualizarTask = findViewById(R.id.btnActualizarTask);

        sqLiteHelper = new SQLiteHelper(this);

        // Configurar Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.periodos_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriodoEdit.setAdapter(adapter);

        configurarPictogramas();
        obtenerDatos();

        etHoraEdit.setOnClickListener(v -> mostrarTimePicker());
        btnActualizarTask.setOnClickListener(v -> actualizarTask());
    }

    private void configurarPictogramas() {
        pictogramAdapter = new PictogramAdapter(PictogramUtils.getPictograms(this), pictogramaSeleccionado, pictogram -> {
            pictogramaSeleccionado = pictogram.getName();
        });
        rvPictogramasEdit.setAdapter(pictogramAdapter);
    }

    private void actualizarSeleccionVisual() {
        if (pictogramAdapter != null) {
            pictogramAdapter.setSelectedPictogram(pictogramaSeleccionado);
        }
    }

    private void obtenerDatos(){
        taskId = getIntent().getIntExtra("id", 0);
        etTituloEdit.setText(getIntent().getStringExtra("titulo"));
        etDescripcionEdit.setText(getIntent().getStringExtra("descripcion"));
        etHoraEdit.setText(getIntent().getStringExtra("hora"));
        
        String periodo = getIntent().getStringExtra("periodo");
        if (periodo != null) {
            ArrayAdapter adapter = (ArrayAdapter) spinnerPeriodoEdit.getAdapter();
            int position = adapter.getPosition(periodo);
            spinnerPeriodoEdit.setSelection(position);
        }

        pictogramaSeleccionado = getIntent().getStringExtra("pictograma");
        actualizarSeleccionVisual();

        fecha = getIntent().getStringExtra("fecha");
    }

    private void actualizarTask(){
        String titulo = etTituloEdit.getText().toString().trim();
        String descripcion = etDescripcionEdit.getText().toString().trim();
        String hora = etHoraEdit.getText().toString().trim();
        String periodo = spinnerPeriodoEdit.getSelectedItem().toString();

        boolean error = false;
        tilTituloEdit.setError(null);
        tilHoraEdit.setError(null);

        if(titulo.isEmpty()){
            tilTituloEdit.setError(getString(R.string.task_error_fields));
            error = true;
        }
        if(hora.isEmpty()){
            tilHoraEdit.setError(getString(R.string.task_error_fields));
            error = true;
        }

        if (error) return;

        Task task = new Task(taskId, titulo, descripcion, fecha, hora, periodo, pictogramaSeleccionado, 0, 1);
        sqLiteHelper.actualizarTask(task);

        NotificationHelper.mostrarNotificacion(this, "Actividad modificada", titulo + " - " + hora);
        programarNotificacion(titulo, hora, pictogramaSeleccionado);
        
        Toast.makeText(this, "Actividad actualizada", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void programarNotificacion(String titulo, String hora, String pictograma){
        try{
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
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

            // 1. Notificación de Anticipación (5 min antes)
            Calendar calAnt = (Calendar) calendar.clone();
            calAnt.add(Calendar.MINUTE, -5);
            if (calAnt.after(Calendar.getInstance())) {
                Intent intentAnt = new Intent(this, com.example.micalendario.notifications.NotificationReceiver.class);
                intentAnt.putExtra("titulo", titulo);
                intentAnt.putExtra("pictograma", pictograma);
                intentAnt.putExtra("esAnticipacion", true);
                PendingIntent piAnt = PendingIntent.getBroadcast(this, taskId + 1000, intentAnt, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calAnt.getTimeInMillis(), piAnt);
            }

            // 2. Notificación Momento Exacto
            Intent intentEx = new Intent(this, com.example.micalendario.notifications.NotificationReceiver.class);
            intentEx.putExtra("titulo", titulo);
            intentEx.putExtra("pictograma", pictograma);
            intentEx.putExtra("esAnticipacion", false);
            PendingIntent piEx = PendingIntent.getBroadcast(this, taskId, intentEx, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), piEx);

        } catch (Exception e){
            e.printStackTrace();
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
                    etHoraEdit.setText(horaStr);
                },
                hour,
                minute,
                false
        );
        dialog.show();
    }
}
