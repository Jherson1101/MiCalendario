package com.example.micalendario.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.content.ContentValues;
import android.database.Cursor;
import com.example.micalendario.models.Task;
import java.util.ArrayList;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "MiCalendario.db";
    private static final int DATABASE_VERSION = 1;

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "usuario TEXT, " +
                "password TEXT)";

        db.execSQL(createUsersTable);

        String createProfilesTable = "CREATE TABLE perfiles (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT, " +
                "edad INTEGER, " +
                "color TEXT)";

        db.execSQL(createProfilesTable);

        String createTasksTable = "CREATE TABLE actividades (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "titulo TEXT, " +
                "descripcion TEXT, " +
                "fecha TEXT, " +
                "hora TEXT, " +
                "periodo TEXT, " +
                "pictograma TEXT, " +
                "completada INTEGER, " +
                "perfil_id INTEGER)";

        db.execSQL(createTasksTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertarUsuario(String usuario, String password){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("usuario", usuario);
        values.put("password", password);

        db.insert("usuarios", null, values);

        db.close();
    }

    public boolean validarUsuario(String usuario, String password){

        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM usuarios WHERE usuario=? AND password=?";

        Cursor cursor = db.rawQuery(query, new String[]{usuario, password});

        boolean existe = cursor.getCount() > 0;

        cursor.close();
        db.close();

        return existe;
    }

    public void insertarTask(Task task){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("titulo", task.getTitulo());
        values.put("descripcion", task.getDescripcion());
        values.put("fecha", task.getFecha());
        values.put("hora", task.getHora());
        values.put("periodo", task.getPeriodo());
        values.put("pictograma", task.getPictograma());
        values.put("completada", task.getCompletada());
        values.put("perfil_id", task.getPerfilId());

        db.insert("actividades", null, values);

        db.close();
    }

    public ArrayList<Task> obtenerTasks(){

        ArrayList<Task> listaTasks = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM actividades";
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.moveToFirst()){
            do {
                Task task = new Task(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getInt(7),
                        cursor.getInt(8)
                );
                listaTasks.add(task);
            } while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return listaTasks;
    }

    public void eliminarTask(int id){

        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(
                "actividades",
                "id=?",
                new String[]{String.valueOf(id)}
        );

        db.close();
    }

    public void actualizarTask(Task task){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("titulo", task.getTitulo());
        values.put("descripcion", task.getDescripcion());
        values.put("hora", task.getHora());
        values.put("periodo", task.getPeriodo());

        db.update(
                "actividades",
                values,
                "id=?",
                new String[]{String.valueOf(task.getId())}
        );

        db.close();
    }
}
