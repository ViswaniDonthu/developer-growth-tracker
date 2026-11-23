package com.viswani.tracker;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface StudyCompletionRepository extends JpaRepository<StudyCompletion, LocalDate> {
    List<StudyCompletion> findByStudyDateBetween(LocalDate from, LocalDate to);
}
