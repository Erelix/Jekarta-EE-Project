package com.myfirstproject.dao.mybatis;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.myfirstproject.mybatis.mapper.SubjectMapper;
import com.myfirstproject.mybatis.model.SubjectModel;
import com.myfirstproject.mybatis.util.MyBatisUtil;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * MyBatis Data Access Object for Subject.
 * Demonstrates many-to-many relationship handling with MyBatis.
 */
@ApplicationScoped
public class SubjectMyBatisDAO {
    
    /**
     * Find subject by ID.
     */
    public SubjectModel findById(Long id) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            SubjectMapper mapper = session.getMapper(SubjectMapper.class);
            return mapper.findById(id);
        }
    }
    
    /**
     * Find all subjects.
     */
    public List<SubjectModel> findAll() {
        try (SqlSession session = MyBatisUtil.openSession()) {
            SubjectMapper mapper = session.getMapper(SubjectMapper.class);
            return mapper.findAll();
        }
    }
    
    /**
     * Find subject with enrolled students (demonstrates many-to-many).
     */
    public SubjectModel findByIdWithStudents(Long id) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            SubjectMapper mapper = session.getMapper(SubjectMapper.class);
            return mapper.findByIdWithStudents(id);
        }
    }
    
    /**
     * Find subjects for a student (demonstrates many-to-many from other side).
     */
    public List<SubjectModel> findByStudentId(Long studentId) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            SubjectMapper mapper = session.getMapper(SubjectMapper.class);
            return mapper.findByStudentId(studentId);
        }
    }
    
    /**
     * Save new subject.
     */
    public void save(SubjectModel subject) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            SubjectMapper mapper = session.getMapper(SubjectMapper.class);
            mapper.insert(subject);
            session.commit();
        }
    }
    
    /**
     * Update existing subject.
     */
    public void update(SubjectModel subject) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            SubjectMapper mapper = session.getMapper(SubjectMapper.class);
            mapper.update(subject);
            session.commit();
        }
    }
    
    /**
     * Delete subject.
     */
    public void delete(Long id) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            SubjectMapper mapper = session.getMapper(SubjectMapper.class);
            mapper.delete(id);
            session.commit();
        }
    }
}
