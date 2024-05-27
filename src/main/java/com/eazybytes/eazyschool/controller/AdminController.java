package com.eazybytes.eazyschool.controller;

import com.eazybytes.eazyschool.model.*;
import com.eazybytes.eazyschool.repository.CoursesRepository;
import com.eazybytes.eazyschool.repository.EazyClassRepository;
import com.eazybytes.eazyschool.repository.PersonRepository;
import com.eazybytes.eazyschool.repository.RequestRepository;
import com.eazybytes.eazyschool.service.CoursesService;
import com.eazybytes.eazyschool.service.PersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("admin")
public class AdminController {

    EazyClassRepository eazyClassRepository;
    PersonRepository personRepository;
    CoursesRepository coursesRepository;
    CoursesService coursesService;
    PersonService personService;
    RequestRepository requestRepository;
    @Autowired
    public AdminController(EazyClassRepository eazyClassRepository,
                           PersonRepository personRepository,
                           CoursesRepository coursesRepository,
                           RequestRepository requestRepository,
                           CoursesService coursesService,
                           PersonService personService) {

        this.eazyClassRepository = eazyClassRepository;
        this.personRepository = personRepository;
        this.coursesRepository = coursesRepository;
        this.requestRepository = requestRepository;
        this.coursesService = coursesService;
        this.personService = personService;
    }

    @RequestMapping("/displayClasses")
    public ModelAndView displayClasses() {

        List<EazyClass> eazyClasses = eazyClassRepository.findAll();
        ModelAndView modelAndView = new ModelAndView("classes.html");
        modelAndView.addObject("eazyClasses",eazyClasses);
        modelAndView.addObject("eazyClass", new EazyClass());
        return modelAndView;
    }

    @PostMapping("/addNewClass")
    public ModelAndView addNewClass(@ModelAttribute("eazyClass") EazyClass eazyClass) {

        eazyClassRepository.save(eazyClass);
        ModelAndView modelAndView = new ModelAndView("redirect:/admin/displayClasses");
        return modelAndView;
    }

    @RequestMapping("/deleteClass")
    public ModelAndView deleteClass(@RequestParam int id) {

        Optional<EazyClass> eazyClass = eazyClassRepository.findById(id);
        for(Person person : eazyClass.get().getPersons()){
            person.setEazyClass(null);
            personRepository.save(person);
        }
        eazyClassRepository.deleteById(id);
        ModelAndView modelAndView = new ModelAndView("redirect:/admin/displayClasses");
        return modelAndView;
    }

    @GetMapping("/displayStudents")
    public ModelAndView displayStudents(@RequestParam int classId, HttpSession session,
                                        @RequestParam(value = "error", required = false) String error) {

        String errorMessage = null;
        ModelAndView modelAndView = new ModelAndView("students.html");
        Optional<EazyClass> eazyClass = eazyClassRepository.findById(classId);
        modelAndView.addObject("eazyClass",eazyClass.get());
        modelAndView.addObject("person",new Person());
        session.setAttribute("eazyClass",eazyClass.get());
        if(error != null) {
            errorMessage = "Invalid Email entered!!";
            modelAndView.addObject("errorMessage", errorMessage);
        }
        return modelAndView;
    }

    @PostMapping("/addStudent")
    public ModelAndView addStudent(@ModelAttribute("person") Person person, HttpSession session) {

        ModelAndView modelAndView = new ModelAndView();
        EazyClass eazyClass = (EazyClass) session.getAttribute("eazyClass");
        Person personEntity = personRepository.readByEmail(person.getEmail());
        if(personEntity==null || !(personEntity.getPersonId()>0)){
            modelAndView.setViewName("redirect:/admin/displayStudents?classId="+eazyClass.getClassId()
                    +"&error=true");
            return modelAndView;
        }
        personEntity.setEazyClass(eazyClass);
        personRepository.save(personEntity);
        eazyClass.getPersons().add(personEntity);
        eazyClassRepository.save(eazyClass);
        modelAndView.setViewName("redirect:/admin/displayStudents?classId="+eazyClass.getClassId());
        return modelAndView;
    }

    @GetMapping("/deleteStudent")
    public ModelAndView deleteStudent(@RequestParam int personId, HttpSession session) {

        EazyClass eazyClass = (EazyClass) session.getAttribute("eazyClass");
        Optional<Person> person = personRepository.findById(personId);
        person.get().setEazyClass(null);
        eazyClass.getPersons().remove(person.get());
        EazyClass eazyClassSaved = eazyClassRepository.save(eazyClass);
        session.setAttribute("eazyClass",eazyClassSaved);
        ModelAndView modelAndView = new ModelAndView("redirect:/admin/displayStudents?classId="+eazyClass.getClassId());
        return modelAndView;
    }

    //======================================================================
    @GetMapping("/displayCourses")
    public ModelAndView displayCourses() {

        List<Courses> courses = coursesRepository.findAll(Sort.by("name").descending());
        ModelAndView modelAndView = new ModelAndView("courses_secure.html");
        modelAndView.addObject("courses",courses);
        modelAndView.addObject("course", new Courses());
        return modelAndView;
    }

    @PostMapping("/addNewCourse")
    public ModelAndView addNewCourse(@ModelAttribute("course") Courses course) {

        ModelAndView modelAndView = new ModelAndView();
        coursesRepository.save(course);
        modelAndView.setViewName("redirect:/admin/displayCourses");
        return modelAndView;
    }
//=========================================================================
    @GetMapping("/viewStudents")
    public ModelAndView viewStudents(@RequestParam int coursesId,
                                     HttpSession session,
                                     @RequestParam(required = false) String error) {

        String errorMessage = null;
        ModelAndView modelAndView = new ModelAndView("course_students.html");

        Courses courses = coursesService.takeCourseOnlyWithStudents(coursesId);
        modelAndView.addObject("courses", courses);
        modelAndView.addObject("person", new Person());
        session.setAttribute("courses", courses);

        if(error != null) {
            errorMessage = "Invalid Email entered!!";
            modelAndView.addObject("errorMessage", errorMessage);
        }
        return modelAndView;
    }

    @PostMapping("/addStudentToCourse")
    public ModelAndView addStudentToCourse(@ModelAttribute("person") Person person,
                                           HttpSession session) {

        ModelAndView modelAndView = new ModelAndView();
        Courses courses = (Courses) session.getAttribute("courses");
        Person personEntity = personRepository.readByEmail(person.getEmail());
        if(personEntity == null || !(personEntity.getPersonId() > 0)){
            modelAndView.setViewName("redirect:/admin/viewStudents?coursesId="
                    + courses.getCourseId() + "&error=true");
            return modelAndView;
        }
        personEntity.getCourses().add(courses);
        courses.getPersons().add(personEntity);
        personRepository.save(personEntity);
        session.setAttribute("courses",courses);
        modelAndView.setViewName("redirect:/admin/viewStudents?coursesId="
                + courses.getCourseId());
        return modelAndView;
    }

    @GetMapping("/deleteStudentFromCourse")
    public ModelAndView deleteStudentFromCourse(@RequestParam int personId,
                                                HttpSession session) {

        Courses courses = (Courses) session.getAttribute("courses");
        Optional<Person> person = personRepository.findById(personId);
        person.get().getCourses().remove(courses);
        courses.getPersons().remove(person);
        personRepository.save(person.get());
        session.setAttribute("courses",courses);
        ModelAndView modelAndView = new ModelAndView("redirect:/admin/viewStudents?coursesId="
                + courses.getCourseId());
        return modelAndView;
    }

    // ***************************************************************************************************

    @GetMapping("/viewLecturers")
    public ModelAndView viewLecturers(@RequestParam int coursesId,
                                      HttpSession session,
                                      @RequestParam(required = false) String error) {

        String errorMessage = null;
        ModelAndView modelAndView = new ModelAndView("course_lecturers.html");

        Courses courses = coursesService.takeCourseOnlyWithLecturers(coursesId);
        modelAndView.addObject("courses", courses);
        modelAndView.addObject("person", new Person());
        session.setAttribute("courses", courses);

        if(error != null) {
            errorMessage = "Invalid Email entered!!";
            modelAndView.addObject("errorMessage", errorMessage);
        }
        return modelAndView;
    }

    @PostMapping("/addLecturerToCourse")
    public ModelAndView addLecturerToCourse(@ModelAttribute("person") Person person,
                                            HttpSession session) {

        ModelAndView modelAndView = new ModelAndView();
        Courses courses = (Courses) session.getAttribute("courses");
        Person personEntity = personRepository.readByEmail(person.getEmail());
        if(personEntity == null || !(personEntity.getPersonId() > 0)){
            modelAndView.setViewName("redirect:/admin/viewLecturers?courseId="
                    + courses.getCourseId() + "&error=true");
            return modelAndView;
        }
        personEntity.getCourses().add(courses);
        courses.getPersons().add(personEntity);
        personRepository.save(personEntity);
        session.setAttribute("courses",courses);
        modelAndView.setViewName("redirect:/admin/viewLecturers?coursesId="
                + courses.getCourseId());
        return modelAndView;
    }

    @GetMapping("/deleteLecturerFromCourse")
    public ModelAndView deleteLecturerFromCourse(@RequestParam int personId,
                                                 HttpSession session) {

        Courses courses = (Courses) session.getAttribute("courses");
        Optional<Person> person = personRepository.findById(personId);
        person.get().getCourses().remove(courses);
        courses.getPersons().remove(person);
        personRepository.save(person.get());
        session.setAttribute("courses",courses);
        ModelAndView modelAndView = new
                ModelAndView("redirect:/admin/viewLecturers?coursesId="
                + courses.getCourseId());
        return modelAndView;
    }

    @RequestMapping("/displayLecturers")
    public ModelAndView displayLecturers() {

        List<Person> lecturers = personService.getAllLecturers();
        ModelAndView modelAndView = new ModelAndView("lecturers.html");
        modelAndView.addObject("lecturers", lecturers);
        modelAndView.addObject("person", new Person());
        return modelAndView;
    }

    @PostMapping("/addNewLecturer")
    public ModelAndView addNewLecturer(@ModelAttribute("person") Person person) {

        ModelAndView modelAndView = new ModelAndView();
        Person personEntity = personService.createNewLecturer(person);
        if(personEntity == null || !(personEntity.getPersonId() > 0)){
            modelAndView.setViewName("redirect:/admin/displayLecturers?error=true");
            return modelAndView;
        }
        modelAndView.setViewName("redirect:/admin/displayLecturers");
        return modelAndView;
    }

    @GetMapping("/displayRequests")
    public ModelAndView displayRequests() {

        ModelAndView modelAndView = new ModelAndView();
        List<Request> requests = requestRepository.findAll();
        modelAndView.setViewName("course_registration_requests.html");
        modelAndView.addObject("requests", requests);
        return modelAndView;
    }

    @GetMapping("/registerStudentToCourse")
    public ModelAndView addStudentToCourse(@RequestParam int studentId,
                                           @RequestParam int courseId) {

        ModelAndView modelAndView = new ModelAndView();

        Optional<Person> optionalStudent = personRepository.findById(studentId);
        Optional<Courses> optionalCourses = coursesRepository.findById(courseId);
        RequestId requestId = new RequestId(optionalStudent.get().getPersonId(), optionalCourses.get().getCourseId());
        Optional<Request> optionalRequest = requestRepository.findById(requestId);

        optionalStudent.get().getCourses().add(optionalCourses.get());
        optionalStudent.get().getRequests().remove(optionalRequest.get());
        personRepository.save(optionalStudent.get());

        modelAndView.setViewName("redirect:/admin/displayRequests");
        return modelAndView;
    }

    @GetMapping("/unregisterStudentFromCourse")
    public ModelAndView deleteStudentFromCourse(@RequestParam int studentId,
                                                @RequestParam int courseId) {

        Optional<Person> optionalStudent = personRepository.findById(studentId);
        Optional<Courses> optionalCourses = coursesRepository.findById(courseId);
        RequestId requestId = new RequestId(optionalStudent.get().getPersonId(), optionalCourses.get().getCourseId());
        Optional<Request> optionalRequest = requestRepository.findById(requestId);

        optionalStudent.get().getCourses().remove(optionalCourses.get());
        optionalStudent.get().getRequests().remove(optionalRequest.get());
        personRepository.save(optionalStudent.get());

        ModelAndView modelAndView = new ModelAndView("redirect:/admin/displayRequests");
        return modelAndView;
    }
}
