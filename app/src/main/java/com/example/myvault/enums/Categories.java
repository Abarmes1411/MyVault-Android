package com.example.myvault.enums;

public enum Categories {
    PELICULAS("cat_1"),
    SERIES("cat_2"),
    VIDEOJUEGOS("cat_3"),
    ANIME("cat_4"),
    MANGAS("cat_5"),
    NOVELAS_LIGERAS("cat_6");

    private final String id;

    Categories(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}

