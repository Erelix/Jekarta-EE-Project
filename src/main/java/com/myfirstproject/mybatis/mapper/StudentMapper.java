package com.myfirstproject.mybatis.mapper;

import java.util.List;

import com.myfirstproject.mybatis.model.StudentModel;

/**
 * MyBatis Mapper interface for Student operations.
 * Methods are implemented by MyBatis using XML mapping files.
 */
public interface StudentMapper {
    
    /**
     * Find student by ID with associated group and subjects (one-to-many & many-to-many).
     */
    StudentModel findById(Long id);
    
    /**
     * Find all students with their groups.
     */
    List<StudentModel> findAll();
    
    /**
     * Find students by group ID (demonstrates one-to-many).
     */
    List<StudentModel> findByGroupId(Long groupId);
    
    /**
     * Insert new student.
     */
    void insert(StudentModel student);
    
    /**
     * Update existing student.
     */
    void update(StudentModel student);
    
    /**
     * Delete student by ID.
     */
    void delete(Long id);
    
    /**
     * Enroll student in subject (many-to-many).
     */
    void enrollInSubject(Long studentId, Long subjectId);
    
    /**
     * Unenroll student from subject (many-to-many).
     */
    void unenrollFromSubject(Long studentId, Long subjectId);
}
