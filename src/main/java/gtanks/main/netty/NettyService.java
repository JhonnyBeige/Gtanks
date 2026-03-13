/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.main.netty;

import gtanks.configurator.osgi.OSGi;
import gtanks.configurator.server.configuration.entitys.NettyConfiguratorEntity;
import gtanks.logger.Logger;
import gtanks.main.netty.NettyPipelineFactory;
import gtanks.system.destroy.Destroyable;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

public class NettyService
implements Destroyable {
    private static final NettyService instance = new NettyService();
    public int port;
    private final ServerBootstrap bootstrap;

    private NettyService() {
        this.initParams();
        OrderedMemoryAwareThreadPoolExecutor bossExec = new OrderedMemoryAwareThreadPoolExecutor(1, 400000000L, 2000000000L, 60L, TimeUnit.SECONDS);
        OrderedMemoryAwareThreadPoolExecutor ioExec = new OrderedMemoryAwareThreadPoolExecutor(4, 400000000L, 2000000000L, 60L, TimeUnit.SECONDS);
        NioServerSocketChannelFactory factory = new NioServerSocketChannelFactory(bossExec, ioExec, 4);
        this.bootstrap = new ServerBootstrap(factory);
        this.bootstrap.setPipelineFactory(new NettyPipelineFactory());
        this.bootstrap.setOption("child.tcpNoDelay", true);
        this.bootstrap.setOption("child.keepAlive", true);
    }

    public void init() {
        this.bootstrap.bind(new InetSocketAddress(this.port));
        Logger.log("[Netty] Server run on port: " + this.port);
    }

    @Override
    public void destroy() {
        System.exit(0);
        this.bootstrap.releaseExternalResources();
    }

    public static NettyService inject() {
        return instance;
    }

    private void initParams() {
        this.port = ((NettyConfiguratorEntity)OSGi.getModelByInterface(NettyConfiguratorEntity.class)).getPort();
    }
}

