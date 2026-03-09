package com.myfirstproject.dao.jpa;

import java.util.List;

import com.myfirstproject.entity.Group;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

/**
 * JPA Data Access Object for Group entity.
 * Demonstrates one-to-many relationship handling with JPA.
 */
@ApplicationScoped
public class GroupJpaDAO {
    
    @Inject
    private EntityManager em;
    
    /**
     * Find group by ID.
     */
    public Group findById(Long id) {
        return em.find(Group.class, id);
    }
    
    /**
     * Find all groups with students eagerly loaded.
     */
    public List<Group> findAll() {
        return em.createQuery(
            "SELECT DISTINCT g FROM Group g LEFT JOIN FETCH g.students ORDER BY g.name", 
            Group.class
        ).getResultList();
    }
    
    /**
     * Find group with students (demonstrates one-to-many eager loading).
     */
    public Group findByIdWithStudents(Long id) {
        Group group = em.createQuery(
            "SELECT DISTINCT g FROM Group g LEFT JOIN FETCH g.students WHERE g.id = :id", 
            Group.class
        )
        .setParameter("id", id)
        .getSingleResult();
        return group;
    }
    
    /**
     * Save new group.
     */
    public void save(Group group) {
        try {
            em.getTransaction().begin();
            em.persist(group);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }
    
    /**
     * Update existing group.
     */
    public Group update(Group group) {
        try {
            em.getTransaction().begin();
            Group merged = em.merge(group);
            em.getTransaction().commit();
            return merged;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }
    
    /**
     * Delete group.
     */
    public void delete(Long id) {
        try {
            em.getTransaction().begin();
            Group group = em.find(Group.class, id);
            if (group != null) {
                em.remove(group);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }
}
