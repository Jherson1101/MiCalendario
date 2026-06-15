package com.example.micalendario.models;

public class Task {

    private int id;
    private String titulo;
    private String descripcion;
    private String fecha;
    private String hora;
    private String periodo;
    private String pictograma;
    private int completada;
    private int perfilId;

    public Task(int id, String titulo, String descripcion, String fecha, String hora,
                String periodo, String pictograma, int completada, int perfilId) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.hora = hora;
        this.periodo = periodo;
        this.pictograma = pictograma;
        this.completada = completada;
        this.perfilId = perfilId;
    }
    public int getId() {return id;}
    public String getTitulo() {return titulo;}
    public String getDescripcion() {return descripcion;}
    public String getFecha() {return fecha;}
    public String getHora() {return hora;}
    public String getPeriodo() {return periodo;}
    public String getPictograma() {return pictograma;}
    public int getCompletada() {return completada;}
    public int getPerfilId() {return perfilId;}

    public void setId(int id) {this.id = id;}
    public void setTitulo(String titulo) {this.titulo = titulo;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public void setFecha(String fecha) {this.fecha = fecha;}
    public void setHora(String hora) {this.hora = hora;}
    public void setPeriodo(String periodo) {this.periodo = periodo;}
    public void setPictograma(String pictograma) {this.pictograma = pictograma;}
    public void setCompletada(int completada) {this.completada = completada;}
    public void setPerfilId(int perfilId) {this.perfilId = perfilId;}
}
