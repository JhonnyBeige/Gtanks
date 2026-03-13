/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.network;

import org.jboss.netty.channel.ChannelHandlerContext;

public class Session {
    private ChannelHandlerContext context;

    public Session(ChannelHandlerContext context) {
        this.context = context;
    }

    public String getIp() {
        return this.context.getChannel().getRemoteAddress().toString();
    }
}

