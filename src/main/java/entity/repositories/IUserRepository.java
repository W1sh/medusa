package entity.repositories;

import entity.entities.User;
import reactor.util.function.Tuple2;

public interface IUserRepository extends IRepository<User, Long> {

    User read(Tuple2<String, Long>[] tuples);

    int update(Tuple2<String, Long> tupleUpdate, Tuple2<String, Long> tuplePredicate);

    int delete(Tuple2<String, Long> tuple);

    int delete(Tuple2<String, Long> tuple1, Tuple2<String, Long> tuple2);

    boolean isPresent(Tuple2<String, Long> tuple);
}
