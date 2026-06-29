package com.example.micalendario.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.micalendario.R;
import com.example.micalendario.adapters.PictogramAdapter;
import com.example.micalendario.database.SQLiteHelper;
import com.example.micalendario.models.Pictogram;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ManagePictogramsActivity extends AppCompatActivity {

    private RecyclerView rvCustomPictograms;
    private MaterialButton btnAddPictogram;
    private ImageButton btnBack;
    private SQLiteHelper sqLiteHelper;
    private List<Pictogram> customPictograms;
    private PictogramAdapter adapter;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    mostrarDialogoNombrePictograma(imageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_pictograms);

        sqLiteHelper = new SQLiteHelper(this);
        rvCustomPictograms = findViewById(R.id.rvCustomPictograms);
        btnAddPictogram = findViewById(R.id.btnAddPictogram);
        btnBack = findViewById(R.id.btnBackManage);

        btnBack.setOnClickListener(v -> finish());
        btnAddPictogram.setOnClickListener(v -> abrirGaleria());

        cargarPictogramas();
    }

    private void cargarPictogramas() {
        customPictograms = sqLiteHelper.obtenerPictogramasPersonalizados();
        adapter = new PictogramAdapter(customPictograms, "", pictogram -> {
            // Opcional: permitir eliminar o editar
        });
        rvCustomPictograms.setAdapter(adapter);
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void mostrarDialogoNombrePictograma(Uri imageUri) {
        EditText etNombre = new EditText(this);
        etNombre.setHint("Ej: Pilates, Natación...");

        new MaterialAlertDialogBuilder(this)
                .setTitle("Nombre del Pictograma")
                .setMessage("Ingresa un nombre descriptivo para la imagen.")
                .setView(etNombre)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    if (!nombre.isEmpty()) {
                        guardarPictograma(imageUri, nombre);
                    } else {
                        Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarPictograma(Uri uri, String nombre) {
        try {
            // Guardar imagen en almacenamiento interno
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            
            String fileName = "custom_picto_" + System.currentTimeMillis() + ".png";
            File file = new File(getFilesDir(), fileName);
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            // Guardar en DB
            sqLiteHelper.insertarPictogramaPersonalizado(nombre, file.getAbsolutePath());
            
            cargarPictogramas();
            Toast.makeText(this, "Pictograma agregado con éxito", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show();
        }
    }
}
