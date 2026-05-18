package com.myfirstproject.bean;

import java.io.Serializable;
import java.util.List;

import com.myfirstproject.dao.jpa.GroupJpaDAO;
import com.myfirstproject.entity.Group;
import com.myfirstproject.transaction.Transactional;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;


@Named
@RequestScoped
public class GroupBean implements Serializable {
    
    @Inject
    private GroupJpaDAO groupDAO;
    
    @Inject
    private EntityManager em;
    
    private Group group = new Group();
    private List<Group> groups;
    private Long editId;
    private String errorMessage;
    private String successMessage;
    
    public void loadGroup() {
        if (editId != null && editId > 0) {
            Group groupToEdit = groupDAO.findById(editId);
            if (groupToEdit != null) {
                this.group = groupToEdit;
            }
        }
    }
    
    @Transactional
    public String saveGroup() {
        try {
            errorMessage = null;
            successMessage = null;
            
            if (group.getId() == null) {
                groupDAO.save(group);
            } else {
                group = em.merge(group);
                em.flush();
            }
            successMessage = "Group saved successfully!";
            group = new Group();
            groups = null;
            return "groups?faces-redirect=true";
        } catch (OptimisticLockException e) {
            errorMessage = "This record was modified by another user. Please reload and try again.";
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = "Error saving group: " + e.getMessage();
            return null;
        }
    }
    
    @Transactional
    public String reloadAndRetry() {
        try {
            errorMessage = null;
            
            // Clear persistence context to force fresh load
            em.clear();
            
            // Reload fresh data from database
            if (group.getId() != null) {
                group = groupDAO.findById(group.getId());
            }
            
            successMessage = "Data reloaded. You can make your changes again.";
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = "Error reloading: " + e.getMessage();
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
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getSuccessMessage() {
        return successMessage;
    }
    
    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }
}
