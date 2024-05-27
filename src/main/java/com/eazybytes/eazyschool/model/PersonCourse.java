package com.eazybytes.eazyschool.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "person_courses")
@IdClass(RequestId.class)
public class PersonCourse {

    @Id
    @ManyToOne
    @JoinColumn(name = "person_id", referencedColumnName = "personId", nullable = false)
    private Person person;

    @Id
    @ManyToOne
    @JoinColumn(name = "course_id", referencedColumnName = "courseId", nullable = false)
    private Courses course;

    private Integer rating;
}
