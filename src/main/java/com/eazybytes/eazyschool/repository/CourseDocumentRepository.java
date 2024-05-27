package com.eazybytes.eazyschool.repository;

import com.eazybytes.eazyschool.model.CourseDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseDocumentRepository extends JpaRepository<CourseDocument, Integer> {
}