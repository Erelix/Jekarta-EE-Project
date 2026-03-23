package com.myfirstproject.mybatis.model;

import java.util.ArrayList;
import java.util.List;

public class SubjectModel {
    
    private Long id;
    private String name;
    private String description;
    private Integer credits;
    
    private List<StudentModel> students = new ArrayList<>();
    
    public SubjectModel() {
    }
    
    public SubjectModel(String name, String description, Integer credits) {
        this.name = name;
        this.description = description;
        this.credits = credits;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getCredits() {
        return credits;
    }
    
    public void setCredits(Integer credits) {
        this.credits = credits;
    }
    
    public List<StudentModel> getStudents() {
        return students;
    }
    
    public void setStudents(List<StudentModel> students) {
        this.students = students;
    }
}
