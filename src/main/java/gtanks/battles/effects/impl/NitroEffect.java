/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.effects.impl;

import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.effects.Effect;
import gtanks.battles.effects.EffectType;
import gtanks.battles.effects.activator.EffectActivatorService;
import gtanks.battles.tanks.math.Vector3;
import gtanks.commands.Type;
import gtanks.json.JSONUtils;
import gtanks.services.annotations.ServicesInject;
import java.util.ArrayList;
import java.util.TimerTask;

public class NitroEffect
extends TimerTask
implements Effect {
    private static final String CHANGE_TANK_SPEC_COMAND = "change_spec_tank";
    @ServicesInject(target=EffectActivatorService.class)
    private EffectActivatorService effectActivatorService = EffectActivatorService.getInstance();
    private BattlefieldPlayerController player;
    private boolean fromInventory;
    private boolean deactivated;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void activate(BattlefieldPlayerController player, boolean fromInventory, Vector3 tankPos) {
        ArrayList<Effect> arrayList;
        this.fromInventory = fromInventory;
        this.player = player;
        ArrayList<Effect> arrayList2 = arrayList = player.tank.activeEffects;
        synchronized (arrayList2) {
            player.tank.activeEffects.add(this);
        }
        player.tank.speed = this.addPercent(player.tank.speed, 30);
        player.battle.sendToAllPlayers(Type.BATTLE, CHANGE_TANK_SPEC_COMAND, player.tank.id, JSONUtils.parseTankSpec(player.tank, true));
        this.effectActivatorService.activateEffect(this, this.fromInventory ? 60000L : 40000L);
    }

    @Override
    public void deactivate() {
        this.deactivated = true;
        this.player.tank.activeEffects.remove(this);
        this.player.battle.sendToAllPlayers(Type.BATTLE, "disnable_effect", this.player.getUser().getNickname(), String.valueOf(this.getID()));
        this.player.tank.speed = this.player.tank.getHull().speed;
        this.player.battle.sendToAllPlayers(Type.BATTLE, CHANGE_TANK_SPEC_COMAND, this.player.tank.id, JSONUtils.parseTankSpec(this.player.tank, true));
    }

    @Override
    public void run() {
        if (!this.deactivated) {
            this.deactivate();
        }
    }

    @Override
    public EffectType getEffectType() {
        return EffectType.NITRO;
    }

    @Override
    public int getID() {
        return 4;
    }

    private float addPercent(float value, int percent) {
        return value / 100.0f * (float)percent + value;
    }

    @Override
    public int getDurationTime() {
        return 60000;
    }
}

