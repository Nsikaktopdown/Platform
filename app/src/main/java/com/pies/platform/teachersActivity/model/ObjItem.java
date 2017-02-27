package com.pies.platform.teachersActivity.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nsikak  Thompson on 9/5/2016.
 */
public class ObjItem {
    public String subject;
    public String topic;
    public String objective;
    public String sent_time;
    public String author;
    public String day;


    public ObjItem(){
        
    }

    public  String date;

    public ObjItem(String subject, String topic, String date, String objective, String sent_time,String day, String author) {
        this.subject = subject;
        this.topic = topic;
        this.objective = objective;
        this.sent_time = sent_time;
        this.author = author;
        this.date = date;
        this.day = day;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }



    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public String getSent_time() {
        return sent_time;
    }

    public void setSent_time(String sent_time) {
        this.sent_time = sent_time;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
    // [START post_to_map]
    @Exclude
    public Map<String, Object> toObj() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("subject", subject);
        result.put("topic", topic);
        result.put("objective", objective);
        result.put("sent_time", sent_time);
        result.put("date", date);
        result.put("author", author);
        result.put("day", day);





        return result;
    }
}
