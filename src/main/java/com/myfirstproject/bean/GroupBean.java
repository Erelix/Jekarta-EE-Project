package com.myfirstproject.bean;

import java.io.Serializable;
import java.util.List;

import com.myfirstproject.entity.Group;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;

@Named
@RequestScoped
public class GroupBean implements Serializable {
    
    @Inject
    private EntityManager em;
    
    private Group group = new Group();
    private List<Group> groups;
    private Long editId;
    
    public void loadGroup() {
        if (editId != null && editId > 0) {
            Group groupToEdit = em.find(Group.class, editId);
            if (groupToEdit != null) {
                this.group = groupToEdit;
            }
        }
    }
    
    public String saveGroup() {
        try {
            em.getTransaction().begin();
            if (group.getId() == null) {
                em.persist(group);
            } else {
                em.merge(group);
            }
            em.getTransaction().commit();
            group = new Group();
            groups = null;
            return "groups?faces-redirect=true";
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            return null;
        }
    }
    
    public String deleteGroup(Long id) {
        try {
            em.getTransaction().begin();
            Group groupToDelete = em.find(Group.class, id);
            if (groupToDelete != null) {
                em.remove(groupToDelete);
            }
            em.getTransaction().commit();
            groups = null; // Reset list to force refresh
            return "groups?faces-redirect=true";
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            return null;
        }
    }
    
    public String editGroup(Long id) {
        return "group-form?faces-redirect=true&editId=" + id;
    }
    
    public Group findGroupById(Long id) {
        return em.find(Group.class, id);
    }
    
    public List<Group> getAllGroups() {
        if (groups == null) {
            groups = em.createQuery("SELECT DISTINCT g FROM Group g LEFT JOIN FETCH g.students ORDER BY g.name", Group.class).getResultList();
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
