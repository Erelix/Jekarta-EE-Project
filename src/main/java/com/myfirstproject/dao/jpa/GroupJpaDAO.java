package com.myfirstproject.dao.jpa;

import java.util.List;

import com.myfirstproject.entity.Group;
import com.myfirstproject.transaction.Transactional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
@Transactional
public class GroupJpaDAO {
    
    @Inject
    private EntityManager em;
    
    public Group findById(Long id) {
        return em.find(Group.class, id);
    }
    
    public List<Group> findAll() {
        return em.createQuery(
            "SELECT DISTINCT g FROM Group g LEFT JOIN FETCH g.students ORDER BY g.name", 
            Group.class
        ).getResultList();
    }
    
    public Group findByIdWithStudents(Long id) {
        Group group = em.createQuery(
            "SELECT DISTINCT g FROM Group g LEFT JOIN FETCH g.students WHERE g.id = :id", 
            Group.class
        )
        .setParameter("id", id)
        .getSingleResult();
        return group;
    }
    
    public void save(Group group) {
        em.persist(group);
    }
    
    public Group update(Group group) {
        return em.merge(group);
    }
    
    public void delete(Long id) {
        Group group = em.find(Group.class, id);
        if (group != null) {
            em.remove(group);
        }
    }
}
