package com.example.birdwatcher.audio;

import com.google.gson.annotations.SerializedName;

public class AudioResponseModel {

    @SerializedName("Common name")
    private String commonName;

    @SerializedName("Confidence")
    private float confidence;

    @SerializedName("End (s)")
    private float end;

    @SerializedName("Scientific name")
    private String scientificName;

    @SerializedName("Start (s)")
    private float start;


    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public float getEnd() {
        return end;
    }

    public void setEnd(float end) {
        this.end = end;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public float getStart() {
        return start;
    }

    public void setStart(float start) {
        this.start = start;
    }
}

