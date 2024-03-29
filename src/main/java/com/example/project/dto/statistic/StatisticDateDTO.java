package com.example.project.dto.statistic;

import org.joda.time.LocalDateTime;

import javax.validation.constraints.NotNull;

public class StatisticDateDTO {

    private @NotNull LocalDateTime start;
    private @NotNull LocalDateTime end;

    public StatisticDateDTO(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }
}
