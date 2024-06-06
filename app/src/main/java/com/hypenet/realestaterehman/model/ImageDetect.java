package com.hypenet.realestaterehman.model;

public class ImageDetect {

    String labelName;
    double confidence;

    public ImageDetect(String labelName, double confidence) {
        this.labelName = labelName;
        this.confidence = confidence;
    }

    public String getLabelName() {
        return labelName;
    }

    public void setLabelName(String labelName) {
        this.labelName = labelName;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}
