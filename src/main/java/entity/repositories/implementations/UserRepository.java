package entity.repositories.implementations;

import entity.entities.User;
import entity.repositories.IUserRepository;
import reactor.util.function.Tuple2;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class UserRepository implements IUserRepository {

    private static UserRepository instance = null;
    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;

    private UserRepository(){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("PersistenceUnit");
        this.entityManager = emf.createEntityManager();
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
    }

    public static UserRepository getInstance(){
        if(instance == null){
            instance = new UserRepository();
        }
        return instance;
    }

    @Override
    public Stream<User> read() {
        final CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
        final Root<User> root = criteriaQuery.from(User.class);
        criteriaQuery.select(root);
        return entityManager.createQuery(criteriaQuery).getResultStream();
    }

    @Override
    public User read(final Tuple2<String, Long>[] tuples) {
        final CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
        final Root<User> root = criteriaQuery.from(User.class);
        final Predicate predicate = createWhereClause(root, tuples);
        criteriaQuery.select(root).where(predicate);
        final TypedQuery<User> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setMaxResults(1);
        try {
            return typedQuery.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public User read(Long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void persist(User entity) {
        beginTransaction();
        entityManager.persist(entity);
        entityManager.getTransaction().commit();
    }

    @Override
    public int update(Tuple2<String, Long> tupleUpdate, Tuple2<String, Long> tuplePredicate) {
        try {
            beginTransaction();
            final CriteriaUpdate<User> updateQuery = criteriaBuilder.createCriteriaUpdate(User.class);
            final Root<User> root = updateQuery.from(User.class);
            final Predicate predicate = criteriaBuilder.equal(root.get(tuplePredicate.getT1()), tuplePredicate.getT2());
            updateQuery.set(tupleUpdate.getT1(), tupleUpdate.getT2()).where(predicate);
            return entityManager.createQuery(updateQuery).executeUpdate();
        } catch (PersistenceException e){
            entityManager.getTransaction().rollback();
        }
        return 0;
    }

    @Override
    public int update(User entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(User entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Tuple2<String, Long> tuple) {
        final CriteriaDelete<User> deleteQuery = criteriaBuilder.createCriteriaDelete(User.class);
        final Root<User> root = deleteQuery.from(User.class);
        final Predicate predicate = criteriaBuilder.equal(root.get(tuple.getT1()), tuple.getT2());
        deleteQuery.where(predicate);
        return entityManager.createQuery(deleteQuery).executeUpdate();
    }

    @Override @SuppressWarnings(value = "unchecked")
    public int delete(Tuple2<String, Long> tuple1, Tuple2<String, Long> tuple2) {
        final CriteriaDelete<User> deleteQuery = criteriaBuilder.createCriteriaDelete(User.class);
        final Root<User> root = deleteQuery.from(User.class);
        final Predicate predicate = createWhereClause(root, new Tuple2[]{tuple1, tuple2});
        deleteQuery.where(predicate);
        return entityManager.createQuery(deleteQuery).executeUpdate();
    }

    @Override @SuppressWarnings(value = "unchecked")
    public boolean isPresent(final Tuple2<String, Long> tuple) {
        final CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        final Root<User> root = criteriaQuery.from(User.class);
        final Predicate predicate = createWhereClause(root, new Tuple2[]{tuple});
        criteriaQuery.select(criteriaBuilder.count(root)).where(predicate);
        final TypedQuery<Long> typedQuery = entityManager.createQuery(criteriaQuery);
        return typedQuery.getSingleResult() > 0;
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

    private void beginTransaction(){
        if(!entityManager.getTransaction().isActive()){
            entityManager.getTransaction().begin();
        }
    }
}
