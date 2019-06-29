package entities;

import reactor.util.function.Tuple2;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.stream.Stream;

public class GenericRepository<T, K> {

    private final Class<T> typeParameterClass;
    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;
    private final CriteriaQuery<T> criteriaQuery;
    private final CriteriaDelete<T> criteriaDelete;
    private final CriteriaUpdate<T> criteriaUpdate;

    public GenericRepository(Class<T> typeParameterClass, EntityManager entityManager) {
        if(!entityManager.getTransaction().isActive()){
            entityManager.getTransaction().begin();
        }
        this.typeParameterClass = typeParameterClass;
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
        this.criteriaQuery = criteriaBuilder.createQuery(typeParameterClass);
        this.criteriaDelete = criteriaBuilder.createCriteriaDelete(typeParameterClass);
        this.criteriaUpdate = criteriaBuilder.createCriteriaUpdate(typeParameterClass);

    }

    protected Stream<T> read(){
        Root<T> root = criteriaQuery.from(typeParameterClass);
        CriteriaQuery<T> all = criteriaQuery.select(root);

        TypedQuery<T> typedQuery = entityManager.createQuery(all);
        return typedQuery.getResultStream();
    }

    protected T read(Tuple2<String, K> tuple){
        Root<T> root = criteriaQuery.from(typeParameterClass);
        CriteriaQuery<T> builtQuery = criteriaQuery.select(root)
                .where(criteriaBuilder.equal(root.get(tuple.getT1()), tuple.getT2()));

        TypedQuery<T> typedQuery = entityManager.createQuery(builtQuery);
        typedQuery.setMaxResults(1);
        return typedQuery.getSingleResult();
    }

    protected void persist(T entity){
        entityManager.persist(entity);
    }

    protected T update(T entity){
        return null;
    }

    protected int delete(Tuple2<String, K> tuple){
        Root<T> root = criteriaDelete.from(typeParameterClass);
        Predicate predicate = criteriaBuilder.equal(root.get(tuple.getT1()), tuple.getT2());
        CriteriaDelete<T> deleteQuery = criteriaDelete.where(predicate);

        return entityManager.createQuery(deleteQuery).executeUpdate();
    }

    protected int delete(Tuple2<String, K> tuple1, Tuple2<String, K> tuple2){
        Root<T> root = criteriaDelete.from(typeParameterClass);
        Predicate predicate1 = criteriaBuilder.equal(root.get(tuple1.getT1()), tuple1.getT2());
        Predicate predicate2 = criteriaBuilder.equal(root.get(tuple2.getT1()), tuple2.getT2());
        Predicate finalPredicate = criteriaBuilder.and(predicate1, predicate2);
        CriteriaDelete<T> builtQuery = criteriaDelete.where(finalPredicate);

        return entityManager.createQuery(builtQuery).executeUpdate();
    }
}
