package com.example.micalendario.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.micalendario.R;
import com.example.micalendario.database.SQLiteHelper;
import com.example.micalendario.models.Task;

import java.util.List;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieData;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import com.google.android.material.button.MaterialButton;

import android.graphics.Color;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class ReporteActivity extends AppCompatActivity {

    TextView tvCompletadas;
    TextView tvPendientes;
    TextView tvPorcentaje;

    LinearProgressIndicator progressBarInforme;
    PieChart pieChart;
    Button btnVolver;
    ImageButton btnBack;
    
    MaterialButton btnHoy, btnSemana, btnMes;
    TextView tvSummaryTitle, tvRangoTitle;

    SQLiteHelper sqLiteHelper;
    boolean modoOscuro = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reporte);

        // Cargar preferencia de modo oscuro
        android.content.SharedPreferences prefs = getSharedPreferences("Configuracion", MODE_PRIVATE);
        modoOscuro = prefs.getBoolean("modoOscuro", false);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_reporte), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvCompletadas = findViewById(R.id.tvCompletadas);
        tvPendientes = findViewById(R.id.tvPendientes);
        tvPorcentaje = findViewById(R.id.tvPorcentaje);

        progressBarInforme = findViewById(R.id.progressBarInforme);
        pieChart = findViewById(R.id.pieChart);
        btnVolver = findViewById(R.id.btnVolver);
        btnBack = findViewById(R.id.btnBackReporte);
        
        btnHoy = findViewById(R.id.btnHoy);
        btnSemana = findViewById(R.id.btnSemana);
        btnMes = findViewById(R.id.btnMes);
        tvSummaryTitle = findViewById(R.id.tvSummaryTitle);
        tvRangoTitle = findViewById(R.id.tvRangoTitle);
        
        sqLiteHelper = new SQLiteHelper(this);
        
        if (modoOscuro) {
            findViewById(R.id.main_reporte).setBackgroundColor(getColor(R.color.dark_background));
            tvCompletadas.setTextColor(getColor(R.color.dark_text));
            tvPendientes.setTextColor(getColor(R.color.dark_text));
            tvPorcentaje.setTextColor(getColor(R.color.blue_primary));
            tvSummaryTitle.setTextColor(getColor(R.color.dark_text));
            tvRangoTitle.setTextColor(getColor(R.color.dark_text));
        }

        btnHoy.setOnClickListener(v -> cargarInforme("hoy"));
        btnSemana.setOnClickListener(v -> cargarInforme("semana"));
        btnMes.setOnClickListener(v -> cargarInforme("mes"));

        cargarInforme("hoy");
        btnVolver.setOnClickListener(v -> finish());
        btnBack.setOnClickListener(v -> finish());
    }
    private void cargarInforme(String rango) {
        List<Task> listaTasks;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String fechaFin = sdf.format(calendar.getTime());

        marcarBotonActivo(rango);

        if (rango.equals("hoy")) {
            listaTasks = sqLiteHelper.obtenerTasksPorFecha(fechaFin);
            tvSummaryTitle.setText(R.string.report_summary_today);
        } else if (rango.equals("semana")) {
            calendar.add(Calendar.DAY_OF_YEAR, -7);
            String fechaInicio = sdf.format(calendar.getTime());
            listaTasks = sqLiteHelper.obtenerTasksPorRango(fechaInicio, fechaFin);
            tvSummaryTitle.setText(R.string.report_summary_weekly);
        } else {
            calendar.add(Calendar.MONTH, -1);
            String fechaInicio = sdf.format(calendar.getTime());
            listaTasks = sqLiteHelper.obtenerTasksPorRango(fechaInicio, fechaFin);
            tvSummaryTitle.setText(R.string.report_summary_monthly);
        }

        int completadas = 0;
        int pendientes = 0;
        for (Task task : listaTasks) {
            if (task.getCompletada() == 1) {
                completadas++;
            } else {
                pendientes++;
            }
        }
        // calcula total de tareas
        int total = completadas + pendientes;
        int porcentaje = 0;
        if (total > 0) {
            porcentaje = (completadas * 100) / total;
        }
        // muestra cantidad completadas con etiquetas descriptivas
        tvCompletadas.setText(getString(R.string.report_completed_label, completadas));
        tvPendientes.setText(getString(R.string.report_pending_label, pendientes));
        tvPorcentaje.setText(getString(R.string.report_independence_level, porcentaje));
        progressBarInforme.setProgress(porcentaje, true);

        // se crea el grafico circular
        ArrayList<PieEntry> entradas = new ArrayList<>();
        if (completadas > 0) entradas.add(new PieEntry(completadas, getString(R.string.report_status_achieved)));
        if (pendientes > 0) entradas.add(new PieEntry(pendientes, getString(R.string.report_status_pending)));

        if (entradas.isEmpty() && total == 0) {
            pieChart.setNoDataText(getString(R.string.report_no_data));
            pieChart.clear();
            return;
        }

        // crea conjunto de datos para el grafico
        PieDataSet dataSet = new PieDataSet(entradas, "");
        // configuracion de colores suaves pero claros
        ArrayList<Integer> colores = new ArrayList<>();
        colores.add(Color.rgb(129, 199, 132)); // Verde suave
        colores.add(Color.rgb(255, 138, 101)); // Naranja suave

        dataSet.setColors(colores);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText(getString(R.string.report_independence_center, porcentaje));
        pieChart.setCenterTextSize(18f);

        if (modoOscuro) {
            pieChart.setCenterTextColor(Color.WHITE);
            pieChart.setHoleColor(Color.parseColor("#121212"));
            pieChart.getLegend().setTextColor(Color.WHITE);
            pieChart.setEntryLabelColor(Color.WHITE);
        } else {
            pieChart.setHoleColor(Color.WHITE);
            pieChart.setEntryLabelColor(Color.BLACK);
        }

        pieChart.setHoleRadius(60f);
        pieChart.getLegend().setEnabled(false);
        pieChart.animateY(800);
        pieChart.invalidate();
    }

    private void marcarBotonActivo(String rango) {
        // Resetear estilos
        int colorNormal = getColor(R.color.blue_light);
        int colorTextoNormal = getColor(R.color.blue_primary);
        int colorActivo = getColor(R.color.blue_primary);
        int colorTextoActivo = Color.WHITE;

        btnHoy.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorNormal));
        btnHoy.setTextColor(colorTextoNormal);
        btnSemana.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorNormal));
        btnSemana.setTextColor(colorTextoNormal);
        btnMes.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorNormal));
        btnMes.setTextColor(colorTextoNormal);

        if (rango.equals("hoy")) {
            btnHoy.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorActivo));
            btnHoy.setTextColor(colorTextoActivo);
        } else if (rango.equals("semana")) {
            btnSemana.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorActivo));
            btnSemana.setTextColor(colorTextoActivo);
        } else if (rango.equals("mes")) {
            btnMes.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorActivo));
            btnMes.setTextColor(colorTextoActivo);
        }
    }
}