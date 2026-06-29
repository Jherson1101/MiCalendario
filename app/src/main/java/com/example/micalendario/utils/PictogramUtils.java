package com.example.micalendario.utils;

import android.content.Context;
import com.example.micalendario.R;
import com.example.micalendario.database.SQLiteHelper;
import com.example.micalendario.models.Pictogram;
import java.util.ArrayList;
import java.util.List;

public class PictogramUtils {
    public static List<Pictogram> getPictograms(Context context) {
        List<Pictogram> pictograms = new ArrayList<>();
        // Pictogramas predeterminados
        pictograms.add(new Pictogram("desayuno", R.drawable.desayuno, R.string.picto_desayuno));
        pictograms.add(new Pictogram("lavar_dientes", R.drawable.lavar_dientes, R.string.picto_lavar_dientes));
        pictograms.add(new Pictogram("lavar_manos", R.drawable.lavar_manos, R.string.picto_lavar_manos));
        pictograms.add(new Pictogram("vestirse", R.drawable.vestirse, R.string.picto_vestirse));
        pictograms.add(new Pictogram("almuerzo", R.drawable.almuerzo, R.string.picto_almuerzo));
        pictograms.add(new Pictogram("merienda", R.drawable.merienda, R.string.picto_merienda));
        pictograms.add(new Pictogram("cena", R.drawable.cena, R.string.picto_cena));
        pictograms.add(new Pictogram("colegio", R.drawable.colegio, R.string.picto_colegio));
        pictograms.add(new Pictogram("estudiar", R.drawable.estudiar, R.string.picto_estudiar));
        pictograms.add(new Pictogram("tareas", R.drawable.tareas, R.string.picto_tareas));
        pictograms.add(new Pictogram("terapia", R.drawable.terapia, R.string.picto_terapia));
        pictograms.add(new Pictogram("medicina", R.drawable.medicina, R.string.picto_medicina));
        pictograms.add(new Pictogram("ejercicio", R.drawable.ejercicio, R.string.picto_ejercicio));
        pictograms.add(new Pictogram("higiene", R.drawable.higiene, R.string.picto_higiene));
        pictograms.add(new Pictogram("ducha", R.drawable.ducha, R.string.picto_ducha));
        pictograms.add(new Pictogram("parque", R.drawable.parque, R.string.picto_parque));
        pictograms.add(new Pictogram("jugar", R.drawable.jugar, R.string.picto_jugar));
        pictograms.add(new Pictogram("juegos", R.drawable.juegos, R.string.picto_juegos));
        pictograms.add(new Pictogram("ocio", R.drawable.ocio, R.string.picto_ocio));
        pictograms.add(new Pictogram("lectura", R.drawable.lectura, R.string.picto_lectura));
        pictograms.add(new Pictogram("ver_tele", R.drawable.ver_tele, R.string.picto_ver_tele));
        pictograms.add(new Pictogram("dormir", R.drawable.dormir, R.string.picto_dormir));

        // Cargar pictogramas personalizados desde la DB
        SQLiteHelper dbHelper = new SQLiteHelper(context);
        pictograms.addAll(dbHelper.obtenerPictogramasPersonalizados());

        return pictograms;
    }
}
