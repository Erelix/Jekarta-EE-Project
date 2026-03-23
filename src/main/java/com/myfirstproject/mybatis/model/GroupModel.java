package com.myfirstproject.mybatis.model;

import java.util.ArrayList;
import java.util.List;

public class GroupModel {
    
    private Long id;
    private String name;
    private String description;
    
    private List<StudentModel> students = new ArrayList<>();
    
    public GroupModel() {
    }
    
    public GroupModel(String name, String description) {
        this.name = name;
        this.description = description;
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
    
    public List<StudentModel> getStudents() {
        return students;
    }
    
    public void setStudents(List<StudentModel> students) {
        this.students = students;
    }
}
