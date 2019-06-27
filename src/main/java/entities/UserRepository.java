package entities;

import javax.persistence.EntityManager;
import java.util.stream.Stream;

public class UserRepository extends Repository<User, Long> {

    public UserRepository(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public Stream<User> read() {
        return null;
    }

    @Override
    public User read(Long id) {
        return null;
    }

    @Override
    public User create(User entity) {
        return null;
    }

    @Override
    public User update(User entity) {
        return null;
    }

    @Override
    public User delete(User entity) {
        return null;
    }
}
