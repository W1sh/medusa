package entities;

import org.junit.Test;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Tuple;

import static org.junit.Assert.*;

public class GenericRepositoryTest {

    @Test
    public void read() {
        GenericRepository<User, Long> genericRepository = new GenericRepository<>(User.class);
        genericRepository.read().forEach(System.out::println);
    }

    @Test
    public void readById() {
        GenericRepository<User, Long> genericRepository = new GenericRepository<>(User.class);
        Tuple2<String, Long> tuple = Tuples.of("id", 1L);
        System.out.println(genericRepository.read(tuple));
    }

    @Test
    public void delete(){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("PersistenceUnit");
        EntityManager em = emf.createEntityManager();
        User user = new User(1L, 1L);
        em.getTransaction().begin();
        em.persist(user);
        em.getTransaction().commit();
        GenericRepository<User, Long> genericRepository = new GenericRepository<>(User.class);
        Tuple2<String, Long> tuple = Tuples.of("discordId", 1L);
        assertEquals(1, genericRepository.delete(tuple));
    }
}