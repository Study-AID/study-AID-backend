package com.example.api.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.example.api.entity.School;
import com.example.api.entity.SchoolCalendar;

import jakarta.persistence.EntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class SchoolCalendarRepositoryTest {
    @Autowired
    private SchoolCalendarRepository schoolCalendarRepository;

    @Autowired
    private EntityManager entityManager;

    void saveAndFindSchoolCalendarRepositoryTest() {
        UUID schoolUUID = UUID.randomUUID();
        School school = new School();
        school.setId(schoolUUID);
        school.setName("Ajou");

        entityManager.persist(school);

        UUID schoolCalendarUUID = UUID.randomUUID();
        SchoolCalendar schoolCalendar = new SchoolCalendar();
        schoolCalendar.setId(schoolCalendarUUID);
        schoolCalendar.setSchool(school);
        schoolCalendar.setYear(2025);

        schoolCalendarRepository.save(schoolCalendar);
        Optional<SchoolCalendar> found = schoolCalendarRepository.findById(schoolCalendarUUID);

        assertTrue(found.isPresent());
    }
}
