package com.dailymart.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardDto {
    private long totalUsers;
    private long totalProducts;
    private long totalOrders;
    private BigDecimal totalRevenue;
    private long pendingOrders;
    private long deliveredOrders;
    private List<OrderDto> recentOrders;
    private List<ProductDto> topProducts;
    private Map<String, Long> ordersByStatus;
    private Map<String, BigDecimal> revenueByMonth;
}
