package com.eazybytes.eazyschool.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "request")
@IdClass(RequestId.class)
public class Request extends BaseEntity {

    @Id
    @ManyToOne
    @JoinColumn(name = "person_id", referencedColumnName = "personId", nullable = false)
    private Person person;

    @Id
    @ManyToOne
    @JoinColumn(name = "course_id", referencedColumnName = "courseId", nullable = false)
    private Courses course;

    private String subject;
}
