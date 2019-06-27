package entities;

import javax.persistence.EntityManager;
import java.util.stream.Stream;

public abstract class Repository<T, K> {

    private EntityManager entityManager;

    public Repository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    abstract Stream<T> read();

    abstract T read(K id);

    abstract T create(T entity);

    abstract T update(T entity);

    abstract T delete(T entity);

    public EntityManager getEntityManager() {
        return entityManager;
    }
}
