/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.main.netty;

import gtanks.main.netty.NettyUsersHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

import java.nio.charset.StandardCharsets;

public class NettyPipelineFactory
        implements ChannelPipelineFactory {
    @Override
    public ChannelPipeline getPipeline() {
        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("decoder", new StringDecoder(StandardCharsets.UTF_8));
        pipeline.addLast("encoder", new StringEncoder(StandardCharsets.UTF_8));
        pipeline.addLast("handler", new NettyUsersHandler());
        return pipeline;
    }
}