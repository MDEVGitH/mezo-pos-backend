package com.mezo.pos.analytics.infrastructure.adapter;

import com.mezo.pos.analytics.domain.model.TimeSeriesPoint;
import com.mezo.pos.analytics.domain.port.AnalyticsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Repository
public class JpaAnalyticsRepository implements AnalyticsRepository {

    private static final Locale LOCALE_ES = Locale.forLanguageTag("es");

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<TimeSeriesPoint> getTimeSeries(UUID businessId, String timeRange, LocalDateTime from, LocalDateTime to) {
        return switch (timeRange) {
            case "DAY" -> getByHour(businessId, from, to);
            case "WEEK" -> getByDay(businessId, from, to);
            case "MONTH" -> getByWeek(businessId, from, to);
            case "QUARTER" -> getByMonth(businessId, from, to);
            case "YEAR" -> getByMonthWithYear(businessId, from, to);
            default -> List.of();
        };
    }

    private List<TimeSeriesPoint> getByHour(UUID businessId, LocalDateTime from, LocalDateTime to) {
        String sql = """
                SELECT HOUR(s.created_at) AS h,
                       COALESCE(SUM(s.total_amount), 0) AS total,
                       COUNT(s.id) AS cnt
                FROM sales s
                WHERE s.business_id = :businessId
                  AND s.deleted = false
                  AND s.created_at >= :from
                  AND s.created_at <= :to
                GROUP BY HOUR(s.created_at)
                ORDER BY h
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("businessId", businessId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        List<TimeSeriesPoint> points = new ArrayList<>();
        for (Object[] row : rows) {
            int hour = toInt(row[0]);
            TimeSeriesPoint point = new TimeSeriesPoint();
            point.setLabel(hour + ":00");
            point.setDate(null);
            point.setTotal(toBigDecimal(row[1]));
            point.setCount(toLong(row[2]));
            points.add(point);
        }
        return points;
    }

    private List<TimeSeriesPoint> getByDay(UUID businessId, LocalDateTime from, LocalDateTime to) {
        String sql = """
                SELECT CAST(s.created_at AS DATE) AS sale_date,
                       COALESCE(SUM(s.total_amount), 0) AS total,
                       COUNT(s.id) AS cnt
                FROM sales s
                WHERE s.business_id = :businessId
                  AND s.deleted = false
                  AND s.created_at >= :from
                  AND s.created_at <= :to
                GROUP BY CAST(s.created_at AS DATE)
                ORDER BY sale_date
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("businessId", businessId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        String[] dayLabels = {"Dom", "Lun", "Mar", "Mie", "Jue", "Vie", "Sab"};

        List<TimeSeriesPoint> points = new ArrayList<>();
        for (Object[] row : rows) {
            LocalDate date = toLocalDate(row[0]);
            int dow = date.getDayOfWeek().getValue() % 7; // 0=Sunday in our labels array
            String label = dayLabels[dow];

            TimeSeriesPoint point = new TimeSeriesPoint();
            point.setLabel(label);
            point.setDate(date.toString());
            point.setTotal(toBigDecimal(row[1]));
            point.setCount(toLong(row[2]));
            points.add(point);
        }
        return points;
    }

    private List<TimeSeriesPoint> getByWeek(UUID businessId, LocalDateTime from, LocalDateTime to) {
        String sql = """
                SELECT ISO_WEEK(s.created_at) AS week_num,
                       MIN(CAST(s.created_at AS DATE)) AS week_start,
                       COALESCE(SUM(s.total_amount), 0) AS total,
                       COUNT(s.id) AS cnt
                FROM sales s
                WHERE s.business_id = :businessId
                  AND s.deleted = false
                  AND s.created_at >= :from
                  AND s.created_at <= :to
                GROUP BY ISO_WEEK(s.created_at)
                ORDER BY week_num
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("businessId", businessId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        List<TimeSeriesPoint> points = new ArrayList<>();
        int weekIndex = 1;
        for (Object[] row : rows) {
            TimeSeriesPoint point = new TimeSeriesPoint();
            point.setLabel("Sem " + weekIndex);
            point.setDate(toLocalDate(row[1]).toString());
            point.setTotal(toBigDecimal(row[2]));
            point.setCount(toLong(row[3]));
            points.add(point);
            weekIndex++;
        }
        return points;
    }

    private List<TimeSeriesPoint> getByMonth(UUID businessId, LocalDateTime from, LocalDateTime to) {
        String sql = """
                SELECT MONTH(s.created_at) AS m,
                       YEAR(s.created_at) AS y,
                       COALESCE(SUM(s.total_amount), 0) AS total,
                       COUNT(s.id) AS cnt
                FROM sales s
                WHERE s.business_id = :businessId
                  AND s.deleted = false
                  AND s.created_at >= :from
                  AND s.created_at <= :to
                GROUP BY YEAR(s.created_at), MONTH(s.created_at)
                ORDER BY y, m
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("businessId", businessId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        String[] monthLabels = {"Ene", "Feb", "Mar", "Abr", "May", "Jun",
                "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};

        List<TimeSeriesPoint> points = new ArrayList<>();
        for (Object[] row : rows) {
            int month = toInt(row[0]);
            String label = monthLabels[month - 1];

            TimeSeriesPoint point = new TimeSeriesPoint();
            point.setLabel(label);
            point.setDate(null);
            point.setTotal(toBigDecimal(row[2]));
            point.setCount(toLong(row[3]));
            points.add(point);
        }
        return points;
    }

    private List<TimeSeriesPoint> getByMonthWithYear(UUID businessId, LocalDateTime from, LocalDateTime to) {
        String sql = """
                SELECT MONTH(s.created_at) AS m,
                       YEAR(s.created_at) AS y,
                       COALESCE(SUM(s.total_amount), 0) AS total,
                       COUNT(s.id) AS cnt
                FROM sales s
                WHERE s.business_id = :businessId
                  AND s.deleted = false
                  AND s.created_at >= :from
                  AND s.created_at <= :to
                GROUP BY YEAR(s.created_at), MONTH(s.created_at)
                ORDER BY y, m
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("businessId", businessId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        String[] monthLabels = {"Ene", "Feb", "Mar", "Abr", "May", "Jun",
                "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};

        List<TimeSeriesPoint> points = new ArrayList<>();
        for (Object[] row : rows) {
            int month = toInt(row[0]);
            int year = toInt(row[1]);
            String label = monthLabels[month - 1] + " " + year;

            TimeSeriesPoint point = new TimeSeriesPoint();
            point.setLabel(label);
            point.setDate(null);
            point.setTotal(toBigDecimal(row[2]));
            point.setCount(toLong(row[3]));
            points.add(point);
        }
        return points;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd;
        return new BigDecimal(value.toString());
    }

    private long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number n) return n.longValue();
        return Long.parseLong(value.toString());
    }

    private int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number n) return n.intValue();
        return Integer.parseInt(value.toString());
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) return LocalDate.now();
        if (value instanceof java.sql.Date sqlDate) return sqlDate.toLocalDate();
        if (value instanceof LocalDate ld) return ld;
        return LocalDate.parse(value.toString());
    }
}
