package entities;

import reactor.util.function.Tuple2;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.stream.Stream;

public class GenericRepository<T, K> {

    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;
    private final CriteriaQuery<T> criteriaQuery;
    private final Root<T> root;

    public GenericRepository(Class<T> typeParameterClass, EntityManager entityManager) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
        this.criteriaQuery = criteriaBuilder.createQuery(typeParameterClass);
        this.root = criteriaQuery.from(typeParameterClass);
    }

    protected Stream<T> read(){
        CriteriaQuery<T> all = criteriaQuery.select(root);

        TypedQuery<T> typedQuery = entityManager.createQuery(all);
        return typedQuery.getResultStream();
    }

    protected T read(Tuple2<String, K> tuple2){
        CriteriaQuery<T> builtQuery = criteriaQuery.select(root)
                .where(criteriaBuilder.equal(root.get(tuple2.getT1()), tuple2.getT1()));

        TypedQuery<T> typedQuery = entityManager.createQuery(builtQuery);
        typedQuery.setMaxResults(1);
        return typedQuery.getSingleResult();
    }

    protected T create(T entity){
        return null;
    }

    protected T update(T entity){
        return null;
    }

    protected T delete(T entity){
        return null;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }
}
