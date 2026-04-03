package com.scan2dine.scan2dine.dto;

import java.util.List;
import java.util.Map;

public class AnalyticsResponse {

    private double totalRevenue;
    private long totalOrders;
    private long totalUsers;
    private long totalRestaurants;

    // Ordered chronologically (e.g., {"Mon": 1400.5, "Tue": 3000.0})
    private List<Map<String, Object>> revenueTimeline; 

    // E.g., [{"name": "Pizza Plaza", "orders": 120}]
    private List<Map<String, Object>> topRestaurants;

    public AnalyticsResponse() {
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalRestaurants() {
        return totalRestaurants;
    }

    public void setTotalRestaurants(long totalRestaurants) {
        this.totalRestaurants = totalRestaurants;
    }

    public List<Map<String, Object>> getRevenueTimeline() {
        return revenueTimeline;
    }

    public void setRevenueTimeline(List<Map<String, Object>> revenueTimeline) {
        this.revenueTimeline = revenueTimeline;
    }

    public List<Map<String, Object>> getTopRestaurants() {
        return topRestaurants;
    }

    public void setTopRestaurants(List<Map<String, Object>> topRestaurants) {
        this.topRestaurants = topRestaurants;
    }
}
