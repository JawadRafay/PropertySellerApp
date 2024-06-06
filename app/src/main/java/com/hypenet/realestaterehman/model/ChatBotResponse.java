package com.hypenet.realestaterehman.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ChatBotResponse {

    @SerializedName("choices")
    @Expose
    private List<Choice> choices;
    @SerializedName("created")
    @Expose
    private long created;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("error")
    @Expose
    private Error error;

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }
}
