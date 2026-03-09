package com.myfirstproject.mybatis.mapper;

import java.util.List;

import com.myfirstproject.mybatis.model.GroupModel;

/**
 * MyBatis Mapper interface for Group operations.
 */
public interface GroupMapper {
    
    /**
     * Find group by ID with students (demonstrates one-to-many).
     */
    GroupModel findById(Long id);
    
    /**
     * Find all groups.
     */
    List<GroupModel> findAll();
    
    /**
     * Find group with all its students loaded.
     */
    GroupModel findByIdWithStudents(Long id);
    
    /**
     * Insert new group.
     */
    void insert(GroupModel group);
    
    /**
     * Update existing group.
     */
    void update(GroupModel group);
    
    /**
     * Delete group by ID.
     */
    void delete(Long id);
}
