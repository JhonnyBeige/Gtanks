/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.main.netty;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ConnectionRateLimiter {
    private static final int MAX_CONNECTIONS_PER_IP = 100;
    private static final int COOLDOWN_PERIOD_SECONDS = 60;
    private static final ConcurrentHashMap<String, AtomicLong> connectionsPerIp = new ConcurrentHashMap();
    private static final ConcurrentHashMap<String, AtomicLong> cooldownEndTimes = new ConcurrentHashMap();

    public static boolean allowConnection(String ipAddress) {
        if (ConnectionRateLimiter.isInCooldown(ipAddress)) {
            return false;
        }
        AtomicLong connectionCount = connectionsPerIp.computeIfAbsent(ipAddress, ip -> new AtomicLong(0L));
        if (connectionCount.incrementAndGet() > 100L) {
            ConnectionRateLimiter.startCooldown(ipAddress);
            return false;
        }
        return true;
    }

    public static void releaseConnection(String ipAddress) {
        connectionsPerIp.computeIfPresent(ipAddress, (ip, count) -> {
            count.decrementAndGet();
            return count;
        });
    }

    private static boolean isInCooldown(String ipAddress) {
        AtomicLong cooldownEndTime = cooldownEndTimes.get(ipAddress);
        if (cooldownEndTime != null) {
            return System.currentTimeMillis() < cooldownEndTime.get();
        }
        return false;
    }

    private static void startCooldown(String ipAddress) {
        long cooldownEndTime = System.currentTimeMillis() + 60000L;
        cooldownEndTimes.put(ipAddress, new AtomicLong(cooldownEndTime));
    }
}

