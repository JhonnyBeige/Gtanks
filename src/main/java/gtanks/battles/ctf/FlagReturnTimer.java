/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.ctf;

import gtanks.battles.ctf.CTFModel;
import gtanks.battles.ctf.flags.FlagServer;

public class FlagReturnTimer
extends Thread {
    public boolean stop = false;
    private CTFModel ctfModel;
    private FlagServer flag;

    public FlagReturnTimer(CTFModel ctfModel, FlagServer flag) {
        super.setName("FlagReturnTimer THREAD");
        this.ctfModel = ctfModel;
        this.flag = flag;
    }

    @Override
    public void run() {
        try {
            FlagReturnTimer.sleep(20000L);
            if (!this.stop) {
                this.ctfModel.returnFlag(null, this.flag);
            }
        } catch (InterruptedException var2) {
            var2.printStackTrace();
        }
    }
}

