package com.myfirstproject.dao.jpa;

import java.util.List;

import com.myfirstproject.entity.Subject;
import com.myfirstproject.transaction.Transactional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
@Transactional
public class SubjectJpaDAO {
    
    @Inject
    private EntityManager em;
    
    public Subject findById(Long id) {
        return em.find(Subject.class, id);
    }
    
    public List<Subject> findAll() {
        return em.createQuery(
            "SELECT DISTINCT s FROM Subject s LEFT JOIN FETCH s.students ORDER BY s.name", 
            Subject.class
        ).getResultList();
    }
    
    public Subject findByIdWithStudents(Long id) {
        return em.createQuery(
            "SELECT DISTINCT s FROM Subject s LEFT JOIN FETCH s.students WHERE s.id = :id", 
            Subject.class
        )
        .setParameter("id", id)
        .getSingleResult();
    }
    
    public List<Subject> findByStudentId(Long studentId) {
        return em.createQuery(
            "SELECT s FROM Subject s JOIN s.students st WHERE st.id = :studentId ORDER BY s.name", 
            Subject.class
        )
        .setParameter("studentId", studentId)
        .getResultList();
    }
    
    public void save(Subject subject) {
        em.persist(subject);
    }
    
    public Subject update(Subject subject) {
        return em.merge(subject);
    }
    
    public void delete(Long id) {
        Subject subject = em.find(Subject.class, id);
        if (subject != null) {
            em.remove(subject);
        }
    }
}
