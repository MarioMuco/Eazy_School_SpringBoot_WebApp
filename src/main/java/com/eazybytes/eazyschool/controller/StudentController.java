package com.eazybytes.eazyschool.controller;

import com.eazybytes.eazyschool.model.*;
import com.eazybytes.eazyschool.repository.CourseDocumentRepository;
import com.eazybytes.eazyschool.repository.CoursesRepository;
import com.eazybytes.eazyschool.repository.PersonCourseRepository;
import com.eazybytes.eazyschool.repository.RequestRepository;
import com.eazybytes.eazyschool.service.CourseDocumentService;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("student")
public class StudentController {

    private final RequestRepository requestRepository;
    private final CoursesRepository coursesRepository;
    private final PersonCourseRepository personCourseRepository;

    private final CourseDocumentRepository courseDocumentRepository;

    private final CourseDocumentService courseDocumentService;

    @Autowired
    public StudentController(RequestRepository requestRepository,
                             CoursesRepository coursesRepository,
                             PersonCourseRepository personCourseRepository,
                             CourseDocumentRepository courseDocumentRepository,
                             CourseDocumentService courseDocumentService) {

        this.requestRepository = requestRepository;
        this.coursesRepository = coursesRepository;
        this.personCourseRepository = personCourseRepository;
        this.courseDocumentRepository = courseDocumentRepository;
        this.courseDocumentService = courseDocumentService;
    }





    @GetMapping("/displayCourses")
    public ModelAndView displayCourses(HttpSession session) {

        Person person = (Person) session.getAttribute("loggedInPerson");
        ModelAndView modelAndView = new ModelAndView("courses_enrolled.html");
        modelAndView.addObject("person",person);
        return modelAndView;
    }

    @PostMapping("/enroll")
    public ModelAndView enroll(@RequestParam String courseName, HttpSession session) {

        Person person = (Person) session.getAttribute("loggedInPerson");
        Optional<Courses> optionalCourse = coursesRepository.findByName(courseName);
        String redirectUrl = "redirect:/course_description/" + courseName;

        if (person != null && optionalCourse.isPresent()) {
            Courses course = optionalCourse.get();
            Request request = new Request();
            request.setPerson(person);
            request.setCourse(course);
            request.setSubject("I want to enroll in this course.");
            person.getRequests().add(request);
            requestRepository.save(request);
            session.setAttribute("loggedInPerson", person);
        }

        return new ModelAndView(redirectUrl);
    }

    @PostMapping("/unenroll")
    public ModelAndView cancelEnrollment(@RequestParam String courseName, HttpSession session) {

        Person person = (Person) session.getAttribute("loggedInPerson");
        Optional<Courses> optionalCourse = coursesRepository.findByName(courseName);
        String redirectUrl = "redirect:/course_description/" + courseName;

        if (person != null && optionalCourse.isPresent()) {
            Courses course = optionalCourse.get();
            Request request = new Request();
            request.setPerson(person);
            request.setCourse(course);
            request.setSubject("I want to unenroll from this course.");
            person.getRequests().add(request);
            requestRepository.save(request);
            session.setAttribute("loggedInPerson", person);
        }

        return new ModelAndView(redirectUrl);
    }

    @Transactional
    @PostMapping("/cancel_enrollment")
    public ModelAndView unenroll(@RequestParam String courseName, HttpSession session) {

        Person person = (Person) session.getAttribute("loggedInPerson");
        Optional<Courses> optionalCourse = coursesRepository.findByName(courseName);
        String redirectUrl = "redirect:/course_description/" + courseName;

        if (person != null && optionalCourse.isPresent()) {
            Courses course = optionalCourse.get();
            String subject = "I want to enroll in this course.";

            requestRepository.deleteByPersonAndCourseAndSubject(person.getPersonId(), course.getCourseId(), subject);
            person.getRequests().removeIf(request ->
                    request.getCourse().equals(course) && request.getSubject().equals(subject));
            session.setAttribute("loggedInPerson", person);
        }

        return new ModelAndView(redirectUrl);
    }

    @Transactional
    @PostMapping("/cancel_unenrollment")
    public ModelAndView cancel_unenrollment(@RequestParam String courseName,
                                            HttpSession session) {

        Person person = (Person) session.getAttribute("loggedInPerson");
        Optional<Courses> optionalCourse = coursesRepository.findByName(courseName);
        String redirectUrl = "redirect:/course_description/" + courseName;

        if (person != null && optionalCourse.isPresent()) {
            Courses course = optionalCourse.get();
            String subject = "I want to unenroll from this course.";

            requestRepository.deleteByPersonAndCourseAndSubject(person.getPersonId(), course.getCourseId(), subject);
            person.getRequests().removeIf(request ->
                    request.getCourse().equals(course) && request.getSubject().equals(subject));
            session.setAttribute("loggedInPerson", person);
        }

        return new ModelAndView(redirectUrl);
    }

    //===================================================================
    @PostMapping("/save_rating")
    public ModelAndView saveRating(@RequestParam String courseName,
                                   @RequestParam String rating,
                                   HttpSession session) {

        Person person = (Person) session.getAttribute("loggedInPerson");
        Optional<Courses> optionalCourse = coursesRepository.findByName(courseName);
        String redirectUrl = "redirect:/course_description/" + courseName;

        if (person != null && optionalCourse.isPresent()) {
            Courses course = optionalCourse.get();
            int ratingValue = Integer.parseInt(rating);

            Optional<PersonCourse> existingRating = personCourseRepository.findByPersonAndCourse(person, course);
            if (existingRating.isPresent()) {
                return new ModelAndView(redirectUrl);
            } else {
                PersonCourse personCourse = new PersonCourse();
                personCourse.setPerson(person);
                personCourse.setCourse(course);
                personCourse.setRating(ratingValue);
                personCourseRepository.save(personCourse);
            }
        }

        return new ModelAndView(redirectUrl);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable int id) {
        CourseDocument document = courseDocumentService.getDocumentById(id);

        if (document == null) {
            return ResponseEntity.notFound().build();
        }

        courseDocumentService.incrementDownloadCount(id);

        Path path = Paths.get("src/main/resources/static/media/pdfs/" + document.getDocumentPath());
        Resource resource;
        try {
            resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/pdf"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getDocumentName() + "\"")
                .body(resource);
    }


}
