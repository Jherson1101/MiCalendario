package com.example.micalendario.models;

public class Profile {
    private int id;
    private String nombre;
    private int edad;
    private String color;

    // constructor
    public Profile(int id,String nombre, int edad, String color){
        this.id=id;
        this.nombre=nombre;
        this.edad=edad;
        this.color=color;
    }
    // getters y setters

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}
    public String getNombre() {return nombre;}
    public void setNombre(String nombre) {this.nombre = nombre;}
    public int getEdad() {return edad;}
    public void setEdad(int edad) {this.edad = edad;}
    public String getColor() {return color;}
    public void setColor(String color) {this.color = color;}
}
