package com.eazybytes.eazyschool.repository;

import com.eazybytes.eazyschool.model.Courses;
import com.eazybytes.eazyschool.model.Person;
import com.eazybytes.eazyschool.model.PersonCourse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonCourseRepository extends JpaRepository<PersonCourse, Long> {

    Optional<PersonCourse> findByPersonAndCourse(Person person, Courses course);
}
