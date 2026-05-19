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

public class EditTaskActivity extends AppCompatActivity {

    EditText etTituloEdit;
    EditText etDescripcionEdit;
    EditText etHoraEdit;
    EditText etPeriodoEdit;

    Button btnActualizarTask;

    SQLiteHelper sqLiteHelper;

    int taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_task);

        etTituloEdit = findViewById(R.id.etTituloEdit);
        etDescripcionEdit = findViewById(R.id.etDescripcionEdit);
        etHoraEdit = findViewById(R.id.etHoraEdit);
        etPeriodoEdit = findViewById(R.id.etPeriodoEdit);

        btnActualizarTask = findViewById(R.id.btnActualizarTask);
        sqLiteHelper = new SQLiteHelper(this);
        obtenerDatos();
        btnActualizarTask.setOnClickListener(v -> actualizarTask());
    }
    private void obtenerDatos(){
        taskId = getIntent().getIntExtra("id", 0);
        etTituloEdit.setText(getIntent().getStringExtra("titulo"));
        etDescripcionEdit.setText(getIntent().getStringExtra("descripcion"));
        etHoraEdit.setText(getIntent().getStringExtra("hora"));
        etPeriodoEdit.setText(getIntent().getStringExtra("periodo"));
    }
    private void actualizarTask(){
        String titulo = etTituloEdit.getText().toString().trim();
        String descripcion = etDescripcionEdit.getText().toString().trim();
        String hora = etHoraEdit.getText().toString().trim();
        String periodo = etPeriodoEdit.getText().toString().trim();
        Task task = new Task(taskId, titulo,
                descripcion,
                "",
                hora,
                periodo,
                "",
                0,
                1
        );

        sqLiteHelper.actualizarTask(task);
        Toast.makeText(this,
                "Actividad actualizada",
                Toast.LENGTH_SHORT).show();
        finish();
    }
}