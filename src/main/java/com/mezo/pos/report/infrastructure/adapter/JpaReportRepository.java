package com.mezo.pos.report.infrastructure.adapter;

import com.mezo.pos.report.domain.model.*;
import com.mezo.pos.report.domain.port.ReportRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class JpaReportRepository implements ReportRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public SalesTotal getSalesTotal(UUID businessId, LocalDateTime from, LocalDateTime to) {
        String sql = """
                SELECT COALESCE(SUM(s.total_amount), 0),
                       COALESCE(SUM(s.tip_amount), 0),
                       COUNT(s.id)
                FROM sales s
                WHERE s.business_id = :businessId
                  AND s.deleted = false
                  AND s.created_at >= :from
                  AND s.created_at <= :to
                """;

        Object[] row = (Object[]) em.createNativeQuery(sql)
                .setParameter("businessId", businessId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();

        SalesTotal result = new SalesTotal();
        result.setTotalSales(toBigDecimal(row[0]));
        result.setTotalTips(toBigDecimal(row[1]));
        result.setCurrency("COP");
        result.setSalesCount(toLong(row[2]));
        return result;
    }

    @Override
    public long getSalesCount(UUID businessId, LocalDateTime from, LocalDateTime to) {
        String sql = """
                SELECT COUNT(s.id)
                FROM sales s
                WHERE s.business_id = :businessId
                  AND s.deleted = false
                  AND s.created_at >= :from
                  AND s.created_at <= :to
                """;

        Object count = em.createNativeQuery(sql)
                .setParameter("businessId", businessId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();

        return toLong(count);
    }

    @Override
    public PeakHour getPeakHour(UUID businessId, LocalDateTime from, LocalDateTime to) {
        String sql = """
                SELECT HOUR(s.created_at) AS sale_hour,
                       COUNT(s.id) AS sale_count,
                       COALESCE(SUM(s.total_amount), 0) AS sale_total
                FROM sales s
                WHERE s.business_id = :businessId
                  AND s.deleted = false
                  AND s.created_at >= :from
                  AND s.created_at <= :to
                GROUP BY HOUR(s.created_at)
                ORDER BY sale_count DESC, sale_total DESC
                LIMIT 1
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("businessId", businessId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        if (rows.isEmpty()) {
            PeakHour empty = new PeakHour();
            empty.setHour(0);
            empty.setLabel("0:00 - 1:00");
            empty.setSalesCount(0);
            empty.setTotal(BigDecimal.ZERO);
            return empty;
        }

        Object[] row = rows.get(0);
        int hour = toInt(row[0]);

        PeakHour result = new PeakHour();
        result.setHour(hour);
        result.setLabel(hour + ":00 - " + (hour + 1) + ":00");
        result.setSalesCount(toLong(row[1]));
        result.setTotal(toBigDecimal(row[2]));
        return result;
    }

    @Override
    public List<TopProduct> getTopProducts(UUID businessId, LocalDateTime from, LocalDateTime to, int limit) {
        String sql = """
                SELECT ol.product_id,
                       ol.product_name,
                       SUM(ol.quantity) AS total_sold,
                       SUM(ol.subtotal_amount) AS revenue
                FROM order_lines ol
                JOIN orders o ON ol.order_id = o.id
                WHERE o.business_id = :businessId
                  AND o.deleted = false
                  AND o.created_at >= :from
                  AND o.created_at <= :to
                GROUP BY ol.product_id, ol.product_name
                ORDER BY total_sold DESC
                LIMIT :limit
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("businessId", businessId)
                .setParameter("from", from)
                .setParameter("to", to)
                .setParameter("limit", limit)
                .getResultList();

        List<TopProduct> products = new ArrayList<>();
        for (Object[] row : rows) {
            TopProduct tp = new TopProduct();
            tp.setProductId(toUUID(row[0]));
            tp.setName((String) row[1]);
            tp.setTotalSold(toLong(row[2]));
            tp.setRevenue(toBigDecimal(row[3]));
            products.add(tp);
        }
        return products;
    }

    @Override
    public List<PaymentMethodStats> getPaymentMethodStats(UUID businessId, LocalDateTime from, LocalDateTime to) {
        String sql = """
                SELECT s.payment_method,
                       COUNT(s.id) AS method_count,
                       COALESCE(SUM(s.total_amount), 0) AS method_total
                FROM sales s
                WHERE s.business_id = :businessId
                  AND s.deleted = false
                  AND s.created_at >= :from
                  AND s.created_at <= :to
                GROUP BY s.payment_method
                ORDER BY method_count DESC
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("businessId", businessId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        long totalCount = rows.stream().mapToLong(r -> toLong(r[1])).sum();

        List<PaymentMethodStats> stats = new ArrayList<>();
        for (Object[] row : rows) {
            PaymentMethodStats pms = new PaymentMethodStats();
            pms.setMethod((String) row[0]);
            pms.setCount(toLong(row[1]));
            pms.setTotal(toBigDecimal(row[2]));
            pms.setPercentage(totalCount > 0
                    ? BigDecimal.valueOf(pms.getCount())
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalCount), 1, RoundingMode.HALF_UP)
                        .doubleValue()
                    : 0.0);
            stats.add(pms);
        }
        return stats;
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

    private UUID toUUID(Object value) {
        if (value == null) return null;
        if (value instanceof UUID uuid) return uuid;
        return UUID.fromString(value.toString());
    }
}
