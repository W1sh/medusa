package entities;

import reactor.util.function.Tuple2;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class GenericRepository<T, K> {

    private final Class<T> typeParameterClass;
    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;

    public GenericRepository(Class<T> typeParameterClass) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("PersistenceUnit");

        this.typeParameterClass = typeParameterClass;
        this.entityManager = emf.createEntityManager();
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
        this.entityManager.getTransaction().begin();
    }

    public Stream<T> read(){
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(typeParameterClass);
        Root<T> root = criteriaQuery.from(typeParameterClass);
        criteriaQuery.select(root);
        
        return entityManager.createQuery(criteriaQuery).getResultStream();
    }

    @SafeVarargs
    public final T read(Tuple2<String, K>... tuples){
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(typeParameterClass);
        Root<T> root = criteriaQuery.from(typeParameterClass);
        Predicate predicate = createWhereClause(root, tuples);
        criteriaQuery.select(root).where(predicate);
        TypedQuery<T> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setMaxResults(1);
        return typedQuery.getSingleResult();
    }

    public void persist(T entity){
        entityManager.persist(entity);
    }

    public int update(Tuple2<String, K> tupleUpdate, Tuple2<String, K> tuplePredicate){
        CriteriaUpdate<T> updateQuery = criteriaBuilder.createCriteriaUpdate(typeParameterClass);
        Root<T> root = updateQuery.from(typeParameterClass);
        Predicate predicate = criteriaBuilder.equal(root.get(tuplePredicate.getT1()), tuplePredicate.getT2());
        updateQuery.set(tupleUpdate.getT1(), tupleUpdate.getT2()).where(predicate);
        return entityManager.createQuery(updateQuery).executeUpdate();
    }

    public int delete(Tuple2<String, K> tuple){
        CriteriaDelete<T> deleteQuery = criteriaBuilder.createCriteriaDelete(typeParameterClass);
        Root<T> root = deleteQuery.from(typeParameterClass);
        Predicate predicate = criteriaBuilder.equal(root.get(tuple.getT1()), tuple.getT2());
        deleteQuery.where(predicate);
        return entityManager.createQuery(deleteQuery).executeUpdate();
    }

    public int delete(Tuple2<String, K> tuple1, Tuple2<String, K> tuple2){
        CriteriaDelete<T> deleteQuery = criteriaBuilder.createCriteriaDelete(typeParameterClass);
        Root<T> root = deleteQuery.from(typeParameterClass);
        Predicate predicate = createWhereClause(root, tuple1, tuple2);
        deleteQuery.where(predicate);
        return entityManager.createQuery(deleteQuery).executeUpdate();
    }

    public boolean isPresent(Tuple2<String, K> tuple){
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<T> root = criteriaQuery.from(typeParameterClass);
        Predicate predicate = createWhereClause(root, tuple);
        criteriaQuery.select(criteriaBuilder.count(root)).where(predicate);

        final TypedQuery<Long> typedQuery = entityManager.createQuery(criteriaQuery);
        return typedQuery.getSingleResult() > 0;
    }

    @SafeVarargs
    private final Predicate createWhereClause(Root<T> root, Tuple2<String, K>... tuples){
        if(tuples.length > 1){
            List<Predicate> predicates = new ArrayList<>();
            for(Tuple2<String, K> tuple : tuples){
                Predicate predicate = criteriaBuilder.equal(root.get(tuple.getT1()), tuple.getT2());
                predicates.add(predicate);
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }else{
            return criteriaBuilder.equal(root.get(tuples[0].getT1()), tuples[0].getT2());
        }
    }
}
