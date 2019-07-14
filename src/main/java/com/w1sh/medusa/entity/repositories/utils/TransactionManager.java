package com.w1sh.medusa.entity.repositories.utils;

import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.util.concurrent.Callable;

public class TransactionManager {

    private EntityManager entityManager;

    public TransactionManager(EntityManager entityManager){
        this.entityManager = entityManager;
    }

    public Mono doCallable(Callable<?> callable){
        return Mono.fromCallable(callable).onErrorMap(error -> {
            // log
            return Exceptions.propagate(error);
        });
    }

    public void doRunnable(Runnable runnable){
        try {
            runnable.run();
        } catch (PersistenceException pe){
            // log
            Exceptions.propagate(pe);
        }
    }

    public void doRunnableWithTransaction(Runnable runnable){
        try{
            if(!entityManager.getTransaction().isActive()){
                entityManager.getTransaction().begin();
            }
            System.out.println("Persisting! Tx");
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
