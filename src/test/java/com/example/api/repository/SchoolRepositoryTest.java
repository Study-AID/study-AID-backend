package com.example.api.repository;

import com.example.api.entity.School;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class SchoolRepositoryTest {
    @Autowired
    private SchoolRepository schoolRepository;

    @Test
    void saveAndFindSchoolRepositoryTest() {
        UUID schoolUUID = UUID.randomUUID();
        School school = new School();
        school.setId(schoolUUID);
        school.setName("Ajou");

        schoolRepository.save(school);
        Optional<School> found = schoolRepository.findById(schoolUUID);

        assertTrue(found.isPresent());
    }
}
