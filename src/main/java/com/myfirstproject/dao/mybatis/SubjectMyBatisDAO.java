package com.myfirstproject.dao.mybatis;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.myfirstproject.mybatis.mapper.SubjectMapper;
import com.myfirstproject.mybatis.model.SubjectModel;
import com.myfirstproject.mybatis.util.MyBatisUtil;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SubjectMyBatisDAO {
    
    public SubjectModel findById(Long id) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            SubjectMapper mapper = session.getMapper(SubjectMapper.class);
            return mapper.findById(id);
        }
    }
    
    public List<SubjectModel> findAll() {
        try (SqlSession session = MyBatisUtil.openSession()) {
            SubjectMapper mapper = session.getMapper(SubjectMapper.class);
            return mapper.findAll();
        }
    }
    
    public SubjectModel findByIdWithStudents(Long id) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            SubjectMapper mapper = session.getMapper(SubjectMapper.class);
            return mapper.findByIdWithStudents(id);
        }
    }
    
    public List<SubjectModel> findByStudentId(Long studentId) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            SubjectMapper mapper = session.getMapper(SubjectMapper.class);
            return mapper.findByStudentId(studentId);
        }
    }
    
    public void save(SubjectModel subject) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            SubjectMapper mapper = session.getMapper(SubjectMapper.class);
            mapper.insert(subject);
            session.commit();
        }
    }
    
    public void update(SubjectModel subject) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            SubjectMapper mapper = session.getMapper(SubjectMapper.class);
            mapper.update(subject);
            session.commit();
        }
    }
    
    public void delete(Long id) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            SubjectMapper mapper = session.getMapper(SubjectMapper.class);
            mapper.delete(id);
            session.commit();
        }
    }
}
