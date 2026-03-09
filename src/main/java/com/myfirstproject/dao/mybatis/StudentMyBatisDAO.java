package com.myfirstproject.dao.mybatis;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.myfirstproject.mybatis.mapper.StudentMapper;
import com.myfirstproject.mybatis.model.StudentModel;
import com.myfirstproject.mybatis.util.MyBatisUtil;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * MyBatis Data Access Object for Student.
 * Uses MyBatis SqlSession and mapper interfaces for database operations.
 * 
 * Key differences from JPA:
 * - Manual session management (open/close)
 * - Manual transaction control (commit/rollback)
 * - Direct SQL control via XML mappers
 * - POJOs instead of managed entities
 */
@ApplicationScoped
public class StudentMyBatisDAO {
    
    /**
     * Find student by ID with relationships.
     */
    public StudentModel findById(Long id) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            StudentMapper mapper = session.getMapper(StudentMapper.class);
            return mapper.findById(id);
        }
    }
    
    /**
     * Find all students.
     */
    public List<StudentModel> findAll() {
        try (SqlSession session = MyBatisUtil.openSession()) {
            StudentMapper mapper = session.getMapper(StudentMapper.class);
            return mapper.findAll();
        }
    }
    
    /**
     * Find students by group ID (demonstrates one-to-many).
     */
    public List<StudentModel> findByGroupId(Long groupId) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            StudentMapper mapper = session.getMapper(StudentMapper.class);
            return mapper.findByGroupId(groupId);
        }
    }
    
    /**
     * Save new student.
     * Manual transaction management: commit() must be called.
     */
    public void save(StudentModel student) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            StudentMapper mapper = session.getMapper(StudentMapper.class);
            mapper.insert(student);
            session.commit(); // Manual commit
        }
    }
    
    /**
     * Update existing student.
     */
    public void update(StudentModel student) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            StudentMapper mapper = session.getMapper(StudentMapper.class);
            mapper.update(student);
            session.commit();
        }
    }
    
    /**
     * Delete student.
     */
    public void delete(Long id) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            StudentMapper mapper = session.getMapper(StudentMapper.class);
            mapper.delete(id);
            session.commit();
        }
    }
    
    /**
     * Enroll student in subject (demonstrates many-to-many).
     */
    public void enrollInSubject(Long studentId, Long subjectId) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            StudentMapper mapper = session.getMapper(StudentMapper.class);
            mapper.enrollInSubject(studentId, subjectId);
            session.commit();
        }
    }
    
    /**
     * Unenroll student from subject.
     */
    public void unenrollFromSubject(Long studentId, Long subjectId) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            StudentMapper mapper = session.getMapper(StudentMapper.class);
            mapper.unenrollFromSubject(studentId, subjectId);
            session.commit();
        }
    }
}
