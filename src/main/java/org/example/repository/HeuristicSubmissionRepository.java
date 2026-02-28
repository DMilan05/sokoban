package org.example.repository;

import org.example.model.HeuristicSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HeuristicSubmissionRepository extends JpaRepository<HeuristicSubmission, Long> {
    // Ez fogja kiszolgálni a ranglistát: növekvő sorrendbe rendezi a beküldéseket a lépésszám alapján
    List<HeuristicSubmission> findAllByOrderByStepsToSolveAsc();
}