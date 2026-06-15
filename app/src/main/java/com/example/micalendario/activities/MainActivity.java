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

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerTasks;
    List<Task> listaTasks;
    TaskAdapter taskAdapter;
    FloatingActionButton fabAgregar;
    Button btnSeleccionarFecha, btnCerrarSesion;
    ImageButton btnModoOscuro, btnInformes, btnPerfil;
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
        pedirPermisoNotificaciones();

        // Cargar preferencia de modo oscuro
        android.content.SharedPreferences prefs = getSharedPreferences("Configuracion", MODE_PRIVATE);
        modoOscuro = prefs.getBoolean("modoOscuro", false);
        // Aplicar inmediatamente si estaba activo
        if (modoOscuro) {
            // Usamos post para asegurar que la UI esté lista
            findViewById(R.id.main).post(() -> actualizarInterfazModoOscuro());
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        recyclerTasks = findViewById(R.id.recyclerTasks);
        fabAgregar = findViewById(R.id.fabAgregar);
        btnSeleccionarFecha = findViewById(R.id.btnSeleccionarFecha);
        btnModoOscuro = findViewById(R.id.btnModoOscuro);
        btnInformes = findViewById(R.id.btnInformes);
        btnPerfil = findViewById(R.id.btnPerfil);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
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

        // boton de cierre de sesión
        btnCerrarSesion.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
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

        // boton modo oscuro
        btnModoOscuro.setOnClickListener(v -> {
            modoOscuro = !modoOscuro;
            
            // Guardar preferencia
            android.content.SharedPreferences.Editor editor = getSharedPreferences("Configuracion", MODE_PRIVATE).edit();
            editor.putBoolean("modoOscuro", modoOscuro);
            editor.apply();

            actualizarInterfazModoOscuro();
        });

        // button para agregar tarea
        fabAgregar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            intent.putExtra("fecha", fechaActual);
            intent.putExtra("modoOscuro", modoOscuro);
            startActivity(intent);
        });

        btnSeleccionarFecha.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dialog = new DatePickerDialog(
                            MainActivity.this,
                            (view, selectedYear, selectedMonth, selectedDay) -> {
                                fechaActual = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                                cargarTasksPorFecha(fechaActual);
                                // Actualizar fecha visual si es hoy, o mostrar la seleccionada
                                Calendar sel = Calendar.getInstance();
                                sel.set(selectedYear, selectedMonth, selectedDay);
                                String friendlyDate = new SimpleDateFormat("EEEE, d 'de' MMMM", new Locale("es", "ES")).format(sel.getTime());
                                tvFecha.setText(friendlyDate);
                            },
                            year, month, day);
            dialog.show();
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
        View mainView = findViewById(R.id.main);
        View topBar = findViewById(R.id.topBar);
        View bottomActions = findViewById(R.id.bottomActions);
        
        if (modoOscuro) {
            mainView.setBackgroundColor(getColor(R.color.dark_background));
            topBar.setBackgroundColor(getColor(R.color.dark_card));
            bottomActions.setBackgroundColor(getColor(R.color.dark_card));
            tvBienvenida.setTextColor(getColor(R.color.dark_text));
            tvFecha.setTextColor(getColor(R.color.text_secondary));
            btnModoOscuro.setImageResource(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP ? android.R.drawable.ic_menu_day : android.R.drawable.ic_menu_compass);
        } else {
            mainView.setBackgroundColor(getColor(R.color.background));
            topBar.setBackgroundColor(getColor(R.color.white));
            bottomActions.setBackgroundColor(getColor(R.color.white));
            tvBienvenida.setTextColor(getColor(R.color.text_primary));
            tvFecha.setTextColor(getColor(R.color.text_secondary));
            btnModoOscuro.setImageResource(android.R.drawable.ic_menu_compass);
            aplicarColorPerfil();
        }

        taskAdapter = new TaskAdapter(this, listaTasks, rol, modoOscuro);
        recyclerTasks.setAdapter(taskAdapter);
        marcarBotonActivo(btnFiltroTodo);
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
        return String.format(
                "%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }
}
