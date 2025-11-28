package io.warmup.framework.asm;

import io.warmup.framework.cache.ASMCacheManager;

public class AsmEngine {

    private static AnnotatedConstructorAnalyzer annotatedConstructorAnalyzer;
    private static AnnotatedConstructorFinder annotatedConstructorFinder;
    private static AnnotationCheckingConstructorAnalyzer annotationCheckingConstructorAnalyzer;
    private static AsmComponentScanner asmComponentScanner;
    private static AsmConstructorScanner asmConstructorScanner;
    private static AsmFieldInjector asmFieldInjector;
    private static ConstructorAnalyzer constructorAnalyzer;
    private static ConstructorFinder constructorFinder;
    private static EnhancedConstructorAnalyzer enhancedConstructorAnalyzer;
    private static FieldAccessorGenerator fieldAccessorGenerator;
    private static FullConstructorFinder fullConstructorFinder;
    private static ASMCacheManager aSMCacheManager;

    static {
        initializeComponents();
    }

    private static void initializeComponents() {
        aSMCacheManager = ASMCacheManager.getInstance();
    }

    public static void shutdown() {
        // Limpiar recursos si alguno de los componentes los tiene
        annotatedConstructorAnalyzer = null;
        annotatedConstructorFinder = null;
        annotationCheckingConstructorAnalyzer = null;
        asmComponentScanner = null;
        asmConstructorScanner = null;
        asmFieldInjector = null;
        constructorAnalyzer = null;
        constructorFinder = null;
        enhancedConstructorAnalyzer = null;
        fieldAccessorGenerator = null;
        fullConstructorFinder = null;
    }

    public static AnnotatedConstructorAnalyzer getAnnotatedConstructorAnalyzer() {
        return annotatedConstructorAnalyzer;
    }

    public static void setAnnotatedConstructorAnalyzer(AnnotatedConstructorAnalyzer annotatedConstructorAnalyzer) {
        AsmEngine.annotatedConstructorAnalyzer = annotatedConstructorAnalyzer;
    }

    public static AnnotatedConstructorFinder getAnnotatedConstructorFinder() {
        return annotatedConstructorFinder;
    }

    public static void setAnnotatedConstructorFinder(AnnotatedConstructorFinder annotatedConstructorFinder) {
        AsmEngine.annotatedConstructorFinder = annotatedConstructorFinder;
    }

    public static AnnotationCheckingConstructorAnalyzer getAnnotationCheckingConstructorAnalyzer() {
        return annotationCheckingConstructorAnalyzer;
    }

    public static void setAnnotationCheckingConstructorAnalyzer(AnnotationCheckingConstructorAnalyzer annotationCheckingConstructorAnalyzer) {
        AsmEngine.annotationCheckingConstructorAnalyzer = annotationCheckingConstructorAnalyzer;
    }

    public static AsmComponentScanner getAsmComponentScanner() {
        return asmComponentScanner;
    }

    public static void setAsmComponentScanner(AsmComponentScanner asmComponentScanner) {
        AsmEngine.asmComponentScanner = asmComponentScanner;
    }

    public static AsmConstructorScanner getAsmConstructorScanner() {
        return asmConstructorScanner;
    }

    public static void setAsmConstructorScanner(AsmConstructorScanner asmConstructorScanner) {
        AsmEngine.asmConstructorScanner = asmConstructorScanner;
    }

    public static AsmFieldInjector getAsmFieldInjector() {
        return asmFieldInjector;
    }

    public static void setAsmFieldInjector(AsmFieldInjector asmFieldInjector) {
        AsmEngine.asmFieldInjector = asmFieldInjector;
    }

    public static ConstructorAnalyzer getConstructorAnalyzer() {
        return constructorAnalyzer;
    }

    public static void setConstructorAnalyzer(ConstructorAnalyzer constructorAnalyzer) {
        AsmEngine.constructorAnalyzer = constructorAnalyzer;
    }

    public static ConstructorFinder getConstructorFinder() {
        return constructorFinder;
    }

    public static void setConstructorFinder(ConstructorFinder constructorFinder) {
        AsmEngine.constructorFinder = constructorFinder;
    }

    public static EnhancedConstructorAnalyzer getEnhancedConstructorAnalyzer() {
        return enhancedConstructorAnalyzer;
    }

    public static void setEnhancedConstructorAnalyzer(EnhancedConstructorAnalyzer enhancedConstructorAnalyzer) {
        AsmEngine.enhancedConstructorAnalyzer = enhancedConstructorAnalyzer;
    }

    public static FieldAccessorGenerator getFieldAccessorGenerator() {
        return fieldAccessorGenerator;
    }

    public static void setFieldAccessorGenerator(FieldAccessorGenerator fieldAccessorGenerator) {
        AsmEngine.fieldAccessorGenerator = fieldAccessorGenerator;
    }

    public static FullConstructorFinder getFullConstructorFinder() {
        return fullConstructorFinder;
    }

    public static void setFullConstructorFinder(FullConstructorFinder fullConstructorFinder) {
        AsmEngine.fullConstructorFinder = fullConstructorFinder;
    }

    public static ASMCacheManager getaSMCacheManager() {
        return aSMCacheManager;
    }

    public static void setaSMCacheManager(ASMCacheManager aSMCacheManager) {
        AsmEngine.aSMCacheManager = aSMCacheManager;
    }

}
