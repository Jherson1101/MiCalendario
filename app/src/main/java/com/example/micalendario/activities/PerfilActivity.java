package com.example.micalendario.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.micalendario.R;
import com.example.micalendario.database.SQLiteHelper;
import com.example.micalendario.models.Profile;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class PerfilActivity extends AppCompatActivity {

    TextView tvNombre, tvEdad, tvColor, tvAvatarLetra;
    View viewBlue, viewGreen, viewYellow;
    Button btnGuardar;
    ImageButton btnBack;

    String colorSeleccionado = "";
    SQLiteHelper sqLiteHelper;
    boolean modoOscuro = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil);

        tvNombre = findViewById(R.id.tvNombrePerfil);
        tvEdad = findViewById(R.id.tvEdadPerfil);
        tvColor = findViewById(R.id.tvColorPerfil);
        tvAvatarLetra = findViewById(R.id.tvAvatarLetra);

        viewBlue = findViewById(R.id.viewColorBlue);
        viewGreen = findViewById(R.id.viewColorGreen);
        viewYellow = findViewById(R.id.viewColorYellow);
        btnGuardar = findViewById(R.id.btnGuardarPerfil);
        btnBack = findViewById(R.id.btnBackPerfil);

        sqLiteHelper = new SQLiteHelper(this);
        modoOscuro = getIntent().getBooleanExtra("modoOscuro", false);
        cargarPerfil();
        aplicarModoOscuro();

        btnBack.setOnClickListener(v -> finish());

        // Configurar selección de colores
        viewBlue.setOnClickListener(v -> seleccionarColor("Azul", viewBlue));
        viewGreen.setOnClickListener(v -> seleccionarColor("Verde", viewGreen));
        viewYellow.setOnClickListener(v -> seleccionarColor("Amarillo", viewYellow));

        btnGuardar.setOnClickListener(v -> {
            if (!colorSeleccionado.isEmpty()) {
                sqLiteHelper.actualizarColorPerfil(colorSeleccionado);
                Toast.makeText(this, getString(R.string.profile_update_success), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.profile_select_color_warning), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void seleccionarColor(String nombreColor, View view) {
        colorSeleccionado = nombreColor;
        // Reiniciar elevación y escala
        viewBlue.setScaleX(1f); viewBlue.setScaleY(1f); viewBlue.setElevation(2f);
        viewGreen.setScaleX(1f); viewGreen.setScaleY(1f); viewGreen.setElevation(2f);
        viewYellow.setScaleX(1f); viewYellow.setScaleY(1f); viewYellow.setElevation(2f);

        // Resaltar seleccionado
        view.setScaleX(1.15f);
        view.setScaleY(1.15f);
        view.setElevation(dpToPx(4));
        
        tvColor.setText(String.format(getString(R.string.profile_color_selected_hint), nombreColor));
    }

    private float dpToPx(int dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private void aplicarModoOscuro() {
        if (modoOscuro) {
            findViewById(R.id.main_perfil).setBackgroundColor(getColor(R.color.dark_background));
            tvNombre.setTextColor(getColor(R.color.dark_text));
            tvEdad.setTextColor(getColor(R.color.dark_text));
            tvColor.setTextColor(getColor(R.color.dark_text));
            ((TextView)findViewById(R.id.tvTituloPerfil)).setTextColor(getColor(R.color.dark_text));
            ((TextView)findViewById(R.id.tvEligeColor)).setTextColor(getColor(R.color.dark_text));
        }
    }

    private void cargarPerfil(){
        Profile profile = sqLiteHelper.obtenerPerfil();
        if(profile != null){
            tvNombre.setText(profile.getNombre());
            tvEdad.setText(profile.getEdad() + " años");
            if (!profile.getNombre().isEmpty()) {
                tvAvatarLetra.setText(String.valueOf(profile.getNombre().charAt(0)).toUpperCase());
            }
            colorSeleccionado = profile.getColor();
            tvColor.setText(String.format(getString(R.string.profile_color_selected_hint), colorSeleccionado));
            
            // Marcar el color actual en la UI
            if(colorSeleccionado.equals("Azul")) seleccionarColor("Azul", viewBlue);
            else if(colorSeleccionado.equals("Verde")) seleccionarColor("Verde", viewGreen);
            else if(colorSeleccionado.equals("Amarillo")) seleccionarColor("Amarillo", viewYellow);
        }else {
            Toast.makeText(this, "Perfil no encontrado", Toast.LENGTH_LONG).show();
        }
    }
}