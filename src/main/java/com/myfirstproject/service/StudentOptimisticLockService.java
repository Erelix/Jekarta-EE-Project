package com.myfirstproject.service;

import com.myfirstproject.dao.jpa.StudentJpaDAO;
import com.myfirstproject.entity.Student;
import com.myfirstproject.transaction.Transactional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;

@ApplicationScoped
public class StudentOptimisticLockService {

    @Inject
    private StudentJpaDAO studentDAO;

    @Inject
    private EntityManager em;

    private static final int MAX_RETRIES = 3;

    public Student updateStudentWithRetry(Long studentId, String newFirstName, String newEmail) {
        int retryCount = 0;

        while (retryCount < MAX_RETRIES) {
            try {
                return updateStudentInternal(studentId, newFirstName, newEmail);
            } catch (OptimisticLockException e) {
                retryCount++;

                if (retryCount >= MAX_RETRIES) {
                    System.err.println("Failed after " + MAX_RETRIES + " retries: " + e.getMessage());
                    throw new OptimisticLockRetryExhaustedException(
                        "Could not update student after " + MAX_RETRIES + " attempts", e);
                }

                // Clear persistence context for next attempt
                em.clear();
                System.out.println("Retry attempt " + (retryCount + 1) + " due to optimistic lock conflict");
            }
        }

        return null;
    }

    @Transactional
    private Student updateStudentInternal(Long studentId, String newFirstName, String newEmail) {
        Student student = em.find(Student.class, studentId);
        if (student == null) {
            throw new IllegalArgumentException("Student not found: " + studentId);
        }

        System.out.println("Current version: " + student.getVersion());
        student.setFirstName(newFirstName);
        student.setEmail(newEmail);

        // merge triggers version check on flush
        Student merged = em.merge(student);
        em.flush();

        System.out.println("After update, new version: " + merged.getVersion());
        return merged;
    }

    public static class OptimisticLockRetryExhaustedException extends RuntimeException {
        public OptimisticLockRetryExhaustedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
