package io.warmup.framework.cache;

class CachedClass {

    final String className;
    final String sourceHash;
    final byte[] bytecode;
    final long timestamp;

    CachedClass(String className, String sourceHash, byte[] bytecode) {
        this.className = className;
        this.sourceHash = sourceHash;
        this.bytecode = bytecode;
        this.timestamp = System.currentTimeMillis();
    }
}