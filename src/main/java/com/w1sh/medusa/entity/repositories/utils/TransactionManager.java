package com.w1sh.medusa.entity.repositories.utils;

import reactor.core.Exceptions;

import javax.persistence.EntityManager;

public class TransactionManager {

    private EntityManager entityManager;

    public TransactionManager(EntityManager entityManager){
        this.entityManager = entityManager;
    }

    public void doRunnableWithTransaction(Runnable runnable){
        try{
            if(!entityManager.getTransaction().isActive()){
                entityManager.getTransaction().begin();
            }
            runnable.run();
            // log completed runnable
            entityManager.getTransaction().commit();
        }catch(RuntimeException e){
            try{
                entityManager.getTransaction().rollback();
            }catch(RuntimeException rbe){
                // log
                throw Exceptions.propagate(e);
            }
            // log
            throw Exceptions.propagate(e);
        }
    }
}
