package com.myfirstproject.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.myfirstproject.dao.jpa.GroupJpaDAO;
import com.myfirstproject.dao.jpa.StudentJpaDAO;
import com.myfirstproject.dao.jpa.SubjectJpaDAO;
import com.myfirstproject.entity.Group;
import com.myfirstproject.entity.Student;
import com.myfirstproject.entity.Subject;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Business logic component (CDI bean) for Student operations.
 * 
 * @RequestScoped means a new instance is created for each HTTP request.
 * Other scopes: @SessionScoped (per user session), @ApplicationScoped (singleton).
 * 
 * @Inject injects dependencies (DAOs) - CDI automatically provides instances.
 */
@Named
@RequestScoped
public class StudentBean implements Serializable {
    
    @Inject
    private StudentJpaDAO studentDAO;
    
    @Inject
    private GroupJpaDAO groupDAO;
    
    @Inject
    private SubjectJpaDAO subjectDAO;
    
    private Student student = new Student();
    private List<Student> students;
    private Long selectedGroupId;
    private List<Long> selectedSubjectIds = new ArrayList<>();
    private Long editId;
    
    public void loadStudent() {
        if (editId != null && editId > 0) {
            Student studentToEdit = studentDAO.findById(editId);
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
            // DAO methods have @Transactional - automatic transaction management
            Student managedStudent;
            if (student.getId() == null) {
                // Create new
                if (selectedGroupId != null) {
                    Group group = groupDAO.findById(selectedGroupId);
                    student.setGroup(group);
                }
                if (selectedSubjectIds != null && !selectedSubjectIds.isEmpty()) {
                    List<Subject> subjects = new ArrayList<>();
                    for (Long subjectId : selectedSubjectIds) {
                        Subject subject = subjectDAO.findById(subjectId);
                        if (subject != null) {
                            subjects.add(subject);
                        }
                    }
                    student.setSubjects(subjects);
                }
                studentDAO.save(student);
            } else {
                // Update existing
                managedStudent = studentDAO.findById(student.getId());
                managedStudent.setFirstName(student.getFirstName());
                managedStudent.setLastName(student.getLastName());
                managedStudent.setEmail(student.getEmail());
                
                if (selectedGroupId != null) {
                    Group group = groupDAO.findById(selectedGroupId);
                    managedStudent.setGroup(group);
                }
                
                managedStudent.getSubjects().clear();
                if (selectedSubjectIds != null && !selectedSubjectIds.isEmpty()) {
                    for (Long subjectId : selectedSubjectIds) {
                        Subject subject = subjectDAO.findById(subjectId);
                        if (subject != null) {
                            managedStudent.getSubjects().add(subject);
                        }
                    }
                }
                studentDAO.update(managedStudent);
            }
            
            student = new Student();
            selectedGroupId = null;
            selectedSubjectIds = new ArrayList<>();
            students = null;
            return "students?faces-redirect=true";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public String deleteStudent(Long id) {
        try {
            studentDAO.delete(id);
            students = null;
            return "students?faces-redirect=true";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public String editStudent(Long id) {
        return "student-form?faces-redirect=true&editId=" + id;
    }
    
    public Student findStudentById(Long id) {
        return studentDAO.findById(id);
    }
    
    public List<Student> getAllStudents() {
        if (students == null) {
            students = studentDAO.findAll();
        }
        return students;
    }
    
    public List<Group> getAllGroups() {
        return groupDAO.findAll();
    }
    
    public List<Subject> getAllSubjects() {
        return subjectDAO.findAll();
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
