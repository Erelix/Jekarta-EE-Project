package com.myfirstproject.bean;

import java.io.Serializable;
import java.util.List;

import com.myfirstproject.entity.Subject;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;

@Named
@RequestScoped
public class SubjectBean implements Serializable {
    
    @Inject
    private EntityManager em;
    
    private Subject subject = new Subject();
    private List<Subject> subjects;
    private Long editId;
    
    public void loadSubject() {
        if (editId != null && editId > 0) {
            Subject subjectToEdit = em.find(Subject.class, editId);
            if (subjectToEdit != null) {
                this.subject = subjectToEdit;
            }
        }
    }
    
    public String saveSubject() {
        try {
            em.getTransaction().begin();
            if (subject.getId() == null) {
                em.persist(subject);
            } else {
                em.merge(subject);
            }
            em.getTransaction().commit();
            subject = new Subject();
            subjects = null;
            return "subjects?faces-redirect=true";
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            return null;
        }
    }
    
    public String deleteSubject(Long id) {
        try {
            em.getTransaction().begin();
            Subject subjectToDelete = em.find(Subject.class, id);
            if (subjectToDelete != null) {
                // Remove associations with students
                for (var student : subjectToDelete.getStudents()) {
                    student.getSubjects().remove(subjectToDelete);
                }
                em.remove(subjectToDelete);
            }
            em.getTransaction().commit();
            subjects = null;
            return "subjects?faces-redirect=true";
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            return null;
        }
    }
    
    public String editSubject(Long id) {
        return "subject-form?faces-redirect=true&editId=" + id;
    }
    
    public Subject findSubjectById(Long id) {
        return em.find(Subject.class, id);
    }
    
    public List<Subject> getAllSubjects() {
        if (subjects == null) {
            subjects = em.createQuery("SELECT DISTINCT s FROM Subject s LEFT JOIN FETCH s.students ORDER BY s.name", Subject.class).getResultList();
        }
        return subjects;
    }
    
    public Subject getSubject() {
        return subject;
    }
    
    public void setSubject(Subject subject) {
        this.subject = subject;
    }
    
    public List<Subject> getSubjects() {
        return getAllSubjects();
    }
    
    public Long getEditId() {
        return editId;
    }
    
    public void setEditId(Long editId) {
        this.editId = editId;
    }
}
