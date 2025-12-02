package io.warmup.framework.cache;

import java.io.File;

public class CacheConfig {

    private static final String DEFAULT_CACHE_DIR = System.getProperty("user.home")
            + File.separator + ".warmup" + File.separator + "asm-cache";
    String cacheDirectory = DEFAULT_CACHE_DIR;
    boolean enableDiskCache = true;
    boolean compressCache = true;
    long maxCacheAge = 7 * 24 * 60 * 60 * 1000L;
    int maxMemoryCacheSize = 1000;
    int maxDiskCacheSizeMB = 500;
    int diskIOThreads = 2;
    public boolean asyncDiskWrites = true;

    public static CacheConfig defaultConfig() {
        return new CacheConfig();
    }

    public CacheConfig withCacheDirectory(String directory) {
        this.cacheDirectory = directory;
        return this;
    }

    public CacheConfig withDiskCache(boolean enabled) {
        this.enableDiskCache = enabled;
        return this;
    }

    public CacheConfig withCompression(boolean enabled) {
        this.compressCache = enabled;
        return this;
    }

    public CacheConfig withMaxAge(long milliseconds) {
        this.maxCacheAge = milliseconds;
        return this;
    }

    public CacheConfig withMaxMemorySize(int entries) {
        if (entries <= 0) {
            throw new IllegalArgumentException("Max memory size must be positive");
        }
        this.maxMemoryCacheSize = entries;
        return this;
    }

    public CacheConfig withMaxDiskSize(int megabytes) {
        if (megabytes < 0) {
            throw new IllegalArgumentException("Max disk size cannot be negative");
        }
        this.maxDiskCacheSizeMB = megabytes;
        return this;
    }

    public CacheConfig withDiskIOThreads(int threads) {
        if (threads <= 0 || threads > 10) {
            throw new IllegalArgumentException("Disk IO threads must be between 1 and 10");
        }
        this.diskIOThreads = threads;
        return this;
    }

    public CacheConfig withAsyncDiskWrites(boolean async) {
        this.asyncDiskWrites = async;
        return this;
    }

    public String getCacheDirectory() {
        return cacheDirectory;
    }

    public boolean isEnableDiskCache() {
        return enableDiskCache;
    }

    public boolean isCompressCache() {
        return compressCache;
    }

    public long getMaxCacheAge() {
        return maxCacheAge;
    }

    public int getMaxMemoryCacheSize() {
        return maxMemoryCacheSize;
    }

    public int getMaxDiskCacheSizeMB() {
        return maxDiskCacheSizeMB;
    }

    public int getDiskIOThreads() {
        return diskIOThreads;
    }

    public boolean isAsyncDiskWrites() {
        return asyncDiskWrites;
    }
    
    
}