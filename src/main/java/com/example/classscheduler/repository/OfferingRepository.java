package com.example.classscheduler.repository;

import com.example.classscheduler.entity.Offering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferingRepository extends JpaRepository<Offering, Long> {

    @Query("SELECT o FROM Offering o JOIN FETCH o.course JOIN FETCH o.teacher")
    List<Offering> findAllWithCourseAndTeacher();
}
