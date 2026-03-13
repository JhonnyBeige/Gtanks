/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.effects.impl;

import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.effects.Effect;
import gtanks.battles.effects.EffectType;
import gtanks.battles.tanks.math.Vector3;
import gtanks.battles.tanks.weapons.frezee.effects.FrezeeEffectModel;
import gtanks.commands.Type;

public class HealthEffect
extends Thread
implements Effect {
    private int resource;
    private int accumulatedResource;
    private BattlefieldPlayerController player;
    private boolean fromInventory;
    private boolean deactivated;

    @Override
    public void activate(BattlefieldPlayerController player, boolean fromInventory, Vector3 tankPos) {
        this.fromInventory = fromInventory;
        this.player = player;
        this.resource = fromInventory ? (int)player.tank.getHull().hp : (int)player.tank.getHull().hp / 2;
        player.tank.activeEffects.add(this);
        this.start();
        this.removeFrezeeEffectIfPresent();
    }

    @Override
    public void deactivate() {
        this.deactivated = true;
        this.player.tank.activeEffects.remove(this);
        this.player.battle.sendToAllPlayers(Type.BATTLE, "disnable_effect", this.player.getUser().getNickname(), String.valueOf(this.getID()));
    }

    private void removeFrezeeEffectIfPresent() {
        FrezeeEffectModel frezeeEffect = this.player.tank.frezeeEffect;
        if (frezeeEffect != null) {
            frezeeEffect.removeFrezeeEffect();
            this.player.tank.frezeeEffect = null;
        }
    }

    @Override
    public void run() {
        try {
            while (!this.deactivated) {
                if (this.accumulatedResource + 15 > this.resource) {
                    this.healTank(this.resource - this.accumulatedResource);
                    break;
                }
                this.healTank(15);
                this.accumulatedResource += 15;
                HealthEffect.sleep(500L);
                if (this.accumulatedResource <= this.resource) continue;
            }
            if (!this.deactivated) {
                this.deactivate();
            }
        } catch (InterruptedException var2) {
            var2.printStackTrace();
        }
    }

    private void healTank(int hp) {
        this.player.battle.tanksKillModel.healPlayer(null, this.player, hp);
    }

    @Override
    public EffectType getEffectType() {
        return EffectType.HEALTH;
    }

    @Override
    public int getID() {
        return 1;
    }

    @Override
    public int getDurationTime() {
        return 5000;
    }
}

