package entities;

import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.junit.Assert.*;

public class GenericRepositoryTest {

    @Test
    public void read() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("PersistenceUnit");
        EntityManager em = emf.createEntityManager();
        User user = new User();
        GenericRepository<User, Long> genericRepository = new GenericRepository<>(User.class, em);
        genericRepository.read().forEach(System.out::println);
    }
}