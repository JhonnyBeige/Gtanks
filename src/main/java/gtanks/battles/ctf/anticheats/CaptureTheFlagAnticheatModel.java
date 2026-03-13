/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.ctf.anticheats;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.ctf.flags.FlagServer;
import gtanks.battles.ctf.flags.FlagState;
import java.util.HashMap;

public class CaptureTheFlagAnticheatModel {
    private final HashMap<BattlefieldPlayerController, Data> datas = new HashMap();
    private final BattlefieldModel bfModel;

    public CaptureTheFlagAnticheatModel(BattlefieldModel bfModel) {
        this.bfModel = bfModel;
    }

    public boolean onTakeFlag(BattlefieldPlayerController taker, FlagServer flag) {
        Data data = this.datas.get(taker);
        if (data == null) {
            data = new Data();
            this.datas.put(taker, data);
        }
        data.lastTimeTakeFlag = System.currentTimeMillis();
        data.prevState = flag.state;
        return false;
    }

    public boolean onDeliveredFlag(BattlefieldPlayerController taker) {
        Data data = this.datas.get(taker);
        long time = System.currentTimeMillis() - data.lastTimeTakeFlag;
        if (time <= 4000L && data.prevState == FlagState.BASE) {
            this.bfModel.cheatDetected(taker, this.getClass());
            return true;
        }
        if (time <= 4000L && data.prevState == FlagState.DROPED) {
            this.bfModel.cheatDetected(taker, this.getClass());
            return true;
        }
        return false;
    }

    static class Data {
        long lastTimeTakeFlag;
        FlagState prevState;

        Data() {
        }
    }
}

