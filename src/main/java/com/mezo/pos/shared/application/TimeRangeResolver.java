package com.mezo.pos.shared.application;

import com.mezo.pos.shared.domain.exception.DomainException;
import com.mezo.pos.shared.domain.valueobject.TimeRange;
import org.springframework.stereotype.Component;

@Component
public class TimeRangeResolver {

    public TimeRange resolve(String time) {
        if (time == null || time.isBlank()) {
            throw new DomainException("Query param 'time' is required. Valid values: DAY, WEEK, MONTH, QUARTER, YEAR");
        }

        return switch (time.toUpperCase().trim()) {
            case "DAY" -> TimeRange.ofDay();
            case "WEEK" -> TimeRange.ofWeek();
            case "MONTH" -> TimeRange.ofMonth();
            case "QUARTER" -> TimeRange.ofQuarter();
            case "YEAR" -> TimeRange.ofYear();
            default -> throw new DomainException(
                    "Invalid time param: '" + time + "'. Valid values: DAY, WEEK, MONTH, QUARTER, YEAR"
            );
        };
    }
}
