package com.toosame.weather.model;

import java.util.List;

/**
 *
 */

public class DistrictsRoot {
    private List<Districts> districts ;

    public void setDistricts(List<Districts> districts){
        this.districts = districts;
    }

    public List<Districts> getDistricts(){
        return this.districts;
    }
}
