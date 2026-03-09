package com.myfirstproject.dao.mybatis;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.myfirstproject.mybatis.mapper.GroupMapper;
import com.myfirstproject.mybatis.model.GroupModel;
import com.myfirstproject.mybatis.util.MyBatisUtil;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * MyBatis Data Access Object for Group.
 * Demonstrates one-to-many relationship handling with MyBatis.
 */
@ApplicationScoped
public class GroupMyBatisDAO {
    
    /**
     * Find group by ID.
     */
    public GroupModel findById(Long id) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            GroupMapper mapper = session.getMapper(GroupMapper.class);
            return mapper.findById(id);
        }
    }
    
    /**
     * Find all groups.
     */
    public List<GroupModel> findAll() {
        try (SqlSession session = MyBatisUtil.openSession()) {
            GroupMapper mapper = session.getMapper(GroupMapper.class);
            return mapper.findAll();
        }
    }
    
    /**
     * Find group with students (demonstrates one-to-many eager loading).
     */
    public GroupModel findByIdWithStudents(Long id) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            GroupMapper mapper = session.getMapper(GroupMapper.class);
            return mapper.findByIdWithStudents(id);
        }
    }
    
    /**
     * Save new group.
     */
    public void save(GroupModel group) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            GroupMapper mapper = session.getMapper(GroupMapper.class);
            mapper.insert(group);
            session.commit();
        }
    }
    
    /**
     * Update existing group.
     */
    public void update(GroupModel group) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            GroupMapper mapper = session.getMapper(GroupMapper.class);
            mapper.update(group);
            session.commit();
        }
    }
    
    /**
     * Delete group.
     */
    public void delete(Long id) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            GroupMapper mapper = session.getMapper(GroupMapper.class);
            mapper.delete(id);
            session.commit();
        }
    }
}
