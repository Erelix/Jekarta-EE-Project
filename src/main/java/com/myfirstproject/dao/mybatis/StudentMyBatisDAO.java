package com.myfirstproject.dao.mybatis;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.myfirstproject.mybatis.mapper.StudentMapper;
import com.myfirstproject.mybatis.model.StudentModel;
import com.myfirstproject.mybatis.util.MyBatisUtil;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StudentMyBatisDAO {
    
    public StudentModel findById(Long id) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            StudentMapper mapper = session.getMapper(StudentMapper.class);
            return mapper.findById(id);
        }
    }
    
    public List<StudentModel> findAll() {
        try (SqlSession session = MyBatisUtil.openSession()) {
            StudentMapper mapper = session.getMapper(StudentMapper.class);
            return mapper.findAll();
        }
    }
    
    public List<StudentModel> findByGroupId(Long groupId) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            StudentMapper mapper = session.getMapper(StudentMapper.class);
            return mapper.findByGroupId(groupId);
        }
    }
    
    /**
     * Manual transaction management: commit() must be called.
     */
    public void save(StudentModel student) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            StudentMapper mapper = session.getMapper(StudentMapper.class);
            mapper.insert(student);
            session.commit(); // Manual commit
        }
    }
    
    public void update(StudentModel student) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            StudentMapper mapper = session.getMapper(StudentMapper.class);
            mapper.update(student);
            session.commit();
        }
    }
    
    public void delete(Long id) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            StudentMapper mapper = session.getMapper(StudentMapper.class);
            mapper.delete(id);
            session.commit();
        }
    }
    
    public void enrollInSubject(Long studentId, Long subjectId) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            StudentMapper mapper = session.getMapper(StudentMapper.class);
            mapper.enrollInSubject(studentId, subjectId);
            session.commit();
        }
    }
    
    public void unenrollFromSubject(Long studentId, Long subjectId) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            StudentMapper mapper = session.getMapper(StudentMapper.class);
            mapper.unenrollFromSubject(studentId, subjectId);
            session.commit();
        }
    }
}
