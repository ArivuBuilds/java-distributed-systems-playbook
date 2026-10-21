package com.arivubuilds.concurrency.jmm;

/**
 * Demonstrates JMM Transitivity and "Piggybacking" on a volatile write.
 * Plain field mutations prior to the volatile write are guaranteed visible to any thread
 * that subsequently reads the volatile flag.
 */
public class VolatilePiggybacking {

    private String configData;
    private int timeoutMs;
    private volatile boolean initialized = false;

    public void publishConfiguration(String data, int timeout) {
        this.configData = data;       // (1) Normal write
        this.timeoutMs = timeout;     // (2) Normal write
        this.initialized = true;      // (3) Volatile write -> StoreStore + StoreLoad barrier
    }

    public String readConfigurationIfReady() {
        if (initialized) {            // (4) Volatile read -> LoadLoad + LoadStore barrier
            // Transitivity guarantees (1) & (2) are visible here without making them volatile
            return "Data: " + configData + ", Timeout: " + timeoutMs;
        }
        return null;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
