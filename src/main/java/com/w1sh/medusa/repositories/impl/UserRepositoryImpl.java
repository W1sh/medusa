package com.w1sh.medusa.repositories.impl;

import com.w1sh.medusa.model.entities.User;
import com.w1sh.medusa.repositories.UserRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@Repository
public class UserRepositoryImpl implements UserRepository {

    //private static final Logger logger = LogManager.getLogger(UserRepository.class.getName());
    //private final EntityManager em;
    //private final CriteriaBuilder criteriaBuilder;

    public UserRepositoryImpl(){
        //this.em = em.createEntityManager();
        //this.criteriaBuilder = em.getCriteriaBuilder();
    }

    @Override
    public Mono<Long> isPresent(User user) {
        /*try {
            final Query query = em.createNamedQuery("User.isPresentInGuildById", User.class)
                    .setParameter("gId", user.getGuildId())
                    .setParameter("dId", user.getDiscordId());
            //logger.info("Searching database for user with discordId: {} and guildId: {}", user.getDiscordId(), user.getGuildId());
            return Mono.justOrEmpty((Long)query.getSingleResult());
        } catch (PersistenceException e){
            //logger.error("Failed to find user with discordId: {} and guildId: {}", user.getDiscordId(), user.getGuildId(), e);
            return Mono.empty();
        }*/
        return Mono.empty();
    }

    @Override
    public Flux<User> read() {
        /*try {
            final TypedQuery<User> query = em.createNamedQuery("User.isPresentInGuildById", User.class);
            //logger.info("Searching for all users...");
            return Flux.fromStream(query.getResultStream());
        } catch (PersistenceException e){
            //logger.error("Failed when searching for all users", e);
            return Flux.empty();
        }*/
        return Flux.empty();
    }

    @Override
    public Mono<User> read(Long id) {
        //return Mono.just(em.find(User.class, id));
        return Mono.empty();
    }

    @Override
    public void persist(User entity) {
        //em.persist(entity);
        //transactionManager.doRunnableWithTransaction(() -> entityManager.persist(entity));
    }

    @Override
    public void update(User entity) {
        /*try {
            final CriteriaUpdate<User> criteriaUpdate = criteriaBuilder.createCriteriaUpdate(User.class);
            final Root<User> root = criteriaUpdate.from(User.class);
            final Predicate predicateId = criteriaBuilder.equal(root.get("id"), entity.getId());
            criteriaUpdate.set("id", entity.getPoints()).where(predicateId);
            em.createQuery(criteriaUpdate).executeUpdate();
        } catch (PersistenceException e){
            e.printStackTrace();
        }*/
        /*transactionManager.doRunnableWithTransaction(() -> {
            final CriteriaUpdate<User> criteriaUpdate = criteriaBuilder.createCriteriaUpdate(User.class);
            final Root<User> root = criteriaUpdate.from(User.class);
            final Predicate predicateId = criteriaBuilder.equal(root.get("id"), entity.getId());
            criteriaUpdate.set("id", entity.getPoints()).where(predicateId);
            entityManager.createQuery(criteriaUpdate).executeUpdate();
        });*/
    }

    @Override
    public void delete(User entity) { }

    private Predicate createWhereClause(final Root<User> root, final Tuple2<String, Long>[] tuples){
        /*if(tuples.length > 1){
            final List<Predicate> predicates = new ArrayList<>();
            for(Tuple2<String, Long> tuple : tuples){
                final Predicate predicate = criteriaBuilder.equal(root.get(tuple.getT1()), tuple.getT2());
                predicates.add(predicate);
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }else{
            return criteriaBuilder.equal(root.get(tuples[0].getT1()), tuples[0].getT2());
        }*/
        return null;
    }
}
