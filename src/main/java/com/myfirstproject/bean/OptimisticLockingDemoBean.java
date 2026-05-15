package com.myfirstproject.bean;

import java.io.Serializable;

import com.myfirstproject.dao.jpa.StudentJpaDAO;
import com.myfirstproject.entity.Student;
import com.myfirstproject.transaction.Transactional;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;

@Named("optimisticLockDemo")
@SessionScoped
public class OptimisticLockingDemoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private StudentJpaDAO studentDAO;

    @Inject
    private EntityManager em;

    private Student student1;
    private Student student2;
    private String newEmail;
    private String errorMessage = "";
    private String updateMessage = "";
    private String retryMessage = "";

    @Transactional
    public String loadStudent() {
        reset();
        // Load first student (ID 1)
        student1 = em.find(Student.class, 1L);
        if (student1 == null) {
            errorMessage = "Student with ID 1 not found. Please ensure there's data in the database.";
        }
        return null;
    }

    @Transactional
    public String simulateAnotherUserUpdate() {
        if (student1 == null) {
            errorMessage = "Please load student first!";
            return null;
        }

        try {
            // Clear to simulate a different user (different EntityManager/session)
            em.clear();

            // Another user loads the same student
            student2 = em.find(Student.class, student1.getId());

            // User 2 modifies (change something to force version increment)
            student2.setEmail("user2update" + System.currentTimeMillis() + "@test.com");
            em.merge(student2);
            em.flush();
            em.refresh(student2);  // Refresh to get updated version from DB

            updateMessage = "User 2 successfully updated student. "
                + "Database version changed from " + student1.getVersion()
                + " to " + student2.getVersion();

        } catch (Exception e) {
            errorMessage = "Error during User 2 update: " + e.getMessage();
        }
        return null;
    }

    @Transactional
    public String attemptUpdateWithStaleVersion() {
        if (student1 == null) {
            errorMessage = "Please load student first!";
            return null;
        }

        try {
            // User 1 tries to update with stale version
            student1.setEmail(newEmail);
            em.merge(student1);
            em.flush();  // This triggers the version check in the UPDATE SQL

            errorMessage = "";
            updateMessage = "Update succeeded (no conflict detected)";

        } catch (OptimisticLockException e) {
            errorMessage = "OptimisticLockException caught! "
                + "Another transaction modified this entity. "
                + "Version mismatch detected in UPDATE WHERE clause. "
                + "Transaction rolled back automatically. "
                + "EntityManager remains open and usable.";

            // Important: Clear persistence context
            em.clear();
            student1 = null;  // Mark as stale

        } catch (Exception e) {
            errorMessage = "Unexpected error: " + e.getMessage();
        }
        return null;
    }

    @Transactional
    public String reloadAndRetry() {
        if (student1 == null || student1.getId() == null) {
            errorMessage = "Student data lost. Please load student again.";
            return null;
        }

        int retryCount = 0;
        final int MAX_RETRIES = 3;

        while (retryCount < MAX_RETRIES) {
            try {
                // Reload fresh data from database
                student1 = em.find(Student.class, student1.getId());

                if (student1 == null) {
                    errorMessage = "Student not found in database.";
                    return null;
                }

                // Now update with fresh version
                student1.setEmail(newEmail);
                em.merge(student1);
                em.flush();

                retryMessage = "SUCCESS! Update committed with fresh data. "
                    + "Version after successful update: " + student1.getVersion();
                errorMessage = "";
                return null;

            } catch (OptimisticLockException e) {
                retryCount++;

                em.clear();
                student1 = null;

                if (retryCount < MAX_RETRIES) {
                    retryMessage = "Retry attempt " + retryCount
                        + " failed due to conflict. Retrying...";
                } else {
                    errorMessage = "FAILED: Maximum retries (" + MAX_RETRIES
                        + ") exceeded. Could not update entity.";
                    retryMessage = "";
                    return null;
                }

            } catch (Exception e) {
                errorMessage = "Error during retry: " + e.getMessage();
                retryMessage = "";
                return null;
            }
        }

        return null;
    }

    public String reset() {
        student1 = null;
        student2 = null;
        newEmail = "";
        errorMessage = "";
        updateMessage = "";
        retryMessage = "";
        return null;
    }

    // Getters and setters
    public Student getStudent1() {
        return student1;
    }

    public void setStudent1(Student student1) {
        this.student1 = student1;
    }

    public Student getStudent2() {
        return student2;
    }

    public void setStudent2(Student student2) {
        this.student2 = student2;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getUpdateMessage() {
        return updateMessage;
    }

    public void setUpdateMessage(String updateMessage) {
        this.updateMessage = updateMessage;
    }

    public String getRetryMessage() {
        return retryMessage;
    }

    public void setRetryMessage(String retryMessage) {
        this.retryMessage = retryMessage;
    }
}
