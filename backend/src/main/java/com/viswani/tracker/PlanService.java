package com.viswani.tracker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlanService {
    private final List<Map<String, Object>> plan;

    public PlanService(ObjectMapper mapper) throws Exception {
        try (InputStream in = new ClassPathResource("study-plan.json").getInputStream()) {
            this.plan = mapper.readValue(in, new TypeReference<>() {});
        }
    }

    public List<StudyDay> getDays(LocalDate from, LocalDate to, StudyCompletionRepository repo) {
        Map<LocalDate, StudyCompletion> completed = repo.findByStudyDateBetween(from, to).stream()
                .collect(Collectors.toMap(StudyCompletion::getStudyDate, x -> x));
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        return plan.stream().map(row -> {
            LocalDate date = LocalDate.parse((String) row.get("date"));
            if (date.isBefore(from) || date.isAfter(to)) return null;
            StudyCompletion c = completed.get(date);
            boolean done = c != null;
            String status = done ? "COMPLETED" : (date.isBefore(today) ? "MISSED" : (date.equals(today) ? "TODAY" : "UPCOMING"));
            return new StudyDay(date, (String) row.get("phase"), (String) row.get("domain"),
                    (String) row.get("topic"), (String) row.get("detail"),
                    ((Number) row.get("minutes")).intValue(), ((Number) row.get("week")).intValue(),
                    done, status, (done && c.getCompletedAt() != null) ? c.getCompletedAt().toString() : null);
        }).filter(Objects::nonNull).toList();
    }

    public LocalDate getStartDate() { return LocalDate.parse((String) plan.get(0).get("date")); }
    public LocalDate getEndDate() { return LocalDate.parse((String) plan.get(plan.size()-1).get("date")); }
}
