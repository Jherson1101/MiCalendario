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
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

import android.app.DatePickerDialog;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Calendar;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerTasks;
    List<Task> listaTasks;
    TaskAdapter taskAdapter;
    FloatingActionButton fabAgregar;
    com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigation;
    ImageButton btnSettings, btnInformes, btnPerfil;
    Button btnFiltroTodo, btnFiltroManana, btnFiltroTarde, btnFiltroNoche;
    TextView tvBienvenida, tvFecha;
    SQLiteHelper sqLiteHelper;
    String fechaActual = "";
    String rol = "";
    boolean modoOscuro = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        rol = getIntent().getStringExtra("rol");
        if (rol == null) rol = "usuario"; // Valor por defecto para evitar errores
        pedirPermisoNotificaciones();

        // Cargar preferencia de modo oscuro
        android.content.SharedPreferences prefs = getSharedPreferences("Configuracion", MODE_PRIVATE);
        modoOscuro = prefs.getBoolean("modoOscuro", false);
        AppCompatDelegate.setDefaultNightMode(modoOscuro ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        recyclerTasks = findViewById(R.id.recyclerTasks);
        fabAgregar = findViewById(R.id.fabAgregar);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        btnSettings = findViewById(R.id.btnSettings);
        btnInformes = findViewById(R.id.btnInformes);
        btnPerfil = findViewById(R.id.btnPerfil);
        tvBienvenida = findViewById(R.id.tvBienvenida);
        tvFecha = findViewById(R.id.tvFecha);

        // Botones de filtro
        btnFiltroTodo = findViewById(R.id.btnFiltroTodo);
        btnFiltroManana = findViewById(R.id.btnFiltroManana);
        btnFiltroTarde = findViewById(R.id.btnFiltroTarde);
        btnFiltroNoche = findViewById(R.id.btnFiltroNoche);

        // Listeners para filtros
        btnFiltroTodo.setOnClickListener(v -> {
            marcarBotonActivo(btnFiltroTodo);
            cargarTasksPorFecha(fechaActual);
        });
        btnFiltroManana.setOnClickListener(v -> {
            marcarBotonActivo(btnFiltroManana);
            filtrarPorPeriodo("Mañana");
        });
        btnFiltroTarde.setOnClickListener(v -> {
            marcarBotonActivo(btnFiltroTarde);
            filtrarPorPeriodo("Tarde");
        });
        btnFiltroNoche.setOnClickListener(v -> {
            marcarBotonActivo(btnFiltroNoche);
            filtrarPorPeriodo("Noche");
        });

        // condicional para el usuario terapeuta que no puede agregar tareas
        // y que si puede ver los informes
        if(rol.equals("terapeuta")){
            fabAgregar.setVisibility(View.GONE);
            btnInformes.setVisibility(View.VISIBLE);
        }

        recyclerTasks.setLayoutManager(new LinearLayoutManager(this));

        sqLiteHelper = new SQLiteHelper(this);
        insertarDatosIniciales();
        
        // Actualizar Saludo y Fecha
        actualizarSaludoYFecha();
        aplicarColorPerfil();

        fechaActual = obtenerFechaHoy();
        listaTasks = sqLiteHelper.obtenerTasksPorFecha(fechaActual);

        taskAdapter = new TaskAdapter(this, listaTasks, rol, modoOscuro);
        recyclerTasks.setAdapter(taskAdapter);

        // Listener para la barra de navegación inferior
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                findViewById(R.id.tvFecha).setVisibility(View.VISIBLE);
                findViewById(R.id.layoutFiltros).setVisibility(View.VISIBLE);
                actualizarSaludoYFecha();
                cargarTasksPorFecha(obtenerFechaHoy());
                return true;
            } else if (id == R.id.nav_month) {
                findViewById(R.id.tvFecha).setVisibility(View.VISIBLE);
                findViewById(R.id.layoutFiltros).setVisibility(View.GONE);
                tvFecha.setText("Actividades del Mes");
                cargarTasksMes();
                return true;
            } else if (id == R.id.nav_week) {
                findViewById(R.id.tvFecha).setVisibility(View.VISIBLE);
                findViewById(R.id.layoutFiltros).setVisibility(View.GONE);
                tvFecha.setText("Actividades de la Semana");
                cargarTasksSemana();
                return true;
            } else if (id == R.id.nav_list) {
                findViewById(R.id.tvFecha).setVisibility(View.GONE);
                findViewById(R.id.layoutFiltros).setVisibility(View.GONE);
                cargarTodasLasTasks();
                return true;
            }
            return false;
        });

        // boton para informes
        btnInformes.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReporteActivity.class);
            startActivity(intent);
        });

        btnPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PerfilActivity.class);
            intent.putExtra("modoOscuro", modoOscuro);
            startActivity(intent);
        });

        // boton configuracion
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            intent.putExtra("rol", rol);
            startActivity(intent);
        });

        // button para agregar tarea
        fabAgregar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            intent.putExtra("fecha", fechaActual);
            intent.putExtra("modoOscuro", modoOscuro);
            startActivity(intent);
        });
    }

    private void actualizarSaludoYFecha() {
        com.example.micalendario.models.Profile profile = sqLiteHelper.obtenerPerfil();
        String nombre = (profile != null) ? profile.getNombre() : "";
        if (!nombre.isEmpty()) {
            tvBienvenida.setText("¡Hola, " + nombre + "!");
        } else {
            tvBienvenida.setText("¡Hola!");
        }

        String friendlyDate = new SimpleDateFormat("EEEE, d 'de' MMMM", new Locale("es", "ES")).format(Calendar.getInstance().getTime());
        tvFecha.setText(friendlyDate.substring(0, 1).toUpperCase() + friendlyDate.substring(1));
    }
    // metodo para pedir permiso de notificaciones
    private void pedirPermisoNotificaciones(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED){

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        100
                );
            }
        }
    }
    // metodo ejecutado al volver al activity
    @Override
    protected void onResume() {
        super.onResume();
        
        // Recargar modo oscuro por si se cambió en Settings
        android.content.SharedPreferences prefs = getSharedPreferences("Configuracion", MODE_PRIVATE);
        boolean nuevoModoOscuro = prefs.getBoolean("modoOscuro", false);
        if (nuevoModoOscuro != modoOscuro) {
            modoOscuro = nuevoModoOscuro;
            // No llamamos a actualizarInterfazModoOscuro() porque el sistema ya lo maneja
        }

        listaTasks.clear();
        // recarga tarea desde SQLite
        listaTasks.addAll(sqLiteHelper.obtenerTasksPorFecha(fechaActual));
        taskAdapter.notifyDataSetChanged();
    }
    // carga tareas segun la fecha
    private void marcarBotonActivo(Button botonActivo) {
        // Estilo para botones inactivos
        btnFiltroTodo.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.blue_light)));
        btnFiltroManana.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.blue_light)));
        btnFiltroTarde.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.blue_light)));
        btnFiltroNoche.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.blue_light)));

        btnFiltroTodo.setTextColor(getColor(R.color.blue_primary));
        btnFiltroManana.setTextColor(getColor(R.color.blue_primary));
        btnFiltroTarde.setTextColor(getColor(R.color.blue_primary));
        btnFiltroNoche.setTextColor(getColor(R.color.blue_primary));

        // Estilo para el botón activo
        botonActivo.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.blue_primary)));
        botonActivo.setTextColor(getColor(R.color.white));
    }

    private void actualizarInterfazModoOscuro() {
        // El sistema maneja esto automáticamente mediante AppCompatDelegate y recursos DayNight.
    }

    private void cargarTasksPorFecha(String fecha){
        listaTasks.clear();
        listaTasks.addAll(sqLiteHelper.obtenerTasksPorFecha(fecha));
        taskAdapter.notifyDataSetChanged();
    }

    private void filtrarPorPeriodo(String periodo) {
        listaTasks.clear();
        List<Task> todasLasDeLaFecha = sqLiteHelper.obtenerTasksPorFecha(fechaActual);
        for (Task t : todasLasDeLaFecha) {
            if (t.getPeriodo().equalsIgnoreCase(periodo)) {
                listaTasks.add(t);
            }
        }
        taskAdapter.notifyDataSetChanged();
    }

    private void aplicarColorPerfil() {
        com.example.micalendario.models.Profile profile = sqLiteHelper.obtenerPerfil();
        if (profile != null) {
            String color = profile.getColor();
            View mainView = findViewById(R.id.main);
            
            if (color.equals("Azul")) {
                mainView.setBackgroundColor(getColor(R.color.blue_soft));
                recyclerTasks.setBackgroundColor(getColor(R.color.blue_soft));
            } else if (color.equals("Verde")) {
                mainView.setBackgroundColor(getColor(R.color.green_soft));
                recyclerTasks.setBackgroundColor(getColor(R.color.green_soft));
            } else if (color.equals("Amarillo")) {
                mainView.setBackgroundColor(getColor(R.color.yellow_soft));
                recyclerTasks.setBackgroundColor(getColor(R.color.yellow_soft));
            }
        }
    }

    private void cargarTasksSemana() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        String inicio = formatearFecha(cal);
        cal.add(Calendar.DAY_OF_WEEK, 6);
        String fin = formatearFecha(cal);
        
        listaTasks.clear();
        // Ordenar por fecha descendente para ver lo más reciente primero
        List<Task> tareas = sqLiteHelper.obtenerTasksPorRango(inicio, fin);
        tareas.sort((t1, t2) -> t2.getFecha().compareTo(t1.getFecha()));
        listaTasks.addAll(tareas);
        taskAdapter.notifyDataSetChanged();
    }

    private void cargarTasksMes() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        String inicio = formatearFecha(cal);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        String fin = formatearFecha(cal);

        listaTasks.clear();
        // Ordenar por fecha descendente
        List<Task> tareas = sqLiteHelper.obtenerTasksPorRango(inicio, fin);
        tareas.sort((t1, t2) -> t2.getFecha().compareTo(t1.getFecha()));
        listaTasks.addAll(tareas);
        taskAdapter.notifyDataSetChanged();
    }

    private void cargarTodasLasTasks() {
        listaTasks.clear();
        // Ordenar todas por fecha descendente
        List<Task> tareas = sqLiteHelper.obtenerTasks();
        tareas.sort((t1, t2) -> t2.getFecha().compareTo(t1.getFecha()));
        listaTasks.addAll(tareas);
        taskAdapter.notifyDataSetChanged();
    }

    private String formatearFecha(Calendar cal) {
        return String.format(Locale.US, "%04d-%02d-%02d", 
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
    }

    // inserta datos de ejemplo que aparecen al instalar la app
    private void insertarDatosIniciales(){

        if(sqLiteHelper.obtenerTasks().size() == 0){

            sqLiteHelper.insertarTask(new Task(
                    1,
                    "Desayunar",
                    "Tomar desayuno saludable",
                    obtenerFechaHoy(),
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
                    obtenerFechaHoy(),
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
                    obtenerFechaHoy(),
                    "04:00 PM",
                    "Tarde",
                    "",
                    0,
                    1
            ));
        }
    }
    // nos da la fecha actual
    private String obtenerFechaHoy(){

        Calendar calendar = Calendar.getInstance();
        // devuelve fecha formateada YYYY-MM-DD
        return String.format(Locale.US,
                "%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }
}
