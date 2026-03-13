package gtanks.main.netty;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public final class LocalhostBypass {
    private LocalhostBypass() {
    }

    public static String extractIp(SocketAddress remoteAddress) {
        if (remoteAddress == null) {
            return "";
        }
        if (remoteAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress)remoteAddress;
            InetAddress inetAddress = inetSocketAddress.getAddress();
            if (inetAddress != null) {
                return inetAddress.getHostAddress();
            }
            return LocalhostBypass.normalizeIp(inetSocketAddress.getHostString());
        }
        return LocalhostBypass.normalizeIp(remoteAddress.toString());
    }

    public static String normalizeIp(String ip) {
        if (ip == null) {
            return "";
        }
        String normalizedIp = ip;
        while (normalizedIp.startsWith("/")) {
            normalizedIp = normalizedIp.substring(1);
        }
        if (normalizedIp.startsWith("[") && normalizedIp.contains("]")) {
            return normalizedIp.substring(1, normalizedIp.indexOf("]"));
        }
        int lastColon = normalizedIp.lastIndexOf(":");
        if (lastColon > 0 && normalizedIp.indexOf(":") == lastColon) {
            return normalizedIp.substring(0, lastColon);
        }
        return normalizedIp;
    }

    public static boolean isLocalhost(SocketAddress remoteAddress) {
        if (remoteAddress instanceof InetSocketAddress) {
            InetAddress inetAddress = ((InetSocketAddress)remoteAddress).getAddress();
            if (inetAddress != null && inetAddress.isLoopbackAddress()) {
                return true;
            }
        }
        return LocalhostBypass.isLocalhost(LocalhostBypass.extractIp(remoteAddress));
    }

    public static boolean isLocalhost(String ip) {
        String normalizedIp = LocalhostBypass.normalizeIp(ip);
        return "127.0.0.1".equals(normalizedIp)
                || "::1".equals(normalizedIp)
                || "0:0:0:0:0:0:0:1".equals(normalizedIp)
                || "localhost".equalsIgnoreCase(normalizedIp);
    }
}
