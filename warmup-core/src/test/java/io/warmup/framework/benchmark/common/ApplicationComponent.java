package io.warmup.framework.benchmark.common;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Componente de aplicación que usa inyección de dependencias
 */
@Singleton
public class ApplicationComponent {
    
    private final BasicService basicService;
    private final ConfigService configService;
    
    @Inject
    public ApplicationComponent(BasicService basicService, ConfigService configService) {
        this.basicService = basicService;
        this.configService = configService;
    }
    
    public void start() {
        String config = configService.getValue("app.name");
        String serviceResult = basicService.process("App starting: " + config);
        basicService.performOperation();
    }
    
    public String getServiceStatus() {
        return "ApplicationComponent: " + basicService.getName() + " configured";
    }
    
    public BasicService getBasicService() {
        return basicService;
    }
}