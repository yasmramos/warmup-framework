package io.warmup.framework.startup.unsafe;

/**
 * Report for unsafe memory operations
 */
public class UnsafeMemoryReport {
    private final String reportId;
    private final UnsafeMemoryStatistics statistics;
    private final long generationTime;
    private final String summary;
    
    public UnsafeMemoryReport(String reportId, UnsafeMemoryStatistics statistics, long generationTime, String summary) {
        this.reportId = reportId;
        this.statistics = statistics;
        this.generationTime = generationTime;
        this.summary = summary;
    }
    
    public String getReportId() { return reportId; }
    public UnsafeMemoryStatistics getStatistics() { return statistics; }
    public long getGenerationTime() { return generationTime; }
    public String getSummary() { return summary; }
}