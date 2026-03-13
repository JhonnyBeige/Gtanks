/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.tanks.weapons.snowman;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.anticheats.AnticheatModel;
import gtanks.battles.tanks.weapons.IEntity;
import gtanks.battles.tanks.weapons.IWeapon;
import gtanks.battles.tanks.weapons.WeaponUtils;
import gtanks.battles.tanks.weapons.WeaponWeakeningData;
import gtanks.battles.tanks.weapons.anticheats.FireableWeaponAnticheatModel;
import gtanks.battles.tanks.weapons.frezee.effects.FrezeeEffectModel;
import gtanks.battles.tanks.weapons.snowman.SnowmanEntity;
import gtanks.commands.Type;
import gtanks.logger.Logger;
import gtanks.utils.RandomUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

@AnticheatModel(name="SnowmanModel", actionInfo="Child FireableWeaponAnticheatModel")
public class SnowmanModel
extends FireableWeaponAnticheatModel
implements IWeapon {
    private BattlefieldModel bfModel;
    private BattlefieldPlayerController player;
    private WeaponWeakeningData weakeingData;
    private SnowmanEntity entity;

    public SnowmanModel(SnowmanEntity snowmanEntity, WeaponWeakeningData weakeingData, BattlefieldPlayerController tank, BattlefieldModel battle) {
        super(snowmanEntity.getShotData().reloadMsec);
        this.bfModel = battle;
        this.player = tank;
        this.entity = snowmanEntity;
        this.weakeingData = weakeingData;
    }

    @Override
    public void startFire(String json) {
        this.bfModel.sendToAllPlayers(this.player, Type.BATTLE, "start_fire_snowman", this.player.tank.id, json);
    }

    @Override
    public void fire(String json) {
        this.bfModel.fire(this.player, json);
        try {
            JSONObject parser = (JSONObject)new JSONParser().parse(json);
            if (!this.check((int)((Long)parser.get("reloadTime")).longValue())) {
                this.bfModel.cheatDetected(this.player, this.getClass());
                return;
            }
            BattlefieldPlayerController victim = this.bfModel.getPlayer((String)parser.get("victimId"));
            this.onTarget(new BattlefieldPlayerController[]{victim}, (int)((Long)parser.get("distance")).longValue());
        } catch (Exception e) {
            Logger.log("Error in parsing json SnowmanModel().fire() Data: " + json);
        }
    }

    @Override
    public void quickFire(String var1) {
    }

    @Override
    public void onTarget(BattlefieldPlayerController[] targetsTanks, int distance) {
        float damage = RandomUtils.getRandom(this.entity.damage_min, this.entity.damage_max);
        if (targetsTanks.length == 0) {
            return;
        }
        if (targetsTanks[0] == null) {
            return;
        }
        if ((double)distance >= this.weakeingData.minimumDamageRadius) {
            damage = WeaponUtils.calculateDamageFromDistance(damage, (int)this.weakeingData.minimumDamagePercent);
        }
        if (targetsTanks[0].tank.frezeeEffect == null) {
            targetsTanks[0].tank.frezeeEffect = new FrezeeEffectModel(this.entity.frezeeSpeed, targetsTanks[0].tank, this.bfModel);
            targetsTanks[0].tank.frezeeEffect.setStartSpecFromTank();
        }
        targetsTanks[0].tank.frezeeEffect.update();
        this.bfModel.tanksKillModel.damageTank(targetsTanks[0], this.player, damage, true);
    }

    @Override
    public IEntity getEntity() {
        return this.entity;
    }

    @Override
    public void stopFire() {
    }
}

