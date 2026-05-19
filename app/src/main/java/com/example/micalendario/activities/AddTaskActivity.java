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

public class AddTaskActivity extends AppCompatActivity {
    EditText etTitulo;
    EditText etDescripcion;
    EditText etHora;
    EditText etPeriodo;

    Button btnGuardarTask;

    SQLiteHelper sqLiteHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_task);

        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        etHora = findViewById(R.id.etHora);
        etPeriodo = findViewById(R.id.etPeriodo);

        btnGuardarTask = findViewById(R.id.btnGuardarTask);

        sqLiteHelper = new SQLiteHelper(this);

        btnGuardarTask.setOnClickListener(v -> guardarTask());
    }
    private void guardarTask(){

        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String hora = etHora.getText().toString().trim();
        String periodo = etPeriodo.getText().toString().trim();

        if(titulo.isEmpty() ||
                descripcion.isEmpty() ||
                hora.isEmpty() ||
                periodo.isEmpty()){

            Toast.makeText(this,
                    "Complete todos los campos",
                    Toast.LENGTH_SHORT).show();

            return;
        }

        Task task = new Task(
                0,
                titulo,
                descripcion,
                "2026-05-18",
                hora,
                periodo,
                "",
                0,
                1
        );

        sqLiteHelper.insertarTask(task);

        Toast.makeText(this,
                "Actividad guardada",
                Toast.LENGTH_SHORT).show();

        finish();
    }
}