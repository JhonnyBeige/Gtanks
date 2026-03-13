/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.main.netty;

import gtanks.logger.Logger;
import gtanks.logger.remote.RemoteDatabaseLogger;
import gtanks.main.netty.ConnectionRateLimiter;
import gtanks.main.netty.NettyUsersHandlerController;
import gtanks.main.netty.blackip.model.BlackIPsModel;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

public class NettyUsersHandler
extends SimpleChannelUpstreamHandler {
    private final NettyUsersHandlerController controller = new NettyUsersHandlerController();
    private static final BlackIPsModel blackList = new BlackIPsModel();

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        super.handleUpstream(ctx, e);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        String ipAddress = LocalhostBypass.extractIp(ctx.getChannel().getRemoteAddress());
        boolean localhost = LocalhostBypass.isLocalhost(ctx.getChannel().getRemoteAddress());
        if (!localhost && !ConnectionRateLimiter.allowConnection(ipAddress)) {
            ctx.getChannel().close();
            Logger.log("[ANTI_DDOS_SYSTEM]: IP: " + ipAddress + " banned due to limited connections.");
            NettyUsersHandler.block(ipAddress);
            return;
        }
        if (!localhost && blackList.contains(ipAddress)) {
            ctx.getChannel().close();
            return;
        }
        this.controller.onClientConnected(ctx);
        Logger.log("New Connection Established - IP: " + ipAddress);
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
        String ipAddress = LocalhostBypass.extractIp(ctx.getChannel().getRemoteAddress());
        ConnectionRateLimiter.releaseConnection(ipAddress);
        this.controller.onClientDisconnect(ctx);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        this.controller.onMessageRecived(ctx, e);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        try {
            StringWriter sw = new StringWriter();
            e.getCause().printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            RemoteDatabaseLogger.error(exceptionAsString);
            if (ctx.getChannel().isConnected()) {
                ctx.getChannel().close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            RemoteDatabaseLogger.error(ex);
        }
    }

    public static void block(String ip) {
        ip = LocalhostBypass.normalizeIp(ip);
        if (LocalhostBypass.isLocalhost(ip)) {
            return;
        }
        blackList.block(ip);
    }

    public static void unblock(String ip) {
        ip = LocalhostBypass.normalizeIp(ip);
        blackList.unblock(ip);
    }


}
