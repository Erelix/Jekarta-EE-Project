package com.myfirstproject.dao.jpa;

import java.util.List;

import com.myfirstproject.entity.Subject;
import com.myfirstproject.transaction.Transactional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

/**
 * JPA Data Access Object for Subject entity.
 * Demonstrates many-to-many relationship handling with JPA.
 * Transactions managed declaratively with @Transactional annotation.
 */
@ApplicationScoped
@Transactional
public class SubjectJpaDAO {
    
    @Inject
    private EntityManager em;
    
    /**
     * Find subject by ID.
     */
    public Subject findById(Long id) {
        return em.find(Subject.class, id);
    }
    
    /**
     * Find all subjects with enrolled students eagerly loaded.
     */
    public List<Subject> findAll() {
        return em.createQuery(
            "SELECT DISTINCT s FROM Subject s LEFT JOIN FETCH s.students ORDER BY s.name", 
            Subject.class
        ).getResultList();
    }
    
    /**
     * Find subject with enrolled students (demonstrates many-to-many eager loading).
     */
    public Subject findByIdWithStudents(Long id) {
        return em.createQuery(
            "SELECT DISTINCT s FROM Subject s LEFT JOIN FETCH s.students WHERE s.id = :id", 
            Subject.class
        )
        .setParameter("id", id)
        .getSingleResult();
    }
    
    /**
     * Find subjects for a student (demonstrates many-to-many from other side).
     */
    public List<Subject> findByStudentId(Long studentId) {
        return em.createQuery(
            "SELECT s FROM Subject s JOIN s.students st WHERE st.id = :studentId ORDER BY s.name", 
            Subject.class
        )
        .setParameter("studentId", studentId)
        .getResultList();
    }
    
    /**
     * Save new subject.
     * Transaction automatically managed by @Transactional interceptor.
     */
    public void save(Subject subject) {
        em.persist(subject);
    }
    
    /**
     * Update existing subject.
     * Transaction automatically managed by @Transactional interceptor.
     */
    public Subject update(Subject subject) {
        return em.merge(subject);
    }
    
    /**
     * Delete subject.
     * Transaction automatically managed by @Transactional interceptor.
     */
    public void delete(Long id) {
        Subject subject = em.find(Subject.class, id);
        if (subject != null) {
            em.remove(subject);
        }
    }
}
