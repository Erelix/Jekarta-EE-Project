package com.myfirstproject.bean;

import java.io.Serializable;
import java.util.List;

import com.myfirstproject.dao.jpa.GroupJpaDAO;
import com.myfirstproject.entity.Group;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;


@Named
@RequestScoped
public class GroupBean implements Serializable {
    
    @Inject
    private GroupJpaDAO groupDAO;
    
    private Group group = new Group();
    private List<Group> groups;
    private Long editId;
    
    public void loadGroup() {
        if (editId != null && editId > 0) {
            Group groupToEdit = groupDAO.findById(editId);
            if (groupToEdit != null) {
                this.group = groupToEdit;
            }
        }
    }
    
    public String saveGroup() {
        try {
            if (group.getId() == null) {
                groupDAO.save(group);
            } else {
                groupDAO.update(group);
            }
            group = new Group();
            groups = null;
            return "groups?faces-redirect=true";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public String deleteGroup(Long id) {
        try {
            groupDAO.delete(id);
            groups = null; // Reset list to force refresh
            return "groups?faces-redirect=true";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public String editGroup(Long id) {
        return "group-form?faces-redirect=true&editId=" + id;
    }
    
    public Group findGroupById(Long id) {
        return groupDAO.findById(id);
    }
    
    public List<Group> getAllGroups() {
        if (groups == null) {
            groups = groupDAO.findAll();
        }
        return groups;
    }
    
    public Group getGroup() {
        return group;
    }
    
    public void setGroup(Group group) {
        this.group = group;
    }
    
    public List<Group> getGroups() {
        return getAllGroups();
    }
    
    public Long getEditId() {
        return editId;
    }
    
    public void setEditId(Long editId) {
        this.editId = editId;
    }
}
