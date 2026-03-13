/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.system.timers;

import gtanks.system.timers.TimerTaskExecutor;
import java.util.Timer;
import java.util.TimerTask;

public class SystemTimerScheduler {
    private static final Timer TIMER = new Timer("SystemTimerScheduler timer");

    public static void scheduleTask(final TimerTaskExecutor task, long delay) {
        TIMER.schedule(new TimerTask(){

            @Override
            public void run() {
                try {
                    task.run();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, delay);
    }
}

