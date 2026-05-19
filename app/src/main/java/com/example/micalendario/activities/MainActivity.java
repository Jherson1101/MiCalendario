package com.example.micalendario.activities;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.micalendario.R;
import com.example.micalendario.adapters.TaskAdapter;
import com.example.micalendario.database.SQLiteHelper;
import com.example.micalendario.models.Task;

import android.content.Intent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerTasks;
    List<Task> listaTasks;
    TaskAdapter taskAdapter;
    FloatingActionButton fabAgregar;
    SQLiteHelper sqLiteHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        recyclerTasks = findViewById(R.id.recyclerTasks);
        fabAgregar = findViewById(R.id.fabAgregar);
        recyclerTasks.setLayoutManager(new LinearLayoutManager(this));

        sqLiteHelper = new SQLiteHelper(this);
        insertarDatosIniciales();
        listaTasks = sqLiteHelper.obtenerTasks();
        taskAdapter = new TaskAdapter(this, listaTasks);

        recyclerTasks.setAdapter(taskAdapter);
        fabAgregar.setOnClickListener(v -> {

            Intent intent = new Intent(MainActivity.this,
                    AddTaskActivity.class);
            startActivity(intent);
        });

    }
    @Override
    protected void onResume() {

        super.onResume();

        listaTasks.clear();

        listaTasks.addAll(sqLiteHelper.obtenerTasks());

        taskAdapter.notifyDataSetChanged();
    }
    private void insertarDatosIniciales(){

        if(sqLiteHelper.obtenerTasks().size() == 0){

            sqLiteHelper.insertarTask(new Task(
                    1,
                    "Desayunar",
                    "Tomar desayuno saludable",
                    "2026-05-18",
                    "08:00 AM",
                    "Mañana",
                    "",
                    0,
                    1
            ));

            sqLiteHelper.insertarTask(new Task(
                    2,
                    "Cepillarse los dientes",
                    "Lavarse correctamente",
                    "2026-05-18",
                    "09:00 AM",
                    "Mañana",
                    "",
                    0,
                    1
            ));

            sqLiteHelper.insertarTask(new Task(
                    3,
                    "Hacer tareas",
                    "Completar tareas escolares",
                    "2026-05-18",
                    "04:00 PM",
                    "Tarde",
                    "",
                    0,
                    1
            ));
        }
    }
}
