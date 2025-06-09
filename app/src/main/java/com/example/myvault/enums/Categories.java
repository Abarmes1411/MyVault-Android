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

    @Override
    public String toString() {
        String name = this.name();

        String[] parts = name.split("_");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].toLowerCase();
            part = part.substring(0, 1).toUpperCase() + part.substring(1);
            result.append(part);
            if (i < parts.length - 1) {
                result.append(" ");
            }
        }

        return result.toString();
    }
}

