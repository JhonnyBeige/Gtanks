/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.system.timers;

import gtanks.system.timers.InterruptedTimerEvent;
import gtanks.system.timers.exceptions.TimerCausedException;

@Deprecated
public class InterruptedTimer
implements Runnable {
    private long time;
    private boolean interrupted = false;
    private boolean caused = false;
    private InterruptedTimerEvent event;

    public void startTimer(long time, InterruptedTimerEvent event) throws TimerCausedException {
        if (this.caused) {
            throw new TimerCausedException("InterruptedTimer Has already been called");
        }
        this.caused = true;
        this.time = time;
        this.event = event;
        new Thread(this).start();
    }

    private void onFinishTimer() {
        this.event.onComplete();
    }

    @Override
    public void run() {
        try {
            Thread.sleep(this.time);
            if (!this.interrupted) {
                this.onFinishTimer();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

