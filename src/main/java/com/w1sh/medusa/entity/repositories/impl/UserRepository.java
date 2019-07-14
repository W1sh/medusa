package com.w1sh.medusa.entity.repositories.impl;

import com.w1sh.medusa.entity.entities.User;
import com.w1sh.medusa.entity.repositories.IUserRepository;
import com.w1sh.medusa.entity.repositories.utils.TransactionManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

public class UserRepository implements IUserRepository {

    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;
    private final TransactionManager transactionManager;

    public UserRepository(){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("PersistenceUnit");
        this.entityManager = emf.createEntityManager();
        this.transactionManager = new TransactionManager(entityManager);
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
    }

    @Override
    public Mono<Long> isPresent(User user) {
        final CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        final Root<User> root = criteriaQuery.from(User.class);
        final Predicate predicateDiscordId = criteriaBuilder.equal(root.get("discordId"), user.getDiscordId());
        final Predicate predicateGuildId = criteriaBuilder.equal(root.get("guildId"), user.getGuildId());
        final Predicate predicate = criteriaBuilder.and(predicateDiscordId, predicateGuildId);
        criteriaQuery.select(criteriaBuilder.count(root)).where(predicate);
        final TypedQuery<Long> typedQuery = entityManager.createQuery(criteriaQuery);
        return Mono.just(typedQuery.getSingleResult());
    }

    @Override
    public Flux<User> read() {
        final CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
        final Root<User> root = criteriaQuery.from(User.class);
        criteriaQuery.select(root);
        final TypedQuery<User> typedQuery = entityManager.createQuery(criteriaQuery);
        return Flux.fromStream(typedQuery.getResultStream().distinct());
    }

    @Override
    public Mono<User> read(Long id) {
        return null;
    }

    @Override
    public void persist(User entity) {
        transactionManager.doRunnableWithTransaction(() -> entityManager.persist(entity));
    }

    @Override
    public Mono<Integer> update(User entity) {
        return null;
    }

    @Override
    public Mono<Integer> delete(User entity) {
        return null;
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
