package com.example.micalendario.models;

public class Pictogram {
    private String name;
    private int imageResId;
    private int labelResId;
    private String imagePath; // Para pictogramas personalizados

    public Pictogram(String name, int imageResId, int labelResId) {
        this.name = name;
        this.imageResId = imageResId;
        this.labelResId = labelResId;
        this.imagePath = null;
    }

    public Pictogram(String name, String imagePath) {
        this.name = name;
        this.imageResId = 0;
        this.labelResId = 0;
        this.imagePath = imagePath;
    }

    public String getName() {
        return name;
    }

    public int getImageResId() {
        return imageResId;
    }

    public int getLabelResId() {
        return labelResId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public boolean isCustom() {
        return imagePath != null;
    }
}
