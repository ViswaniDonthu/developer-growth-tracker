package com.viswani.tracker;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "${app.cors.allowed-origin:*}")
public class TrackerController {
    private final StudyCompletionRepository repo;
    private final PlanService plan;

    public TrackerController(StudyCompletionRepository repo, PlanService plan) {
        this.repo = repo; this.plan = plan;
    }

    @GetMapping("/study-days")
    public List<StudyDay> days(@RequestParam(required = false) LocalDate from,
                               @RequestParam(required = false) LocalDate to) {
        LocalDate start = from == null ? plan.getStartDate() : from;
        LocalDate finish = to == null ? plan.getEndDate() : to;
        return plan.getDays(start, finish, repo);
    }

    @PostMapping("/study-days/{date}/complete")
    public StudyDay complete(@PathVariable LocalDate date) {
        StudyDay day = plan.getDays(date, date, repo).stream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Date is outside the study plan"));
        repo.save(new StudyCompletion(date));
        return plan.getDays(date, date, repo).get(0);
    }

    @DeleteMapping("/study-days/{date}/complete")
    public Map<String, String> undo(@PathVariable LocalDate date) {
        repo.deleteById(date);
        return Map.of("message", "Completion removed");
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        LocalDate start = plan.getStartDate();
        LocalDate end = plan.getEndDate();
        List<StudyCompletion> allCompletions = repo.findAll();
        long completed = allCompletions.size();

        List<StudyDay> allDays = plan.getDays(start, end, repo);
        long totalPlanDays = allDays.size();

        // Target schedule vs actual pace:
        long calendarElapsed = java.time.temporal.ChronoUnit.DAYS.between(start, today) + 1;
        if (calendarElapsed < 0) calendarElapsed = 0;
        if (calendarElapsed > totalPlanDays) calendarElapsed = totalPlanDays;

        long missed = Math.max(0, calendarElapsed - completed);
        long completionRate = totalPlanDays == 0 ? 0 : Math.round(completed * 100.0 / totalPlanDays);

        // Daily streaks based on actual completion dates
        Set<LocalDate> activeDays = allCompletions.stream()
                .map(c -> c.getCompletedAt() != null ? c.getCompletedAt().toLocalDate() : c.getStudyDate())
                .collect(Collectors.toSet());

        long streak = 0;
        LocalDate cursor = today;
        if (!activeDays.contains(cursor)) {
            cursor = cursor.minusDays(1);
        }
        while (activeDays.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }

        long longest = 0;
        if (!activeDays.isEmpty()) {
            List<LocalDate> sortedDates = activeDays.stream().sorted().toList();
            long run = 0;
            LocalDate prev = null;
            for (LocalDate d : sortedDates) {
                if (prev == null || d.equals(prev.plusDays(1))) {
                    run++;
                } else if (!d.equals(prev)) {
                    run = 1;
                }
                longest = Math.max(longest, run);
                prev = d;
            }
        }

        Map<String, Long> byDomain = allDays.stream()
                .filter(StudyDay::completed)
                .collect(Collectors.groupingBy(StudyDay::domain, Collectors.counting()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("startDate", start);
        result.put("endDate", end);
        result.put("today", today);
        result.put("completed", completed);
        result.put("missed", missed);
        result.put("elapsed", totalPlanDays);
        result.put("completionRate", completionRate);
        result.put("currentStreak", streak);
        result.put("longestStreak", longest);
        result.put("domainCompletions", byDomain);
        return result;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
