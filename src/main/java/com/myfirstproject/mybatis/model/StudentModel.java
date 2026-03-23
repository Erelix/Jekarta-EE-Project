package com.myfirstproject.mybatis.model;

import java.util.ArrayList;
import java.util.List;

public class StudentModel {
    
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Long groupId;
    
    private GroupModel group;
    private List<SubjectModel> subjects = new ArrayList<>();
    
    public StudentModel() {
    }
    
    public StudentModel(String firstName, String lastName, String email, Long groupId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.groupId = groupId;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Long getGroupId() {
        return groupId;
    }
    
    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }
    
    public GroupModel getGroup() {
        return group;
    }
    
    public void setGroup(GroupModel group) {
        this.group = group;
    }
    
    public List<SubjectModel> getSubjects() {
        return subjects;
    }
    
    public void setSubjects(List<SubjectModel> subjects) {
        this.subjects = subjects;
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
