package com.myfirstproject.mybatis.mapper;

import java.util.List;

import com.myfirstproject.mybatis.model.StudentModel;

public interface StudentMapper {
    
    StudentModel findById(Long id);
    
    List<StudentModel> findAll();
    
    List<StudentModel> findByGroupId(Long groupId);
    
    void insert(StudentModel student);
    
    void update(StudentModel student);
    
    void delete(Long id);
    
    void enrollInSubject(Long studentId, Long subjectId);
    
    void unenrollFromSubject(Long studentId, Long subjectId);
}
