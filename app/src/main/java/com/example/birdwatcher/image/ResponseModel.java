package com.example.birdwatcher.image;

import com.google.gson.annotations.SerializedName;

public class ResponseModel {
    @SerializedName("class")
    private String className;

    @SerializedName("scientific_class")
    private String scientificClassName;

    @SerializedName("probability")
    private double probability;

    // getters and setters
//    public ResponseModel(String className, String scientificClassName, double probability){
//        this.className = className;
//        this.scientificClassName = scientificClassName;
//        this.probability = probability;
//    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getScientificClassName() {
        return scientificClassName;
    }

    public void setScientificClassName(String scientificClassName) {
        this.scientificClassName = scientificClassName;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }
}

