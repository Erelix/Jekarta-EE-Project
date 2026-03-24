package com.myfirstproject.mybatis.mapper;

import java.util.List;

import com.myfirstproject.mybatis.model.GroupModel;

public interface GroupMapper {
    
    GroupModel findById(Long id);
    
    List<GroupModel> findAll();
    
    GroupModel findByIdWithStudents(Long id);
    
    void insert(GroupModel group);
    
    void update(GroupModel group);
    
    void delete(Long id);
}
