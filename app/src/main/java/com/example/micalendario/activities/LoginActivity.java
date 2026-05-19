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

public class LoginActivity extends AppCompatActivity {
    EditText etUsuario, etPassword;
    Button btnLogin;

    SQLiteHelper sqLiteHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etUsuario = findViewById(R.id.etUsuario);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        sqLiteHelper = new SQLiteHelper(this);
        sqLiteHelper.insertarUsuario("admin", "1234");

        btnLogin.setOnClickListener(v -> {

            String usuario = etUsuario.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if(usuario.isEmpty() || password.isEmpty()){

                Toast.makeText(this,
                        "Complete todos los campos",
                        Toast.LENGTH_SHORT).show();

            } else {

                boolean existe = sqLiteHelper.validarUsuario(usuario, password);

                if(existe){

                    Toast.makeText(this,
                            "Login correcto",
                            Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this,
                            MainActivity.class);

                    startActivity(intent);

                    finish();

                } else {

                    Toast.makeText(this,
                            "Usuario o contraseña incorrectos",
                            Toast.LENGTH_SHORT).show();

                }

            }

        });
    }
}