package gtanks.services;

import gtanks.commands.Type;
import gtanks.main.netty.ProtocolTransfer;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class PacketDelayService {
    private static final long GARAGE_PACKET_DELAY_MS = 500L;
    private static final long BATTLE_INIT_TANK_PACKET_DELAY_MS = 4955L;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory(){

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "garage-packet-delay");
            thread.setDaemon(true);
            return thread;
        }
    });

    public void send(ProtocolTransfer networker, Type type, String ... args) throws IOException {
        if (type == Type.GARAGE) {
            String[] delayedArgs = Arrays.copyOf(args, args.length);
            this.scheduler.schedule(() -> {
                try {
                    networker.send(type, delayedArgs);
                }
                catch (IOException iOException) {
                    // empty catch block
                }
            }, GARAGE_PACKET_DELAY_MS, TimeUnit.MILLISECONDS);
            return;
        }
        if (type == Type.BATTLE && args.length > 0 && "init_tank".equals(args[0])) {
            String[] delayedArgs = Arrays.copyOf(args, args.length);
            this.scheduler.schedule(() -> {
                try {
                    networker.send(type, delayedArgs);
                }
                catch (IOException iOException) {
                    // empty catch block
                }
            }, BATTLE_INIT_TANK_PACKET_DELAY_MS, TimeUnit.MILLISECONDS);
            return;
        }
        networker.send(type, args);
    }

    public void shutdown() {
        this.scheduler.shutdownNow();
    }
}
