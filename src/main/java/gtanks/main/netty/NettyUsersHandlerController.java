/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.main.netty;

import gtanks.main.netty.ProtocolTransfer;
import java.util.HashMap;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class NettyUsersHandlerController
extends HashMap<ChannelHandlerContext, ProtocolTransfer>
implements Runnable {
    private static final long serialVersionUID = 4922899768061891423L;

    public NettyUsersHandlerController() {
        Thread _thread = new Thread(this);
        _thread.setName("NettyUsersHandlerController THREAD");
        _thread.start();
    }

    public void onClientConnected(ChannelHandlerContext ctx) {
        this.put(ctx, new ProtocolTransfer(ctx.getChannel(), ctx));
    }

    public void onClientDisconnect(ChannelHandlerContext ctx) {
        if (this.get(ctx) != null) {
            ((ProtocolTransfer)this.get(ctx)).onDisconnect();
            this.remove(ctx);
        }
    }

    public void onMessageRecived(ChannelHandlerContext ctx, MessageEvent msg) {
        ((ProtocolTransfer)this.get(ctx)).decryptProtocol((String)msg.getMessage());
    }

    @Override
    public void run() {
    }
}

