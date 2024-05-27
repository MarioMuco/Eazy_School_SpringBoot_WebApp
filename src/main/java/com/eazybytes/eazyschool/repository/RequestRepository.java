package com.eazybytes.eazyschool.repository;

import com.eazybytes.eazyschool.model.Request;
import com.eazybytes.eazyschool.model.RequestId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface RequestRepository extends JpaRepository<Request, RequestId> {

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM request WHERE person_id = :personId AND course_id = :courseId AND subject = :subject", nativeQuery = true)
    void deleteByPersonAndCourseAndSubject(@Param("personId") int personId, @Param("courseId") int courseId, @Param("subject") String subject);

}

