package entities;

import reactor.util.function.Tuple2;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class GenericRepository<T, K> {

    private final Class<T> typeParameterClass;
    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;
    private final CriteriaQuery<T> criteriaQuery;
    private final CriteriaDelete<T> criteriaDelete;
    private final CriteriaUpdate<T> criteriaUpdate;

    public GenericRepository(Class<T> typeParameterClass) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("PersistenceUnit");

        this.typeParameterClass = typeParameterClass;
        this.entityManager = emf.createEntityManager();;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
        this.criteriaQuery = criteriaBuilder.createQuery(typeParameterClass);
        this.criteriaDelete = criteriaBuilder.createCriteriaDelete(typeParameterClass);
        this.criteriaUpdate = criteriaBuilder.createCriteriaUpdate(typeParameterClass);
        this.entityManager.getTransaction().begin();
    }

    public Stream<T> read(){
        Root<T> root = criteriaQuery.from(typeParameterClass);
        CriteriaQuery<T> all = criteriaQuery.select(root);

        TypedQuery<T> typedQuery = entityManager.createQuery(all);
        return typedQuery.getResultStream();
    }

    public T read(Tuple2<String, K> tuple){
        Root<T> root = criteriaQuery.from(typeParameterClass);
        CriteriaQuery<T> builtQuery = criteriaQuery.select(root)
                .where(criteriaBuilder.equal(root.get(tuple.getT1()), tuple.getT2()));

        TypedQuery<T> typedQuery = entityManager.createQuery(builtQuery);
        typedQuery.setMaxResults(1);
        return typedQuery.getSingleResult();
    }

    public T read(Tuple2<String, K> tuple1, Tuple2<String, K> tuple2){
        Root<T> root = criteriaQuery.from(typeParameterClass);
        Predicate predicate = createWhereClause(root, tuple1, tuple2);
        CriteriaQuery<T> builtQuery = criteriaQuery.select(root)
                .where(predicate);

        TypedQuery<T> typedQuery = entityManager.createQuery(builtQuery);
        typedQuery.setMaxResults(1);
        return typedQuery.getSingleResult();
    }

    public void persist(T entity){
        entityManager.persist(entity);
    }

    public T update(T entity){
        return null;
    }

    public int delete(Tuple2<String, K> tuple){
        Root<T> root = criteriaDelete.from(typeParameterClass);
        Predicate predicate = criteriaBuilder.equal(root.get(tuple.getT1()), tuple.getT2());
        CriteriaDelete<T> deleteQuery = criteriaDelete.where(predicate);

        return entityManager.createQuery(deleteQuery).executeUpdate();
    }

    public int delete(Tuple2<String, K> tuple1, Tuple2<String, K> tuple2){
        Root<T> root = criteriaDelete.from(typeParameterClass);
        Predicate predicate = createWhereClause(root, tuple1, tuple2);
        CriteriaDelete<T> builtQuery = criteriaDelete.where(predicate);

        return entityManager.createQuery(builtQuery).executeUpdate();
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
