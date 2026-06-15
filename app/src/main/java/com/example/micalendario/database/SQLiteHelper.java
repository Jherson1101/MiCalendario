package com.example.micalendario.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import com.example.micalendario.models.Task;
import com.example.micalendario.models.Profile;
import java.util.ArrayList;
import java.util.List;

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
                "password TEXT, " +
                "rol TEXT)";
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
        db.execSQL("DROP TABLE IF EXISTS usuarios");
        db.execSQL("DROP TABLE IF EXISTS perfiles");
        db.execSQL("DROP TABLE IF EXISTS actividades");
        onCreate(db);
    }

    public void insertarUsuario(String usuario, String password, String rol){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("usuario", usuario);
        values.put("password", password);
        values.put("rol", rol);
        db.insert("usuarios", null, values);
        db.close();
    }

    public void insertarPerfil(String nombre, int edad, String color){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("edad", edad);
        values.put("color", color);
        db.insert("perfiles", null, values);
        db.close();
    }

    public boolean validarUsuario(String usuario, String password){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM usuarios WHERE usuario=? AND password=?", new String[]{usuario, password});
        boolean existe = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return existe;
    }

    public String obtenerRolUsuario(String usuario){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT rol FROM usuarios WHERE usuario=?", new String[]{usuario});
        String rol = "";
        if(cursor.moveToFirst()){
            rol = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return rol;
    }

    public Profile obtenerPerfil(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM perfiles LIMIT 1", null);
        Profile profile = null;
        if(cursor.moveToFirst()){
            profile = new Profile(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getInt(2),
                    cursor.getString(3)
            );
        }
        cursor.close();
        db.close();
        return profile;
    }

    public boolean existeUsuario(String usuario){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM usuarios WHERE usuario=?", new String[]{usuario});
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
        Cursor cursor = db.rawQuery("SELECT * FROM actividades", null);
        if(cursor.moveToFirst()){
            do {
                listaTasks.add(new Task(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getInt(7),
                        cursor.getInt(8)
                ));
            } while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return listaTasks;
    }

    public List<Task> obtenerTasksPorFecha(String fecha){
        List<Task> listaTasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM actividades WHERE fecha=?", new String[]{fecha});
        if(cursor.moveToFirst()){
            do{
                listaTasks.add(new Task(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getInt(7),
                        cursor.getInt(8)
                ));
            } while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return listaTasks;
    }

    public List<Task> obtenerTasksPorRango(String fechaInicio, String fechaFin) {
        List<Task> listaTasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM actividades WHERE fecha BETWEEN ? AND ?",
                new String[]{fechaInicio, fechaFin}
        );
        if (cursor.moveToFirst()) {
            do {
                listaTasks.add(new Task(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getInt(7),
                        cursor.getInt(8)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return listaTasks;
    }

    public void eliminarTask(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("actividades", "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void actualizarEstadoTask(int id, int completada){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("completada", completada);
        db.update("actividades", values, "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void actualizarTask(Task task){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("titulo", task.getTitulo());
        values.put("descripcion", task.getDescripcion());
        values.put("hora", task.getHora());
        values.put("periodo", task.getPeriodo());
        values.put("pictograma", task.getPictograma());
        db.update("actividades", values, "id=?", new String[]{String.valueOf(task.getId())});
        db.close();
    }

    public boolean existePerfil(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM perfiles", null);
        boolean existe = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return existe;
    }

    public void actualizarColorPerfil(String color){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("color", color);
        db.update("perfiles", values, null, null);
        db.close();
    }
}