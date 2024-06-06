package com.hypenet.realestaterehman.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Choice {

    @SerializedName("finish_reason")
    @Expose
    private String finishReason;
    @SerializedName("index")
    @Expose
    private Integer index;
    @SerializedName("message")
    @Expose
    private Chat message;

    public String getFinishReason() {
        return finishReason;
    }

    public void setFinishReason(String finishReason) {
        this.finishReason = finishReason;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Chat getMessage() {
        return message;
    }

    public void setMessage(Chat message) {
        this.message = message;
    }

}
