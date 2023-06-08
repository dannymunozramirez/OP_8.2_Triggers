package com.ibm.openpages.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an observation of the USD/CAD exchange rate at a specific date.
 * Each observation includes the date (d) and the exchange rate (FXUSDCAD).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Observation {
    private String d;

    @JsonProperty("FXUSDCAD")
    private FXUSDCAD fxusdcad;

    private String url;

    // Getters and setters

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public FXUSDCAD getFxusdcad() {
        return fxusdcad;
    }

    public void setFxusdcad(FXUSDCAD fxusdcad) {
        this.fxusdcad = fxusdcad;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
