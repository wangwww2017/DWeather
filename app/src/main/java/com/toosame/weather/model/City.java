package com.toosame.weather.model;

import java.util.List;

/**
 *
 */

public class City {
    public City(String name,int id){
        this.name = name;
        this.id = id;
    }

    private String name;
    private int id;
    private List<DisCity> districts ;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
