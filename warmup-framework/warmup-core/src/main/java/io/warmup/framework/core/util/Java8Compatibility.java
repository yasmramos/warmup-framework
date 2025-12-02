package io.warmup.framework.core.util;

/**
 * Java 8 compatibility utilities to replace Java 9+ features
 */
public class Java8Compatibility {
    
    /**
     * Java 8 compatible replacement for String.repeat(int)
     */
    public static String repeat(String str, int count) {
        if (str == null) {
            return null;
        }
        if (count <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    /**
     * Java 8 compatible Map creation
     */
    public static java.util.Map<String, Object> mapOf(Object... keyValuePairs) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            String key = String.valueOf(keyValuePairs[i]);
            Object value = (i + 1 < keyValuePairs.length) ? keyValuePairs[i + 1] : null;
            map.put(key, value);
        }
        return map;
    }
}