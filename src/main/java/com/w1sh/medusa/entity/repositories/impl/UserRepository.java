package com.w1sh.medusa.entity.repositories.impl;

import com.w1sh.medusa.entity.entities.User;
import com.w1sh.medusa.entity.repositories.IUserRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserRepository implements IUserRepository {

    private final EntityManager em;
    private final CriteriaBuilder criteriaBuilder;
    //private final TransactionManager transactionManager;

    public UserRepository(EntityManagerFactory em){
        this.em = em.createEntityManager();
        //this.sessionFactory = sessionFactory;
        //this.transactionManager = new TransactionManager(entityManager);
        this.criteriaBuilder = em.getCriteriaBuilder();
    }

    @Override
    public Mono<Long> isPresent(User user) {
        final CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        final Root<User> root = criteriaQuery.from(User.class);
        final Predicate predicateDiscordId = criteriaBuilder.equal(root.get("discordId"), user.getDiscordId());
        final Predicate predicateGuildId = criteriaBuilder.equal(root.get("guildId"), user.getGuildId());
        final Predicate predicate = criteriaBuilder.and(predicateDiscordId, predicateGuildId);
        criteriaQuery.select(criteriaBuilder.count(root)).where(predicate);
        final TypedQuery<Long> typedQuery = em.createQuery(criteriaQuery);
        return Mono.just(typedQuery.getSingleResult());
    }

    @Override
    public Flux<User> read() {
        final CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
        final Root<User> root = criteriaQuery.from(User.class);
        criteriaQuery.select(root);
        final TypedQuery<User> typedQuery = em.createQuery(criteriaQuery);
        return Flux.fromStream(typedQuery.getResultStream().distinct());
    }

    @Override
    public Mono<User> read(Long id) {
        return Mono.just(em.find(User.class, id));
    }

    @Override
    public void persist(User entity) {
        em.persist(entity);
        //transactionManager.doRunnableWithTransaction(() -> entityManager.persist(entity));
    }

    @Override
    public void update(User entity) {
        try {
            final CriteriaUpdate<User> criteriaUpdate = criteriaBuilder.createCriteriaUpdate(User.class);
            final Root<User> root = criteriaUpdate.from(User.class);
            final Predicate predicateId = criteriaBuilder.equal(root.get("id"), entity.getId());
            criteriaUpdate.set("id", entity.getPoints()).where(predicateId);
            em.createQuery(criteriaUpdate).executeUpdate();
        } catch (PersistenceException e){
            e.printStackTrace();
        }
        /*transactionManager.doRunnableWithTransaction(() -> {
            final CriteriaUpdate<User> criteriaUpdate = criteriaBuilder.createCriteriaUpdate(User.class);
            final Root<User> root = criteriaUpdate.from(User.class);
            final Predicate predicateId = criteriaBuilder.equal(root.get("id"), entity.getId());
            criteriaUpdate.set("id", entity.getPoints()).where(predicateId);
            entityManager.createQuery(criteriaUpdate).executeUpdate();
        });*/
    }

    @Override
    public void delete(User entity) {
    }

    private Predicate createWhereClause(final Root<User> root, final Tuple2<String, Long>[] tuples){
        if(tuples.length > 1){
            final List<Predicate> predicates = new ArrayList<>();
            for(Tuple2<String, Long> tuple : tuples){
                final Predicate predicate = criteriaBuilder.equal(root.get(tuple.getT1()), tuple.getT2());
                predicates.add(predicate);
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }else{
            return criteriaBuilder.equal(root.get(tuples[0].getT1()), tuples[0].getT2());
        }
    }
}
