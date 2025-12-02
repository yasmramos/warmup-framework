/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.warmup.framework.core;

public class PerformanceMetrics {
    private long containerStartupTime;
    private long dependencyResolutionTime;
    private long totalInjectionTime;
    private int totalResolutions;
    private int failedResolutions;
    
    // Métodos para medir tiempos
    public void recordContainerStartup(long time) {
        this.containerStartupTime = time;
    }
    
    public void recordResolution(long time, boolean success) {
        this.dependencyResolutionTime += time;
        this.totalResolutions++;
        if (!success) this.failedResolutions++;
    }
}