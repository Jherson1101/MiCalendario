package com.example.micalendario.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.micalendario.R;
import com.example.micalendario.database.SQLiteHelper;
import com.example.micalendario.models.Task;
import com.example.micalendario.utils.NotificationHelper;

import android.app.TimePickerDialog;
import android.graphics.Color;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.Calendar;


import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.card.MaterialCardView;

public class EditTaskActivity extends AppCompatActivity {

    TextInputLayout tilTituloEdit, tilDescripcionEdit, tilHoraEdit;
    EditText etTituloEdit;
    EditText etDescripcionEdit;
    EditText etHoraEdit;
    Spinner spinnerPeriodoEdit;
    MaterialCardView cardDesayunoEdit, cardDientesEdit, cardEstudiarEdit, cardJugarEdit, cardDuchaEdit, cardDormirEdit;
    ImageView imgDesayunoEdit, imgDientesEdit, imgEstudiarEdit, imgJugarEdit, imgDuchaEdit, imgDormirEdit;

    Button btnActualizarTask;
    String pictogramaSeleccionado = "";
    SQLiteHelper sqLiteHelper;

    int taskId;

    String fecha;
    boolean modoOscuro = false;

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

        cardDesayunoEdit = findViewById(R.id.cardPictoDesayunoEdit);
        cardDientesEdit = findViewById(R.id.cardPictoDientesEdit);
        cardEstudiarEdit = findViewById(R.id.cardPictoEstudiarEdit);
        cardJugarEdit = findViewById(R.id.cardPictoJugarEdit);
        cardDuchaEdit = findViewById(R.id.cardPictoDuchaEdit);
        cardDormirEdit = findViewById(R.id.cardPictoDormirEdit);

        imgDesayunoEdit = findViewById(R.id.imgPictoDesayunoEdit);
        imgDientesEdit = findViewById(R.id.imgPictoDientesEdit);
        imgEstudiarEdit = findViewById(R.id.imgPictoEstudiarEdit);
        imgJugarEdit = findViewById(R.id.imgPictoJugarEdit);
        imgDuchaEdit = findViewById(R.id.imgPictoDuchaEdit);
        imgDormirEdit = findViewById(R.id.imgPictoDormirEdit);

        btnActualizarTask = findViewById(R.id.btnActualizarTask);
        sqLiteHelper = new SQLiteHelper(this);

        // Configurar Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.periodos_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriodoEdit.setAdapter(adapter);

        // Configurar selección de pictogramas
        cardDesayunoEdit.setOnClickListener(v -> seleccionarPictograma("desayuno", cardDesayunoEdit));
        cardDientesEdit.setOnClickListener(v -> seleccionarPictograma("dientes", cardDientesEdit));
        cardEstudiarEdit.setOnClickListener(v -> seleccionarPictograma("estudiar", cardEstudiarEdit));
        cardJugarEdit.setOnClickListener(v -> seleccionarPictograma("jugar", cardJugarEdit));
        cardDuchaEdit.setOnClickListener(v -> seleccionarPictograma("ducha", cardDuchaEdit));
        cardDormirEdit.setOnClickListener(v -> seleccionarPictograma("dormir", cardDormirEdit));

        etHoraEdit.setOnClickListener(v -> mostrarTimePicker());

        obtenerDatos();
        aplicarModoOscuro();
        btnActualizarTask.setOnClickListener(v -> actualizarTask());
    }

    // metodo que recibe y muestra los datos de la tarea
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
        seleccionarPictogramaVisual(pictogramaSeleccionado);

        fecha = getIntent().getStringExtra("fecha");
        modoOscuro = getIntent().getBooleanExtra("modoOscuro", false);
    }

    private void seleccionarPictograma(String nombre, MaterialCardView card) {
        pictogramaSeleccionado = nombre;
        limpiarSeleccionPictogramas();
        card.setStrokeColor(getColor(R.color.blue_primary));
        card.setStrokeWidth(4);
        card.setCardElevation(8f);
    }

    private void seleccionarPictogramaVisual(String nombre) {
        if (nombre == null) return;
        MaterialCardView card = null;
        switch (nombre) {
            case "desayuno": card = cardDesayunoEdit; break;
            case "dientes": card = cardDientesEdit; break;
            case "estudiar": card = cardEstudiarEdit; break;
            case "jugar": card = cardJugarEdit; break;
            case "ducha": card = cardDuchaEdit; break;
            case "dormir": card = cardDormirEdit; break;
        }
        if (card != null) {
            seleccionarPictograma(nombre, card);
        }
    }

    private void limpiarSeleccionPictogramas() {
        cardDesayunoEdit.setStrokeWidth(0);
        cardDientesEdit.setStrokeWidth(0);
        cardEstudiarEdit.setStrokeWidth(0);
        cardJugarEdit.setStrokeWidth(0);
        cardDuchaEdit.setStrokeWidth(0);
        cardDormirEdit.setStrokeWidth(0);

        cardDesayunoEdit.setCardElevation(2f);
        cardDientesEdit.setCardElevation(2f);
        cardEstudiarEdit.setCardElevation(2f);
        cardJugarEdit.setCardElevation(2f);
        cardDuchaEdit.setCardElevation(2f);
        cardDormirEdit.setCardElevation(2f);
    }

    private void aplicarModoOscuro() {
        if (modoOscuro) {
            findViewById(R.id.main_layout_edit).setBackgroundColor(getColor(R.color.dark_background));
            etTituloEdit.setTextColor(getColor(R.color.dark_text));
            etTituloEdit.setHintTextColor(getColor(R.color.text_secondary));
            etDescripcionEdit.setTextColor(getColor(R.color.dark_text));
            etDescripcionEdit.setHintTextColor(getColor(R.color.text_secondary));
            etHoraEdit.setTextColor(getColor(R.color.dark_text));
            etHoraEdit.setHintTextColor(getColor(R.color.text_secondary));
            ((TextView)findViewById(R.id.tvEditarActividad)).setTextColor(getColor(R.color.dark_text));
            ((TextView)findViewById(R.id.tvSeleccionaPictoEdit)).setTextColor(getColor(R.color.dark_text));
        }
    }

    // metodo que actualiza la tarea
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

        Task task = new Task(taskId,
                titulo,
                descripcion,
                fecha,
                hora,
                periodo,
                pictogramaSeleccionado,
                0,
                1
        );

        sqLiteHelper.actualizarTask(task);

        // para notificar alerta de cambios en agregar,editar y eliminar
        NotificationHelper.mostrarNotificacion(
                this,
                "Actividad modificada",
                titulo + " - " + hora
        );

        Toast.makeText(this,
                "Actividad actualizada",
                Toast.LENGTH_SHORT).show();
        finish();
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
                    String hora = String.format("%02d:%02d %s", hourFormat, selectedMinute, amPm);
                    etHoraEdit.setText(hora);
                },
                hour,
                minute,
                false
        );
        dialog.show();
    }
}