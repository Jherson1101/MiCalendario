package com.example.micalendario.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.micalendario.R;
import com.example.micalendario.database.SQLiteHelper;

import com.google.android.material.textfield.TextInputLayout;

import android.view.WindowManager;


public class LoginActivity extends AppCompatActivity {
    EditText etUsuario, etPassword;
    TextInputLayout tilUsuario, tilPassword;
    Button btnLogin;
    SQLiteHelper sqLiteHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // evita capturas de pantalla y grabacion para mayor seguridad de datos
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etUsuario = findViewById(R.id.etUsuario);
        etPassword = findViewById(R.id.etPassword);
        tilUsuario = findViewById(R.id.tilUsuario);
        tilPassword = findViewById(R.id.tilPassword);
        btnLogin = findViewById(R.id.btnLogin);
        sqLiteHelper = new SQLiteHelper(this);

        // roles de usuario (niño con tea, cuidador, terapeuta)
        if(!sqLiteHelper.existeUsuario("admin")){

            sqLiteHelper.insertarUsuario(
                    "admin",
                    "admin123",
                    "cuidador"
            );

            sqLiteHelper.insertarUsuario(
                    "nino",
                    "123456",
                    "nino"
            );

            sqLiteHelper.insertarUsuario(
                    "terapeuta",
                    "123456",
                    "terapeuta"
            );
        }

        if(!sqLiteHelper.existePerfil()){

            sqLiteHelper.insertarPerfil(
                    "Juan Pérez",
                    8,
                    "Azul"
            );
        }

        btnLogin.setOnClickListener(v -> {
            String usuario = etUsuario.getText()
                            .toString()
                            .trim();

            String password = etPassword.getText()
                            .toString()
                            .trim();

            // Limpiar errores previos
            tilUsuario.setError(null);
            tilPassword.setError(null);

            // condicionales para el acceso (login)
            if(usuario.isBlank()){
                tilUsuario.setError(getString(R.string.error_usuario_vacio));
                return;
            }
            if(password.isBlank()){
                tilPassword.setError(getString(R.string.error_password_vacio));
                return;
            }
            if(password.length() < 6){
                tilPassword.setError(getString(R.string.error_password_corto));
                return;
            }
            boolean tieneNumero = false;
            for(char c : password.toCharArray()){
                if(Character.isDigit(c)){
                    tieneNumero = true;
                    break;
                }
            }
            if(!tieneNumero){
                tilPassword.setError(getString(R.string.error_password_numero));
                return;
            }


            boolean existe = sqLiteHelper.validarUsuario(usuario, password);
            String rol = sqLiteHelper.obtenerRolUsuario(usuario);
            if(existe){
                Toast.makeText(
                        this,
                        "Login correcto",
                        Toast.LENGTH_SHORT
                ).show();
                // Guardar rol en SharedPreferences para el Widget
                getSharedPreferences("MiCalendarioPrefs", MODE_PRIVATE)
                        .edit()
                        .putString("rol_actual", rol)
                        .apply();

                // Notificar al widget para que se actualice
                Intent updateWidgetIntent = new Intent(this, com.example.micalendario.widgets.NextTaskWidget.class);
                updateWidgetIntent.setAction(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                int[] ids = android.appwidget.AppWidgetManager.getInstance(getApplication())
                        .getAppWidgetIds(new android.content.ComponentName(getApplication(), com.example.micalendario.widgets.NextTaskWidget.class));
                updateWidgetIntent.putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                sendBroadcast(updateWidgetIntent);

                Intent intent = new Intent(
                                LoginActivity.this,
                                MainActivity.class);
                intent.putExtra("rol", rol);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(
                        this,
                        "Usuario o contraseña incorrectos",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}