package io.warmup.framework.hotreload.advanced;

import java.time.Duration;

/**
 * Configuration for the hot reload dashboard.
 * Controls how the real-time dashboard displays information and updates.
 * 
 * @author MiniMax Agent
 * @since 2.0
 */
public class DashboardConfig {
    
    private boolean enabled = true;
    private int updateIntervalSeconds = 5;
    private boolean showDetailedMetrics = true;
    private boolean showHistoricalData = true;
    private int maxHistoryEntries = 1000;
    private boolean enableNotifications = true;
    private boolean showPerformanceCharts = true;
    private boolean enableExport = true;
    private Duration dataRetentionPeriod = Duration.ofHours(24);
    private boolean enableRealTimeUpdates = true;
    private int maxConcurrentDashboardUpdates = 3;
    private boolean showMemoryUsage = true;
    private boolean showReloadStatistics = true;
    private boolean showSystemHealth = true;
    private String dashboardTheme = "default";
    
    public DashboardConfig() {
        // Default configuration
    }
    
    public DashboardConfig(boolean enabled, int updateIntervalSeconds) {
        this.enabled = enabled;
        this.updateIntervalSeconds = updateIntervalSeconds;
    }
    
    /**
     * Checks if dashboard is enabled.
     * 
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Sets whether dashboard is enabled.
     * 
     * @param enabled the enabled flag
     * @return this config
     */
    public DashboardConfig setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
    
    /**
     * Gets update interval in seconds.
     * 
     * @return update interval in seconds
     */
    public int getUpdateIntervalSeconds() {
        return updateIntervalSeconds;
    }
    
    /**
     * Sets update interval in seconds.
     * 
     * @param updateIntervalSeconds the interval in seconds
     * @return this config
     */
    public DashboardConfig setUpdateIntervalSeconds(int updateIntervalSeconds) {
        this.updateIntervalSeconds = updateIntervalSeconds;
        return this;
    }
    
    /**
     * Checks if detailed metrics are shown.
     * 
     * @return true if show detailed metrics
     */
    public boolean isShowDetailedMetrics() {
        return showDetailedMetrics;
    }
    
    /**
     * Sets whether detailed metrics are shown.
     * 
     * @param showDetailedMetrics the show detailed metrics flag
     * @return this config
     */
    public DashboardConfig setShowDetailedMetrics(boolean showDetailedMetrics) {
        this.showDetailedMetrics = showDetailedMetrics;
        return this;
    }
    
    /**
     * Checks if historical data is shown.
     * 
     * @return true if show historical data
     */
    public boolean isShowHistoricalData() {
        return showHistoricalData;
    }
    
    /**
     * Sets whether historical data is shown.
     * 
     * @param showHistoricalData the show historical data flag
     * @return this config
     */
    public DashboardConfig setShowHistoricalData(boolean showHistoricalData) {
        this.showHistoricalData = showHistoricalData;
        return this;
    }
    
    /**
     * Gets maximum history entries.
     * 
     * @return max history entries
     */
    public int getMaxHistoryEntries() {
        return maxHistoryEntries;
    }
    
    /**
     * Sets maximum history entries.
     * 
     * @param maxHistoryEntries the max history entries
     * @return this config
     */
    public DashboardConfig setMaxHistoryEntries(int maxHistoryEntries) {
        this.maxHistoryEntries = maxHistoryEntries;
        return this;
    }
    
    /**
     * Checks if notifications are enabled.
     * 
     * @return true if notifications enabled
     */
    public boolean isEnableNotifications() {
        return enableNotifications;
    }
    
    /**
     * Sets whether notifications are enabled.
     * 
     * @param enableNotifications the notifications flag
     * @return this config
     */
    public DashboardConfig setEnableNotifications(boolean enableNotifications) {
        this.enableNotifications = enableNotifications;
        return this;
    }
    
    /**
     * Checks if performance charts are shown.
     * 
     * @return true if show performance charts
     */
    public boolean isShowPerformanceCharts() {
        return showPerformanceCharts;
    }
    
    /**
     * Sets whether performance charts are shown.
     * 
     * @param showPerformanceCharts the show performance charts flag
     * @return this config
     */
    public DashboardConfig setShowPerformanceCharts(boolean showPerformanceCharts) {
        this.showPerformanceCharts = showPerformanceCharts;
        return this;
    }
    
    /**
     * Checks if export is enabled.
     * 
     * @return true if export enabled
     */
    public boolean isEnableExport() {
        return enableExport;
    }
    
    /**
     * Sets whether export is enabled.
     * 
     * @param enableExport the export flag
     * @return this config
     */
    public DashboardConfig setEnableExport(boolean enableExport) {
        this.enableExport = enableExport;
        return this;
    }
    
    /**
     * Gets data retention period.
     * 
     * @return data retention period
     */
    public Duration getDataRetentionPeriod() {
        return dataRetentionPeriod;
    }
    
    /**
     * Sets data retention period.
     * 
     * @param dataRetentionPeriod the retention period
     * @return this config
     */
    public DashboardConfig setDataRetentionPeriod(Duration dataRetentionPeriod) {
        this.dataRetentionPeriod = dataRetentionPeriod;
        return this;
    }
    
    /**
     * Checks if real-time updates are enabled.
     * 
     * @return true if real-time updates enabled
     */
    public boolean isEnableRealTimeUpdates() {
        return enableRealTimeUpdates;
    }
    
    /**
     * Sets whether real-time updates are enabled.
     * 
     * @param enableRealTimeUpdates the real-time updates flag
     * @return this config
     */
    public DashboardConfig setEnableRealTimeUpdates(boolean enableRealTimeUpdates) {
        this.enableRealTimeUpdates = enableRealTimeUpdates;
        return this;
    }
    
    /**
     * Gets maximum concurrent dashboard updates.
     * 
     * @return max concurrent updates
     */
    public int getMaxConcurrentDashboardUpdates() {
        return maxConcurrentDashboardUpdates;
    }
    
    /**
     * Sets maximum concurrent dashboard updates.
     * 
     * @param maxConcurrentDashboardUpdates the max concurrent updates
     * @return this config
     */
    public DashboardConfig setMaxConcurrentDashboardUpdates(int maxConcurrentDashboardUpdates) {
        this.maxConcurrentDashboardUpdates = maxConcurrentDashboardUpdates;
        return this;
    }
    
    /**
     * Checks if memory usage is shown.
     * 
     * @return true if show memory usage
     */
    public boolean isShowMemoryUsage() {
        return showMemoryUsage;
    }
    
    /**
     * Sets whether memory usage is shown.
     * 
     * @param showMemoryUsage the show memory usage flag
     * @return this config
     */
    public DashboardConfig setShowMemoryUsage(boolean showMemoryUsage) {
        this.showMemoryUsage = showMemoryUsage;
        return this;
    }
    
    /**
     * Checks if reload statistics are shown.
     * 
     * @return true if show reload statistics
     */
    public boolean isShowReloadStatistics() {
        return showReloadStatistics;
    }
    
    /**
     * Sets whether reload statistics are shown.
     * 
     * @param showReloadStatistics the show reload statistics flag
     * @return this config
     */
    public DashboardConfig setShowReloadStatistics(boolean showReloadStatistics) {
        this.showReloadStatistics = showReloadStatistics;
        return this;
    }
    
    /**
     * Checks if system health is shown.
     * 
     * @return true if show system health
     */
    public boolean isShowSystemHealth() {
        return showSystemHealth;
    }
    
    /**
     * Sets whether system health is shown.
     * 
     * @param showSystemHealth the show system health flag
     * @return this config
     */
    public DashboardConfig setShowSystemHealth(boolean showSystemHealth) {
        this.showSystemHealth = showSystemHealth;
        return this;
    }
    
    /**
     * Gets dashboard theme.
     * 
     * @return dashboard theme
     */
    public String getDashboardTheme() {
        return dashboardTheme;
    }
    
    /**
     * Sets dashboard theme.
     * 
     * @param dashboardTheme the dashboard theme
     * @return this config
     */
    public DashboardConfig setDashboardTheme(String dashboardTheme) {
        this.dashboardTheme = dashboardTheme;
        return this;
    }
    
    /**
     * Gets update interval in milliseconds.
     * 
     * @return update interval in milliseconds
     */
    public long getUpdateIntervalInMillis() {
        return updateIntervalSeconds * 1000L;
    }
    
    @Override
    public String toString() {
        return String.format(
            "DashboardConfig{enabled=%s, interval=%ds, theme=%s, retention=%s}",
            enabled,
            updateIntervalSeconds,
            dashboardTheme,
            dataRetentionPeriod
        );
    }
}