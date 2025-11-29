package io.warmup.examples.startup.config;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Carga archivos de configuración en memoria mapeada para acceso instantáneo vía OS page cache.
 * Proporciona acceso directo a datos estructurados sin parsing durante runtime.
 */
public class MemoryMappedConfigLoader {
    
    private final ExecutorService fileLoaderPool;
    private final Map<String, MappedConfigData> loadedConfigs;
    private final Map<String, Path> configFilePaths;
    
    // Estadísticas de rendimiento
    private volatile long totalFilesMapped;
    private volatile long totalBytesMapped;
    private volatile long mappingDurationMs;
    private volatile long parsingDurationMs;
    
    public MemoryMappedConfigLoader() {
        this.fileLoaderPool = Executors.newFixedThreadPool(
            Math.max(4, Runtime.getRuntime().availableProcessors() * 2)
        );
        this.loadedConfigs = new ConcurrentHashMap<>();
        this.configFilePaths = new ConcurrentHashMap<>();
        
        // Rutas de archivos de configuración comunes
        initializeDefaultConfigPaths();
    }
    
    /**
     * Inicializa las rutas predeterminadas de archivos de configuración
     */
    private void initializeDefaultConfigPaths() {
        configFilePaths.put("application.properties", 
            Paths.get("src/main/resources/application.properties"));
        configFilePaths.put("application.yml", 
            Paths.get("src/main/resources/application.yml"));
        configFilePaths.put("config.properties", 
            Paths.get("src/main/resources/config.properties"));
        configFilePaths.put("warmup-config.xml", 
            Paths.get("src/main/resources/warmup-config.xml"));
        configFilePaths.put("runtime-config.json", 
            Paths.get("src/main/resources/runtime-config.json"));
    }
    
    /**
     * Registra un archivo de configuración para mapeo en memoria
     */
    public void registerConfigFile(String configKey, Path filePath) {
        configFilePaths.put(configKey, filePath);
    }
    
    /**
     * Carga todos los archivos de configuración en paralelo usando memoria mapeada
     */
    public CompletableFuture<Map<String, ConfigLoadingResult>> 
        loadAllConfigsInParallel() {
        
        long startTime = System.currentTimeMillis();
        
        List<CompletableFuture<ConfigLoadingResult>> futures = configFilePaths.entrySet()
            .stream()
            .map(entry -> loadConfigAsync(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                long endTime = System.currentTimeMillis();
                mappingDurationMs += (endTime - startTime);
                
                Map<String, ConfigLoadingResult> results = new HashMap<>();
                for (CompletableFuture<ConfigLoadingResult> future : futures) {
                    try {
                        ConfigLoadingResult result = future.get();
                        results.put(result.getConfigKey(), result);
                    } catch (Exception e) {
                        // Log error but continue
                        System.err.println("Error loading config: " + e.getMessage());
                    }
                }
                
                return results;
            });
    }
    
    /**
     * Carga un archivo de configuración específico en memoria mapeada
     */
    private CompletableFuture<ConfigLoadingResult> loadConfigAsync(String configKey, Path filePath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return loadConfigFile(configKey, filePath);
            } catch (Exception e) {
                return ConfigLoadingResult.error(configKey, e);
            }
        }, fileLoaderPool);
    }
    
    /**
     * Carga un archivo específico en memoria mapeada y lo parsea a estructura de datos
     */
    private ConfigLoadingResult loadConfigFile(String configKey, Path filePath) {
        long startTime = System.currentTimeMillis();
        
        if (!Files.exists(filePath)) {
            return ConfigLoadingResult.notFound(configKey, filePath);
        }
        
        try {
            // Verificar tamaño del archivo
            long fileSize = Files.size(filePath);
            if (fileSize == 0) {
                return ConfigLoadingResult.empty(configKey, filePath);
            }
            
            // Mapear archivo en memoria
            try (FileChannel fileChannel = FileChannel.open(
                    filePath, 
                    StandardOpenOption.READ)) {
                
                // Crear buffer mapeado en memoria
                MappedByteBuffer mappedBuffer = fileChannel.map(
                    FileChannel.MapMode.READ_ONLY, 
                    0, 
                    fileSize
                );
                
                // Forzar carga del archivo en memoria (carga eager)
                mappedBuffer.load();
                
                long mapTime = System.currentTimeMillis() - startTime;
                
                // Parsear contenido a estructura de datos
                long parseStart = System.currentTimeMillis();
                MappedConfigData configData = parseConfigData(configKey, mappedBuffer, fileSize);
                long parseTime = System.currentTimeMillis() - parseStart;
                
                // Actualizar estadísticas
                totalFilesMapped++;
                totalBytesMapped += fileSize;
                parsingDurationMs += parseTime;
                
                // Guardar en cache
                loadedConfigs.put(configKey, configData);
                
                ConfigLoadingResult result = ConfigLoadingResult.success(
                    configKey, filePath, configData, mapTime, parseTime, fileSize
                );
                
                return result;
                
            }
            
        } catch (IOException e) {
            return ConfigLoadingResult.error(configKey, e);
        } catch (OutOfMemoryError e) {
            return ConfigLoadingResult.memoryError(configKey, e);
        } catch (Exception e) {
            return ConfigLoadingResult.error(configKey, e);
        }
    }
    
    /**
     * Parsea el contenido mapeado en memoria a estructura de datos estructurada
     */
    private MappedConfigData parseConfigData(String configKey, MappedByteBuffer buffer, long fileSize) {
        
        // Leer todo el contenido como bytes
        byte[] content = new byte[(int) fileSize];
        buffer.get(content);
        
        // Determinar tipo de archivo y parsear según extensión
        String lowerKey = configKey.toLowerCase();
        
        if (lowerKey.endsWith(".json")) {
            return parseJsonConfig(content);
        } else if (lowerKey.endsWith(".properties")) {
            return parsePropertiesConfig(content);
        } else if (lowerKey.endsWith(".xml")) {
            return parseXmlConfig(content);
        } else if (lowerKey.endsWith(".yml") || lowerKey.endsWith(".yaml")) {
            return parseYamlConfig(content);
        } else {
            // Archivo desconocido - tratar como texto plano
            return parsePlainTextConfig(content);
        }
    }
    
    /**
     * Parsea configuración JSON
     */
    private MappedConfigData parseJsonConfig(byte[] content) {
        try {
            String jsonString = new String(content, "UTF-8");
            
            // Parseo simplificado de JSON para demo
            Map<String, Object> dataMap = new HashMap<>();
            
            // Extraer pares key:value simples
            String[] lines = jsonString.split("\\n");
            for (String line : lines) {
                line = line.trim();
                if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2) {
                        String key = parts[0].replaceAll("[\"{}]", "").trim();
                        String value = parts[1].replaceAll("[\"{}]", "").trim();
                        dataMap.put(key, parseValue(value));
                    }
                }
            }
            
            return new MappedConfigData("JSON", dataMap, content.length);
            
        } catch (Exception e) {
            // Fallback a texto plano
            return parsePlainTextConfig(content);
        }
    }
    
    /**
     * Parsea configuración properties
     */
    private MappedConfigData parsePropertiesConfig(byte[] content) {
        try {
            String propertiesContent = new String(content, "UTF-8");
            Map<String, Object> dataMap = new HashMap<>();
            
            String[] lines = propertiesContent.split("\\n");
            for (String line : lines) {
                line = line.trim();
                if (!line.startsWith("#") && line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        dataMap.put(key, parseValue(value));
                    }
                }
            }
            
            return new MappedConfigData("PROPERTIES", dataMap, content.length);
            
        } catch (Exception e) {
            return parsePlainTextConfig(content);
        }
    }
    
    /**
     * Parsea configuración XML simplificada
     */
    private MappedConfigData parseXmlConfig(byte[] content) {
        try {
            String xmlContent = new String(content, "UTF-8");
            Map<String, Object> dataMap = new HashMap<>();
            
            // Extraer elementos simples <key>value</key>
            String[] lines = xmlContent.split("\\n");
            for (String line : lines) {
                line = line.trim();
                if (line.matches("<[^/>]+>[^<]+</[^>]+>")) {
                    String cleanLine = line.replaceAll("<([^>]+)>([^<]+)</\\1>", "$1=$2");
                    String[] parts = cleanLine.split("=", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        dataMap.put(key, parseValue(value));
                    }
                }
            }
            
            return new MappedConfigData("XML", dataMap, content.length);
            
        } catch (Exception e) {
            return parsePlainTextConfig(content);
        }
    }
    
    /**
     * Parsea configuración YAML simplificada
     */
    private MappedConfigData parseYamlConfig(byte[] content) {
        try {
            String yamlContent = new String(content, "UTF-8");
            Map<String, Object> dataMap = new HashMap<>();
            
            String[] lines = yamlContent.split("\\n");
            for (String line : lines) {
                line = line.trim();
                if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        dataMap.put(key, parseValue(value));
                    }
                }
            }
            
            return new MappedConfigData("YAML", dataMap, content.length);
            
        } catch (Exception e) {
            return parsePlainTextConfig(content);
        }
    }
    
    /**
     * Parsea configuración como texto plano
     */
    private MappedConfigData parsePlainTextConfig(byte[] content) {
        try {
            String plainContent = new String(content, "UTF-8");
            Map<String, Object> dataMap = new HashMap<>();
            
            // Extraer pares key=value del texto plano
            String[] lines = plainContent.split("\\n");
            for (String line : lines) {
                line = line.trim();
                if (line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        dataMap.put(key, parseValue(value));
                    }
                }
            }
            
            return new MappedConfigData("PLAIN_TEXT", dataMap, content.length);
            
        } catch (Exception e) {
            // Último fallback
            return new MappedConfigData("RAW", Collections.emptyMap(), content.length);
        }
    }
    
    /**
     * Convierte string a tipo de dato apropiado
     */
    private Object parseValue(String value) {
        value = value.trim();
        
        // Booleanos
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(value);
        }
        
        // Números enteros
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {}
        
        // Números decimales
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {}
        
        // Strings con comillas
        if ((value.startsWith("\"") && value.endsWith("\"")) ||
            (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        
        // Por defecto, devolver como string
        return value;
    }
    
    /**
     * Obtiene acceso directo a datos de configuración
     */
    public ConfigDataAccessor getConfigAccessor(String configKey) {
        MappedConfigData configData = loadedConfigs.get(configKey);
        if (configData == null) {
            throw new IllegalArgumentException("Configuración no cargada: " + configKey);
        }
        return new ConfigDataAccessor(configKey, configData);
    }
    
    /**
     * Verifica si una configuración está cargada
     */
    public boolean isConfigLoaded(String configKey) {
        return loadedConfigs.containsKey(configKey);
    }
    
    /**
     * Obtiene estadísticas de mapeo
     */
    public ConfigMappingStats getMappingStats() {
        return new ConfigMappingStats(
            totalFilesMapped,
            totalBytesMapped,
            mappingDurationMs,
            parsingDurationMs,
            loadedConfigs.size(),
            configFilePaths.size()
        );
    }
    
    /**
     * Libera recursos y cierra el pool de hilos
     */
    public void shutdown() {
        fileLoaderPool.shutdown();
        try {
            if (!fileLoaderPool.awaitTermination(5, TimeUnit.SECONDS)) {
                fileLoaderPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            fileLoaderPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // Limpiar referencias a buffers mapeados
        loadedConfigs.clear();
    }
    
    /**
     * Representa el resultado de carga de una configuración
     */
    public static class ConfigLoadingResult {
        private final String configKey;
        private final boolean success;
        private final String status;
        private final Throwable error;
        private final MappedConfigData configData;
        private final long mappingTimeMs;
        private final long parsingTimeMs;
        private final long fileSize;
        
        private ConfigLoadingResult(String configKey, boolean success, String status,
                                  Throwable error, MappedConfigData configData,
                                  long mappingTimeMs, long parsingTimeMs, long fileSize) {
            this.configKey = configKey;
            this.success = success;
            this.status = status;
            this.error = error;
            this.configData = configData;
            this.mappingTimeMs = mappingTimeMs;
            this.parsingTimeMs = parsingTimeMs;
            this.fileSize = fileSize;
        }
        
        public static ConfigLoadingResult success(String configKey, Path filePath,
                                                MappedConfigData configData,
                                                long mappingTimeMs, long parsingTimeMs,
                                                long fileSize) {
            return new ConfigLoadingResult(configKey, true, "SUCCESS", null, configData,
                                         mappingTimeMs, parsingTimeMs, fileSize);
        }
        
        public static ConfigLoadingResult error(String configKey, Exception error) {
            return new ConfigLoadingResult(configKey, false, "ERROR", error, null, 0, 0, 0);
        }
        
        public static ConfigLoadingResult notFound(String configKey, Path filePath) {
            return new ConfigLoadingResult(configKey, false, "FILE_NOT_FOUND", null, null, 0, 0, 0);
        }
        
        public static ConfigLoadingResult empty(String configKey, Path filePath) {
            return new ConfigLoadingResult(configKey, false, "EMPTY_FILE", null, null, 0, 0, 0);
        }
        
        public static ConfigLoadingResult memoryError(String configKey, OutOfMemoryError error) {
            return new ConfigLoadingResult(configKey, false, "OUT_OF_MEMORY", error, null, 0, 0, 0);
        }
        
        // Getters
        public String getConfigKey() { return configKey; }
        public boolean isSuccess() { return success; }
        public String getStatus() { return status; }
        public Throwable getError() { return error; }
        public MappedConfigData getConfigData() { return configData; }
        public long getMappingTimeMs() { return mappingTimeMs; }
        public long getParsingTimeMs() { return parsingTimeMs; }
        public long getFileSize() { return fileSize; }
    }
}