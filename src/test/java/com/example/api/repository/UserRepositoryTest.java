package com.example.api.repository;

import com.example.api.entity.School;
import com.example.api.entity.User;
import com.example.api.entity.enums.AuthType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void testSaveAndFindById() {
        UUID schoolUuid = UUID.randomUUID();
        School school = new School();
        school.setId(schoolUuid);
        school.setName("Ajou");
        entityManager.persist(school);

        UUID randUUID = UUID.randomUUID();
        User user = new User();
        user.setId(randUUID);
        user.setSchool(school);
        user.setName("Hyunjun");
        user.setEmail("test@example.com");
        user.setAuthType(AuthType.email);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        Optional<User> found = userRepository.findById(randUUID);

        assertTrue(found.isPresent());
        assertEquals("Hyunjun", found.get().getName());
    }
    /*
    @Test
    void testDelete() {
        UUID randUUID = UUID.randomUUID();
        User user = new User();
        user.setId(randUUID);
        user.setName("Yoon");
        userRepository.save(user);

        userRepository.deleteById(randUUID);
        Optional<User> found = userRepository.findById(randUUID);

        assertFalse(found.isPresent());
    } */
}
