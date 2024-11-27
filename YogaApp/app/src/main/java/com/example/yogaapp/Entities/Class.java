package com.example.yogaapp.Entities;

public class Class {
    private int id;
    private String date;
    private int courseId; // Reference to Course ID
    private String teacher;
    private String comments;

    public Class(int id, String date, int courseId, String teacher, String comments) {
        this.id = id;
        this.date = date;
        this.courseId = courseId;
        this.teacher = teacher;
        this.comments = comments;
    }

    public Class(){

    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comment) {
        this.comments = comment;
    }
}
