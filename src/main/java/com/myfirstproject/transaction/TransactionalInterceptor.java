package com.myfirstproject.transaction;

import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

/**
 * CDI Interceptor for automatic transaction management.
 * 
 * When a method is annotated with @Transactional, this interceptor:
 * 1. Begins a transaction before method execution
 * 2. Commits the transaction if the method succeeds
 * 3. Rolls back the transaction if an exception occurs
 * 
 */
@Transactional
@Interceptor
public class TransactionalInterceptor {
    
    @Inject
    private EntityManager em;
    
    @AroundInvoke
    public Object manageTransaction(InvocationContext context) throws Exception {
        EntityTransaction transaction = em.getTransaction();
        boolean isTransactionOwner = false;
        
        try {
            // Only begin transaction if not already active (support nested @Transactional)
            if (!transaction.isActive()) {
                transaction.begin();
                isTransactionOwner = true;
            }
            
            // Execute the actual method
            Object result = context.proceed();
            
            // Commit if we started the transaction
            if (isTransactionOwner && transaction.isActive()) {
                transaction.commit();
            }
            
            return result;
            
        } catch (Exception e) {
            // Rollback on any exception if we own the transaction
            if (isTransactionOwner && transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }
}
