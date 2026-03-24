package com.myfirstproject.bean;

import java.io.Serializable;
import java.util.List;

import com.myfirstproject.dao.jpa.SubjectJpaDAO;
import com.myfirstproject.entity.Subject;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;


@Named
@RequestScoped
public class SubjectBean implements Serializable {
    
    @Inject
    private SubjectJpaDAO subjectDAO;
    
    private Subject subject = new Subject();
    private List<Subject> subjects;
    private Long editId;
    
    public void loadSubject() {
        if (editId != null && editId > 0) {
            Subject subjectToEdit = subjectDAO.findById(editId);
            if (subjectToEdit != null) {
                this.subject = subjectToEdit;
            }
        }
    }
    
    public String saveSubject() {
        try {
            if (subject.getId() == null) {
                subjectDAO.save(subject);
            } else {
                subjectDAO.update(subject);
            }
            subject = new Subject();
            subjects = null;
            return "subjects?faces-redirect=true";
        } catch (Exception e) {
            e.printStackTrace();
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
}
