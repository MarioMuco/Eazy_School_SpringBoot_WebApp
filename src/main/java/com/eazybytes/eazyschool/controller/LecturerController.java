package com.eazybytes.eazyschool.controller;

import com.eazybytes.eazyschool.model.CourseDocument;
import com.eazybytes.eazyschool.model.Courses;
import com.eazybytes.eazyschool.model.Person;
import com.eazybytes.eazyschool.service.CourseDocumentService;
import com.eazybytes.eazyschool.service.CoursesService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Controller
@RequestMapping("lecturer")
public class LecturerController {

    CoursesService coursesService;
    CourseDocumentService courseDocumentService;

    @Autowired
    public LecturerController(CoursesService coursesService,
                              CourseDocumentService courseDocumentService) {

        this.coursesService = coursesService;
        this.courseDocumentService = courseDocumentService;
    }

    @GetMapping("/displayCourses")
    public ModelAndView displayCourses(HttpSession session) {

        Person person = (Person) session.getAttribute("loggedInPerson");
        ModelAndView modelAndView = new ModelAndView("courses_responsible.html");
        modelAndView.addObject("lecturer", person);
        return modelAndView;
    }

    @GetMapping("/viewStudents")
    public ModelAndView viewStudents(@RequestParam int coursesId,
                                     HttpSession session,
                                     @RequestParam(required = false) String error) {

        String errorMessage = null;
        ModelAndView modelAndView = new ModelAndView("course_students_lecturer_mode.html");

        Courses courses = coursesService.takeCourseOnlyWithStudents(coursesId);
        modelAndView.addObject("courses",courses);
        session.setAttribute("courses",courses);

        if(error != null) {
            errorMessage = "Invalid Email entered!!";
            modelAndView.addObject("errorMessage", errorMessage);
        }
        return modelAndView;
    }

    @GetMapping("/viewDocuments")
    public ModelAndView viewDocuments( @RequestParam int coursesId,
                                HttpSession session,
                                @RequestParam(required = false) String error) {

        String errorMessage = null;
        ModelAndView modelAndView = new ModelAndView("course_documents_lecturer_mode.html");

        Courses courses = coursesService.takeCourseById(coursesId);
        modelAndView.addObject("courses", courses);
        session.setAttribute("courses", courses);

        if(error != null) {
            errorMessage = "Invalid Email entered!!";
            modelAndView.addObject("errorMessage", errorMessage);
        }
        return modelAndView;
    }

    @PostMapping("/addDocumentToCourse")
    public ModelAndView addDocumentToCourse(@RequestParam("document") MultipartFile file,
                                            HttpSession session) throws Exception {

        ModelAndView modelAndView = new ModelAndView();
        Courses courses = (Courses) session.getAttribute("courses");

        CourseDocument courseDocument = courseDocumentService.saveDocument(file, courses);
        courses.getDocuments().add(courseDocument);
        session.setAttribute("courses", courses);
        modelAndView.setViewName("redirect:/lecturer/viewDocuments?coursesId=" + courses.getCourseId());
        return modelAndView;
    }

    @GetMapping("/deleteDocumentFromCourse")
    public ModelAndView deleteDocumentFromCourse(@RequestParam int documentId,
                                                 HttpSession session) throws Exception {

        ModelAndView modelAndView = new ModelAndView();
        Courses courses = (Courses) session.getAttribute("courses");

        courses = courseDocumentService.deleteDocument(documentId, courses);
        session.setAttribute("courses", courses);
        modelAndView.setViewName("redirect:/lecturer/viewDocuments?coursesId="
                + courses.getCourseId());
        return modelAndView;
    }
}
