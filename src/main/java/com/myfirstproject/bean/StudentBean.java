package com.myfirstproject.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.myfirstproject.entity.Group;
import com.myfirstproject.entity.Student;
import com.myfirstproject.entity.Subject;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;

@Named
@RequestScoped
public class StudentBean implements Serializable {
    
    @Inject
    private EntityManager em;
    
    private Student student = new Student();
    private List<Student> students;
    private Long selectedGroupId;
    private List<Long> selectedSubjectIds = new ArrayList<>();
    private Long editId;
    
    public void loadStudent() {
        if (editId != null && editId > 0) {
            Student studentToEdit = em.find(Student.class, editId);
            if (studentToEdit != null) {
                this.student = studentToEdit;
                this.selectedGroupId = studentToEdit.getGroup() != null ? studentToEdit.getGroup().getId() : null;
                this.selectedSubjectIds = new ArrayList<>();
                for (Subject subject : studentToEdit.getSubjects()) {
                    this.selectedSubjectIds.add(subject.getId());
                }
            }
        }
    }
    
    public String saveStudent() {
        try {
            em.getTransaction().begin();
            
            Student managedStudent;
            if (student.getId() == null) {
                // Create new
                if (selectedGroupId != null) {
                    Group group = em.find(Group.class, selectedGroupId);
                    student.setGroup(group);
                }
                if (selectedSubjectIds != null && !selectedSubjectIds.isEmpty()) {
                    List<Subject> subjects = new ArrayList<>();
                    for (Long subjectId : selectedSubjectIds) {
                        Subject subject = em.find(Subject.class, subjectId);
                        if (subject != null) {
                            subjects.add(subject);
                        }
                    }
                    student.setSubjects(subjects);
                }
                em.persist(student);
            } else {
                // Update existing
                managedStudent = em.find(Student.class, student.getId());
                managedStudent.setFirstName(student.getFirstName());
                managedStudent.setLastName(student.getLastName());
                managedStudent.setEmail(student.getEmail());
                
                if (selectedGroupId != null) {
                    Group group = em.find(Group.class, selectedGroupId);
                    managedStudent.setGroup(group);
                }
                
                managedStudent.getSubjects().clear();
                if (selectedSubjectIds != null && !selectedSubjectIds.isEmpty()) {
                    for (Long subjectId : selectedSubjectIds) {
                        Subject subject = em.find(Subject.class, subjectId);
                        if (subject != null) {
                            managedStudent.getSubjects().add(subject);
                        }
                    }
                }
                em.merge(managedStudent);
            }
            
            em.getTransaction().commit();
            student = new Student();
            selectedGroupId = null;
            selectedSubjectIds = new ArrayList<>();
            students = null;
            return "students?faces-redirect=true";
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            return null;
        }
    }
    
    public String deleteStudent(Long id) {
        try {
            em.getTransaction().begin();
            Student studentToDelete = em.find(Student.class, id);
            if (studentToDelete != null) {
                em.remove(studentToDelete);
            }
            em.getTransaction().commit();
            students = null;
            return "students?faces-redirect=true";
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            return null;
        }
    }
    
    public String editStudent(Long id) {
        return "student-form?faces-redirect=true&editId=" + id;
    }
    
    public Student findStudentById(Long id) {
        return em.find(Student.class, id);
    }
    
    public List<Student> getAllStudents() {
        if (students == null) {
            students = em.createQuery("SELECT s FROM Student s ORDER BY s.lastName, s.firstName", Student.class).getResultList();
        }
        return students;
    }
    
    public List<Group> getAllGroups() {
        return em.createQuery("SELECT g FROM Group g ORDER BY g.name", Group.class).getResultList();
    }
    
    public List<Subject> getAllSubjects() {
        return em.createQuery("SELECT s FROM Subject s ORDER BY s.name", Subject.class).getResultList();
    }
    
    public Student getStudent() {
        return student;
    }
    
    public void setStudent(Student student) {
        this.student = student;
    }
    
    public List<Student> getStudents() {
        return getAllStudents();
    }
    
    public Long getSelectedGroupId() {
        return selectedGroupId;
    }
    
    public void setSelectedGroupId(Long selectedGroupId) {
        this.selectedGroupId = selectedGroupId;
    }
    
    public List<Long> getSelectedSubjectIds() {
        return selectedSubjectIds;
    }
    
    public void setSelectedSubjectIds(List<Long> selectedSubjectIds) {
        this.selectedSubjectIds = selectedSubjectIds;
    }
    
    public Long getEditId() {
        return editId;
    }
    
    public void setEditId(Long editId) {
        this.editId = editId;
    }
}
