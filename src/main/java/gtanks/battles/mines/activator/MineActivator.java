/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.mines.activator;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.mines.ServerMine;
import gtanks.commands.Type;
import gtanks.json.JSONUtils;
import java.util.TimerTask;

public class MineActivator
extends TimerTask {
    private final BattlefieldModel bfModel;
    private final ServerMine mine;

    public MineActivator(BattlefieldModel bfModel, ServerMine mine) {
        this.bfModel = bfModel;
        this.mine = mine;
    }

    public void putMine() {
        this.bfModel.sendToAllPlayers(Type.BATTLE, "put_mine", JSONUtils.parsePutMineComand(this.mine));
    }

    public void activateMine() {
        this.bfModel.sendToAllPlayers(Type.BATTLE, "activate_mine", this.mine.getId());
    }

    @Override
    public void run() {
        this.activateMine();
    }
}

