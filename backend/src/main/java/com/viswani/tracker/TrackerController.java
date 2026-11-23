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
        LocalDate through = today.isAfter(end) ? end : today;
        List<StudyDay> elapsed = through.isBefore(start) ? List.of() : plan.getDays(start, through, repo);
        long completed = elapsed.stream().filter(StudyDay::completed).count();
        long missed = elapsed.stream().filter(d -> "MISSED".equals(d.status())).count();
        long total = elapsed.size();
        long streak = 0;
        LocalDate cursor = today;
        if (!repo.existsById(cursor)) cursor = cursor.minusDays(1);
        while (!cursor.isBefore(start) && repo.existsById(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        long longest = 0, run = 0;
        Set<LocalDate> doneDates = repo.findByStudyDateBetween(start, end).stream()
                .map(StudyCompletion::getStudyDate).collect(Collectors.toSet());
        for (LocalDate date = start; !date.isAfter(through); date = date.plusDays(1)) {
            if (doneDates.contains(date)) { run++; longest = Math.max(longest, run); }
            else run = 0;
        }
        Map<String, Long> byDomain = elapsed.stream().filter(StudyDay::completed)
                .collect(Collectors.groupingBy(StudyDay::domain, Collectors.counting()));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("startDate", start); result.put("endDate", end); result.put("today", today);
        result.put("completed", completed); result.put("missed", missed);
        result.put("elapsed", total); result.put("completionRate", total == 0 ? 0 : Math.round(completed * 100.0 / total));
        result.put("currentStreak", streak); result.put("longestStreak", longest);
        result.put("domainCompletions", byDomain);
        return result;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
