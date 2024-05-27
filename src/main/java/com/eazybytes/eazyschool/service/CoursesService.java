package com.eazybytes.eazyschool.service;

import com.eazybytes.eazyschool.model.CourseDocument;
import com.eazybytes.eazyschool.model.Courses;
import com.eazybytes.eazyschool.model.Person;
import com.eazybytes.eazyschool.repository.CoursesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class CoursesService {

    CoursesRepository coursesRepository;

    @Autowired
    public CoursesService(CoursesRepository coursesRepository){
        this.coursesRepository = coursesRepository;
    }

    public Courses takeCourseOnlyWithStudents(int coursesId) {

        Set<Person> students = new HashSet<>();
        Optional<Courses> optionalCourses = coursesRepository.findById(coursesId);

        if (optionalCourses.isPresent()) {

            Courses courses = optionalCourses.get();
            for (Person person : courses.getPersons()) {
                if (person.getRoles().getRoleName().equals("STUDENT")) {
                    students.add(person);
                }
            }
            courses.setPersons(students);
            return courses;
        } else {
            return null;
        }
    }

    public Courses takeCourseOnlyWithLecturers(int coursesId) {

        Set<Person> lecturers = new HashSet<>();
        Optional<Courses> optionalCourses = coursesRepository.findById(coursesId);

        if (optionalCourses.isPresent()) {

            Courses courses = optionalCourses.get();
            for (Person person : courses.getPersons()) {
                if (person.getRoles().getRoleName().equals("LECTURER")) {
                    lecturers.add(person);
                }
            }
            courses.setPersons(lecturers);
            return courses;
        } else {
            return null;
        }
    }

    public Courses takeCourseById(int coursesId) {

        Optional<Courses> optionalCourses = coursesRepository.findById(coursesId);
        Courses courses = optionalCourses.orElse(null);

        courses.getDocuments().sort(Comparator.comparing(CourseDocument::getDocumentName));
        return courses;
    }
}
