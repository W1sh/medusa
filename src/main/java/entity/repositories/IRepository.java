package entity.repositories;

import java.util.stream.Stream;

public interface IRepository<T, K> {

    Stream<T> read();

    T read(K id);

    void persist(T entity);

    int update(T entity);

    int delete(T entity);
}
