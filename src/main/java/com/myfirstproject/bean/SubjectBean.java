package com.myfirstproject.bean;

import java.io.Serializable;
import java.util.List;

import com.myfirstproject.dao.jpa.SubjectJpaDAO;
import com.myfirstproject.entity.Subject;
import com.myfirstproject.transaction.Transactional;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;


@Named
@RequestScoped
public class SubjectBean implements Serializable {
    
    @Inject
    private SubjectJpaDAO subjectDAO;
    
    @Inject
    private EntityManager em;
    
    private Subject subject = new Subject();
    private List<Subject> subjects;
    private Long editId;
    private String errorMessage;
    private String successMessage;
    
    public void loadSubject() {
        if (editId != null && editId > 0) {
            Subject subjectToEdit = subjectDAO.findById(editId);
            if (subjectToEdit != null) {
                this.subject = subjectToEdit;
            }
        }
    }
    
    @Transactional
    public String saveSubject() {
        try {
            errorMessage = null;
            successMessage = null;
            
            if (subject.getId() == null) {
                subjectDAO.save(subject);
            } else {
                subject = em.merge(subject);
                em.flush();
            }
            successMessage = "Subject saved successfully!";
            subject = new Subject();
            subjects = null;
            return "subjects?faces-redirect=true";
        } catch (OptimisticLockException e) {
            errorMessage = "This record was modified by another user. Please reload and try again.";
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = "Error saving subject: " + e.getMessage();
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
            if (subject.getId() != null) {
                subject = subjectDAO.findById(subject.getId());
            }
            
            successMessage = "Data reloaded. You can make your changes again.";
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = "Error reloading: " + e.getMessage();
            return null;
        }
    }
    
    public String deleteSubject(Long id) {
        try {
            subjectDAO.delete(id);
            subjects = null;
            return "subjects?faces-redirect=true";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public String editSubject(Long id) {
        return "subject-form?faces-redirect=true&editId=" + id;
    }
    
    public Subject findSubjectById(Long id) {
        return subjectDAO.findById(id);
    }
    
    public List<Subject> getAllSubjects() {
        if (subjects == null) {
            subjects = subjectDAO.findAll();
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
