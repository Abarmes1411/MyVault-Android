package com.example.myvault.models;

import java.io.Serializable;

public class ContentCategory extends DomainEntity implements Serializable {

    private String name;
    private String image;

    public ContentCategory() {
    }

    public ContentCategory(String name, String image) {
        super();
        this.name = name;
        this.image = image;
    }




    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
