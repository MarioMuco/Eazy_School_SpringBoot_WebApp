package com.eazybytes.eazyschool.controller;

import com.eazybytes.eazyschool.model.Courses;
import com.eazybytes.eazyschool.model.PersonCourse;
import com.eazybytes.eazyschool.repository.CoursesRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final CoursesRepository coursesRepository;

    @Autowired
    public HomeController(CoursesRepository coursesRepository) {
        this.coursesRepository = coursesRepository;
    }

    @RequestMapping(value={"", "/", "home"})
    public ModelAndView displayHomePage(HttpSession session) {

        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by("name").descending());
        List<Courses> courses = coursesRepository.findAll(pageRequest).getContent();

        Map<Integer, String> courseAverageRatings = calculateRating(courses);

        ModelAndView modelAndView = new ModelAndView("home.html");
        modelAndView.addObject("courses",courses);
        modelAndView.addObject("courseAverageRatings", courseAverageRatings);
        return modelAndView;
    }

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
}
