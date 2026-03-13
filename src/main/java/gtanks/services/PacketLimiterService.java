/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.services;

import java.util.concurrent.atomic.AtomicInteger;

public class PacketLimiterService {
    private static final int MAX_PACKETS_PER_SECOND = 100;
    private static final long TIME_FRAME = 1000L;
    private final AtomicInteger packetCount = new AtomicInteger(0);
    private long lastResetTime = System.currentTimeMillis();

    public boolean allowPacket() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastResetTime > 1000L) {
            this.packetCount.set(0);
            this.lastResetTime = currentTime;
        }
        return this.packetCount.incrementAndGet() <= 100;
    }
}

