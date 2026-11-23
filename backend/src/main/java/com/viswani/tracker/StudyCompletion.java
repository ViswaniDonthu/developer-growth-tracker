package com.viswani.tracker;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "study_completion")
public class StudyCompletion {
    @Id
    private LocalDate studyDate;
    private LocalDateTime completedAt;

    public StudyCompletion() {}
    public StudyCompletion(LocalDate studyDate) {
        this.studyDate = studyDate;
        this.completedAt = LocalDateTime.now();
    }
    public LocalDate getStudyDate() { return studyDate; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
