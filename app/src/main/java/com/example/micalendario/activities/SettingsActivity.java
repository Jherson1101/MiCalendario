package com.example.micalendario.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import com.example.micalendario.R;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.micalendario.database.SQLiteHelper;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private MaterialSwitch switchDarkMode;
    private View layoutPassword, layoutColors, layoutStorage;
    private android.widget.Button btnLogout;
    private boolean modoOscuro;
    private SQLiteHelper sqLiteHelper;
    private String currentUser = "admin"; // En un caso real, obtendríamos esto de una sesión
    private String rol = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sqLiteHelper = new SQLiteHelper(this);
        rol = getIntent().getStringExtra("rol");
        if (rol == null) rol = "nino";

        btnBack = findViewById(R.id.btnBackSettings);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        layoutPassword = findViewById(R.id.layoutPassword);
        layoutColors = findViewById(R.id.layoutColors);
        layoutStorage = findViewById(R.id.layoutStorage);
        btnLogout = findViewById(R.id.btnLogout);

        // Cargar estado actual del modo oscuro
        SharedPreferences prefs = getSharedPreferences("Configuracion", MODE_PRIVATE);
        modoOscuro = prefs.getBoolean("modoOscuro", false);
        switchDarkMode.setChecked(modoOscuro);

        btnBack.setOnClickListener(v -> finish());

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = getSharedPreferences("Configuracion", MODE_PRIVATE).edit();
            editor.putBoolean("modoOscuro", isChecked);
            editor.apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        layoutPassword.setOnClickListener(v -> mostrarDialogoCambiarPassword());
        
        layoutColors.setOnClickListener(v -> {
            if (rol.equals("cuidador") || rol.equals("terapeuta")) {
                android.content.Intent intent = new android.content.Intent(this, ManagePictogramsActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Acceso restringido: Solo cuidadores o terapeutas pueden gestionar pictogramas", Toast.LENGTH_SHORT).show();
            }
        });

        layoutStorage.setOnClickListener(v -> mostrarDialogoAlmacenamiento());

        btnLogout.setOnClickListener(v -> mostrarConfirmacionLogout());

        // Aplicar modo oscuro a la interfaz de settings si es necesario
        if (modoOscuro) {
            aplicarModoOscuro();
        }
    }

    private void mostrarDialogoAlmacenamiento() {
        int totalTasks = sqLiteHelper.obtenerTasks().size();
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.settings_storage)
                .setMessage("Actualmente tienes " + totalTasks + " actividades guardadas en la base de datos.")
                .setPositiveButton("Cerrar", null)
                .setNeutralButton("Limpiar Todo", (dialog, which) -> {
                    // Aquí podrías implementar una limpieza de base de datos si fuera necesario
                    Toast.makeText(this, "Función de limpieza en desarrollo", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void mostrarDialogoCambiarPassword() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        
        TextInputEditText etCurrent = view.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNew = view.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirm = view.findViewById(R.id.etConfirmPassword);
        TextInputLayout tilCurrent = view.findViewById(R.id.tilCurrentPassword);
        TextInputLayout tilNew = view.findViewById(R.id.tilNewPassword);
        TextInputLayout tilConfirm = view.findViewById(R.id.tilConfirmPassword);

        new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setPositiveButton("Cambiar", (dialog, which) -> {
                    String current = etCurrent.getText().toString();
                    String newPass = etNew.getText().toString();
                    String confirm = etConfirm.getText().toString();

                    if (sqLiteHelper.validarUsuario(currentUser, current)) {
                        if (newPass.equals(confirm)) {
                            sqLiteHelper.actualizarPassword(currentUser, newPass);
                            Toast.makeText(this, R.string.settings_password_changed_success, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, R.string.settings_passwords_dont_match, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, R.string.settings_password_incorrect, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarConfirmacionLogout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.settings_logout)
                .setMessage(R.string.settings_logout_confirm)
                .setPositiveButton(R.string.settings_logout_yes, (dialog, which) -> {
                    // Navegar a LoginActivity y limpiar el stack de actividades
                    android.content.Intent intent = new android.content.Intent(this, LoginActivity.class);
                    intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(R.string.settings_logout_no, null)
                .show();
    }

    private void aplicarModoOscuro() {
        // Ya no es necesario el cambio manual de colores.
        // El sistema lo maneja mediante AppCompatDelegate y recursos DayNight.
    }
}
