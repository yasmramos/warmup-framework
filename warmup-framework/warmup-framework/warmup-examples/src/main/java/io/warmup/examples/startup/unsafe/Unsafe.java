package io.warmup.examples.startup.unsafe;

/**
 * Stub implementation of Unsafe for compilation purposes.
 * TODO: Replace with VarHandle or proper unsafe access for production.
 */
public abstract class Unsafe {
    public abstract long allocateMemory(long bytes);
    public abstract void freeMemory(long address);
    public abstract void putByte(long address, byte value);
    public abstract byte getByte(long address);
    public abstract void putInt(long address, int value);
    public abstract int getInt(long address);
    public abstract void putLong(long address, long value);
    public abstract long getLong(long address);
    public abstract void putObject(long address, Object value);
    public abstract Object getObject(long address);
}