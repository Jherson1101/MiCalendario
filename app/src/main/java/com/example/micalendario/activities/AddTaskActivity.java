package com.example.micalendario.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

// librerias de androidX
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.micalendario.R;
import com.example.micalendario.database.SQLiteHelper;
import com.example.micalendario.models.Task;

// librerias para la alarma
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
// libreria para manejo de fechas y horas
import java.util.Calendar;

// librerias para selector de hora y Spinner
import android.app.TimePickerDialog;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.graphics.Color;

import com.example.micalendario.utils.NotificationHelper;

import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.card.MaterialCardView;

public class AddTaskActivity extends AppCompatActivity {
    TextInputLayout tilTitulo, tilDescripcion, tilHora;
    EditText etTitulo, etDescripcion, etHora;
    Spinner spinnerPeriodo;
    MaterialCardView cardDesayuno, cardDientes, cardEstudiar, cardJugar, cardDucha, cardDormir;
    ImageView imgDesayuno, imgDientes, imgEstudiar, imgJugar, imgDucha, imgDormir;

    Button btnGuardarTask;
    String pictogramaSeleccionado = "";

    SQLiteHelper sqLiteHelper;

    String fecha;
    boolean modoOscuro = false;
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

        cardDesayuno = findViewById(R.id.cardPictoDesayuno);
        cardDientes = findViewById(R.id.cardPictoDientes);
        cardEstudiar = findViewById(R.id.cardPictoEstudiar);
        cardJugar = findViewById(R.id.cardPictoJugar);
        cardDucha = findViewById(R.id.cardPictoDucha);
        cardDormir = findViewById(R.id.cardPictoDormir);

        imgDesayuno = findViewById(R.id.imgPictoDesayuno);
        imgDientes = findViewById(R.id.imgPictoDientes);
        imgEstudiar = findViewById(R.id.imgPictoEstudiar);
        imgJugar = findViewById(R.id.imgPictoJugar);
        imgDucha = findViewById(R.id.imgPictoDucha);
        imgDormir = findViewById(R.id.imgPictoDormir);

        btnGuardarTask = findViewById(R.id.btnGuardarTask);
        sqLiteHelper = new SQLiteHelper(this);
        fecha = getIntent().getStringExtra("fecha");
        
        // Cargar preferencia de modo oscuro global
        android.content.SharedPreferences prefs = getSharedPreferences("Configuracion", MODE_PRIVATE);
        modoOscuro = prefs.getBoolean("modoOscuro", false);

        aplicarModoOscuro();

        // Configurar selección de pictogramas
        cardDesayuno.setOnClickListener(v -> seleccionarPictograma("desayuno", cardDesayuno));
        cardDientes.setOnClickListener(v -> seleccionarPictograma("dientes", cardDientes));
        cardEstudiar.setOnClickListener(v -> seleccionarPictograma("estudiar", cardEstudiar));
        cardJugar.setOnClickListener(v -> seleccionarPictograma("jugar", cardJugar));
        cardDucha.setOnClickListener(v -> seleccionarPictograma("ducha", cardDucha));
        cardDormir.setOnClickListener(v -> seleccionarPictograma("dormir", cardDormir));

        // crea adaptador para llenar el spinner con datos del array XML
        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.periodos_array,
                        android.R.layout.simple_spinner_item
                );
        // define el diseño desplegable del spinner
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerPeriodo.setAdapter(adapter);
        // evento click del boton guardar
        btnGuardarTask.setOnClickListener(v -> guardarTask());
        // evento click del campo hora
        etHora.setOnClickListener(v -> mostrarTimePicker());
    }

    private void seleccionarPictograma(String nombre, MaterialCardView card) {
        pictogramaSeleccionado = nombre;
        // Limpiar selecciones previas
        cardDesayuno.setStrokeWidth(0);
        cardDientes.setStrokeWidth(0);
        cardEstudiar.setStrokeWidth(0);
        cardJugar.setStrokeWidth(0);
        cardDucha.setStrokeWidth(0);
        cardDormir.setStrokeWidth(0);

        cardDesayuno.setCardElevation(2f);
        cardDientes.setCardElevation(2f);
        cardEstudiar.setCardElevation(2f);
        cardJugar.setCardElevation(2f);
        cardDucha.setCardElevation(2f);
        cardDormir.setCardElevation(2f);

        // Resaltar el seleccionado
        card.setStrokeColor(getColor(R.color.blue_primary));
        card.setStrokeWidth(4);
        card.setCardElevation(8f);
    }

    private void aplicarModoOscuro() {
        if (modoOscuro) {
            findViewById(R.id.main_layout_add).setBackgroundColor(getColor(R.color.dark_background));
            etTitulo.setTextColor(getColor(R.color.dark_text));
            etTitulo.setHintTextColor(getColor(R.color.text_secondary));
            etDescripcion.setTextColor(getColor(R.color.dark_text));
            etDescripcion.setHintTextColor(getColor(R.color.text_secondary));
            etHora.setTextColor(getColor(R.color.dark_text));
            etHora.setHintTextColor(getColor(R.color.text_secondary));
            ((TextView)findViewById(R.id.tvNuevaActividad)).setTextColor(getColor(R.color.dark_text));
            ((TextView)findViewById(R.id.tvSeleccionaPicto)).setTextColor(getColor(R.color.dark_text));
        }
    }

    // metodo que muestra el selector de hora
    private void mostrarTimePicker(){
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    String amPm = "AM";
                    int hourFormat = selectedHour;
                    if(selectedHour >= 12){
                        amPm = "PM";
                    }
                    if(selectedHour > 12){
                        hourFormat -= 12;
                    }
                    if(hourFormat == 0){
                        hourFormat = 12;
                    }
                    String hora = String.format(
                            "%02d:%02d %s",
                            hourFormat,
                            selectedMinute,
                            amPm
                    );

                    etHora.setText(hora);
                },
                hour,
                minute,
                false
        );
        dialog.show();
    }

    // metodo para guardar la tarea
    private void guardarTask(){

        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String hora = etHora.getText().toString().trim();
        String periodo = spinnerPeriodo.getSelectedItem().toString();

        boolean error = false;
        tilTitulo.setError(null);
        tilHora.setError(null);

        // validacion de campos vacios
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

        Task task = new Task(
                0,
                titulo,
                descripcion,
                fecha,
                hora,
                periodo,
                pictogramaSeleccionado,
                0,
                1
        );

        sqLiteHelper.insertarTask(task);
        // para notificar alertas de cambios(agregar, editar, eliminar)
        NotificationHelper.mostrarNotificacion(
                this,
                "Actividad creada",
                titulo + " - " + hora
        );

        // programa notificacion recordatorio
        programarNotificacion(titulo, hora, pictogramaSeleccionado);
        Toast.makeText(this,
                "Actividad guardada",
                Toast.LENGTH_SHORT).show();
        finish();
    }
    // metodo encargado de programar notificaciones
    private void programarNotificacion(String titulo, String hora, String pictograma){

        try{

            Intent intent = new Intent(
                    this,
                    com.example.micalendario.notifications.NotificationReceiver.class
            );

            intent.putExtra("titulo", titulo);
            intent.putExtra("pictograma", pictograma);

            PendingIntent pendingIntent =
                    PendingIntent.getBroadcast(
                            this,
                            (int) System.currentTimeMillis(),
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT |
                                    PendingIntent.FLAG_IMMUTABLE
                    );

            AlarmManager alarmManager =
                    (AlarmManager) getSystemService(ALARM_SERVICE);

            Calendar calendar = Calendar.getInstance();

            String[] partes = hora.split(" ");

            String[] tiempo = partes[0].split(":");

            int hour = Integer.parseInt(tiempo[0]);
            int minute = Integer.parseInt(tiempo[1]);

            String periodo = partes[1];

            if(periodo.equalsIgnoreCase("PM") && hour != 12){
                hour += 12;
            }

            if(periodo.equalsIgnoreCase("AM") && hour == 12){
                hour = 0;
            }

            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            if(calendar.before(Calendar.getInstance())){
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );

            Toast.makeText(
                    this,
                    "Recordatorio programado",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e){

            e.printStackTrace();

            Toast.makeText(
                    this,
                    "Error al programar notificación",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}