package com.myfirstproject.dao.mybatis;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.myfirstproject.mybatis.mapper.GroupMapper;
import com.myfirstproject.mybatis.model.GroupModel;
import com.myfirstproject.mybatis.util.MyBatisUtil;

import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class GroupMyBatisDAO {
    
    public GroupModel findById(Long id) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            GroupMapper mapper = session.getMapper(GroupMapper.class);
            return mapper.findById(id);
        }
    }
    
    public List<GroupModel> findAll() {
        try (SqlSession session = MyBatisUtil.openSession()) {
            GroupMapper mapper = session.getMapper(GroupMapper.class);
            return mapper.findAll();
        }
    }
    
    public GroupModel findByIdWithStudents(Long id) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            GroupMapper mapper = session.getMapper(GroupMapper.class);
            return mapper.findByIdWithStudents(id);
        }
    }
    
    public void save(GroupModel group) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            GroupMapper mapper = session.getMapper(GroupMapper.class);
            mapper.insert(group);
            session.commit();
        }
    }
    
    public void update(GroupModel group) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            GroupMapper mapper = session.getMapper(GroupMapper.class);
            mapper.update(group);
            session.commit();
        }
    }
    
    public void delete(Long id) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            GroupMapper mapper = session.getMapper(GroupMapper.class);
            mapper.delete(id);
            session.commit();
        }
    }
}
