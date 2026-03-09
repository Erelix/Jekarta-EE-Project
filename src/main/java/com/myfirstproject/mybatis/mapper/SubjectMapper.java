package com.myfirstproject.mybatis.mapper;

import java.util.List;

import com.myfirstproject.mybatis.model.SubjectModel;

/**
 * MyBatis Mapper interface for Subject operations.
 */
public interface SubjectMapper {
    
    /**
     * Find subject by ID.
     */
    SubjectModel findById(Long id);
    
    /**
     * Find all subjects.
     */
    List<SubjectModel> findAll();
    
    /**
     * Find subject with enrolled students (demonstrates many-to-many).
     */
    SubjectModel findByIdWithStudents(Long id);
    
    /**
     * Find subjects for a specific student (many-to-many).
     */
    List<SubjectModel> findByStudentId(Long studentId);
    
    /**
     * Insert new subject.
     */
    void insert(SubjectModel subject);
    
    /**
     * Update existing subject.
     */
    void update(SubjectModel subject);
    
    /**
     * Delete subject by ID.
     */
    void delete(Long id);
}
