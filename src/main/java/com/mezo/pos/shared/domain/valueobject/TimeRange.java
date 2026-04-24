package com.mezo.pos.shared.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class TimeRange {

    private final LocalDateTime from;
    private final LocalDateTime to;
    private final String timeParam;

    public static TimeRange ofDay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return new TimeRange(startOfDay, now, "DAY");
    }

    public static TimeRange ofWeek() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7).with(LocalTime.MIN);
        return new TimeRange(sevenDaysAgo, now, "WEEK");
    }

    public static TimeRange ofMonth() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fourWeeksAgo = now.minusWeeks(4).with(LocalTime.MIN);
        return new TimeRange(fourWeeksAgo, now, "MONTH");
    }

    public static TimeRange ofQuarter() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeMonthsAgo = now.minusMonths(3).with(LocalTime.MIN);
        return new TimeRange(threeMonthsAgo, now, "QUARTER");
    }

    public static TimeRange ofYear() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twelveMonthsAgo = now.minusMonths(12).with(LocalTime.MIN);
        return new TimeRange(twelveMonthsAgo, now, "YEAR");
    }
}
