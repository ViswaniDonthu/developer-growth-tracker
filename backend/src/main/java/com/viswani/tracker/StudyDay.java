package com.viswani.tracker;

import java.time.LocalDate;

public record StudyDay(LocalDate date, String phase, String domain, String topic,
                      String detail, int minutes, int week, boolean completed,
                      String status, String completedAt) {}
