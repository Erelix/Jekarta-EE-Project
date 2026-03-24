package com.myfirstproject.transaction;

import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

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
