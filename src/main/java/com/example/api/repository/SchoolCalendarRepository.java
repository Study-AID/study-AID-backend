package com.example.api.repository;

import com.example.api.entity.SchoolCalendar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SchoolCalendarRepository extends JpaRepository<SchoolCalendar, UUID> {

}
