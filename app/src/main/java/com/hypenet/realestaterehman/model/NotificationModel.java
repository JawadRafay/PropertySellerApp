package com.hypenet.realestaterehman.model;

public class NotificationModel {

    private String priority,to;
    private NotificationData notification;

    public NotificationModel(String priority, String to, String title, String body) {
        this.priority = priority;
        this.to = to;
        this.notification = new NotificationData(title,body);
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public NotificationData getNotification() {
        return notification;
    }

    public void setNotification(NotificationData notification) {
        this.notification = notification;
    }
}

class NotificationData{
    private String title,body;
    private int badge;

    public NotificationData(String title, String body) {
        this.title = title;
        this.body = body;
        this.badge = 1;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getBadge() {
        return badge;
    }

    public void setBadge(int badge) {
        this.badge = badge;
    }
}
