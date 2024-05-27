package com.eazybytes.eazyschool.controller;

import com.eazybytes.eazyschool.model.Courses;
import com.eazybytes.eazyschool.model.Person;
import com.eazybytes.eazyschool.model.PersonCourse;
import com.eazybytes.eazyschool.repository.CoursesRepository;
import com.eazybytes.eazyschool.repository.PersonCourseRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class CourseController {

    private final CoursesRepository coursesRepository;
    private final PersonCourseRepository personCourseRepository;

    @Autowired
    public CourseController(CoursesRepository coursesRepository,
                            PersonCourseRepository personCourseRepository) {

        this.coursesRepository = coursesRepository;
        this.personCourseRepository = personCourseRepository;
    }

    //===================================================================
    @GetMapping("/courses")
    public ModelAndView displayCourses(HttpSession session) {

        List<Courses> courses = coursesRepository.findAll(Sort.by("name").descending());

        Map<Integer, String> courseAverageRatings = calculateRating(courses);

        ModelAndView modelAndView = new ModelAndView("courses.html");
        modelAndView.addObject("courses", courses);
        modelAndView.addObject("courseAverageRatings", courseAverageRatings);
        return modelAndView;
    }

    //======================================================================
    @GetMapping("/course_description/{name}")
    public ModelAndView displayCourseDescription(@PathVariable("name") String name,
                                                 HttpSession session) {

        Optional<Courses> optionalCourse = coursesRepository.findByName(name);
        if (optionalCourse.isPresent()) {
            Courses course = optionalCourse.get();
            ModelAndView modelAndView = new ModelAndView("course_description.html");
            modelAndView.addObject("course", course);
            modelAndView.addObject("documents", course.getDocuments());
            Person person = (Person) session.getAttribute("loggedInPerson");

            boolean hasRated = hasPersonRated(person, course);
            boolean isEnrolled = hasPersonEnrolled(person, course);
            int defaultRating = getDefaultRating(person, course);

            modelAndView.addObject("hasRated", hasRated);
            modelAndView.addObject("isEnrolled", isEnrolled);
            modelAndView.addObject("disabled", hasRated ? "disabled" : "");
            modelAndView.addObject("defaultRating", defaultRating);

            String action = determineAction(person, course);
            modelAndView.addObject("action", action);

            return modelAndView;
        } else {
            return new ModelAndView("error.html");
        }
    }

    // ----------------------------------------------------------------------------------------------

    private Map<Integer, String> calculateRating(List<Courses> courses) {
        Map<Integer, String> courseRatings = new HashMap<>();

        for (Courses course : courses) {
            int courseId = course.getCourseId();
            double totalRating = 0.0;
            int ratingCount = 0;

            for (PersonCourse personCourse : course.getPersonCourses()) {
                Integer rating = personCourse.getRating();
                if (rating != null) {
                    totalRating += rating;
                    ratingCount++;
                }
            }

            double averageRating = (ratingCount > 0) ? totalRating / ratingCount : 0;
            String formattedAverage = String.format("%.1f", averageRating);
            courseRatings.put(courseId, formattedAverage);
        }

        return courseRatings;
    }


    private boolean hasPersonEnrolled(Person person, Courses course) {

        if (person != null) {
            Optional<PersonCourse> existingEnrollment = personCourseRepository.findByPersonAndCourse(person, course);
            return existingEnrollment.isPresent();
        }
        return false;
    }

    private boolean hasPersonRated(Person person, Courses course) {

        if (person != null) {
            Optional<PersonCourse> existingRating = personCourseRepository.findByPersonAndCourse(person, course);
            return existingRating.isPresent();
        }
        return false;
    }

    private int getDefaultRating(Person person, Courses course) {

        if (person != null) {
            Optional<PersonCourse> existingRating = personCourseRepository.findByPersonAndCourse(person, course);
            if (existingRating.isPresent()) {
                return existingRating.get().getRating();
            }
        }
        return 0;
    }

    private String determineAction(Person person, Courses course) {

        if (person != null) {
            boolean hasEnrollRequest = person.getRequests().stream()
                    .anyMatch(request -> request.getCourse().equals(course) &&
                            request.getSubject().equals("I want to enroll in this course."));
            boolean hasUnenrollRequest = person.getRequests().stream()
                    .anyMatch(request -> request.getCourse().equals(course) &&
                            request.getSubject().equals("I want to unenroll from this course."));

            if (hasEnrollRequest) {
                return "Cancel Enrollment";
            } else if (hasUnenrollRequest) {
                return "Cancel Unenrollment";
            } else {
                boolean isEnrolled = person.getCourses().contains(course);
                return isEnrolled ? "Unenroll" : "Enroll";
            }
        }
        return "Enroll";
    }
}
