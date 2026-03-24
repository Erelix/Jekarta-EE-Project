package com.myfirstproject.mybatis.mapper;

import java.util.List;

import com.myfirstproject.mybatis.model.SubjectModel;

public interface SubjectMapper {
    
    SubjectModel findById(Long id);
    
    List<SubjectModel> findAll();
    
    SubjectModel findByIdWithStudents(Long id);
    
    List<SubjectModel> findByStudentId(Long studentId);
    
    void insert(SubjectModel subject);
    
    void update(SubjectModel subject);
    
    void delete(Long id);
}
