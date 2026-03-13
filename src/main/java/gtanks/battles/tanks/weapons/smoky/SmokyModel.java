/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.tanks.weapons.smoky;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.anticheats.AnticheatModel;
import gtanks.battles.tanks.weapons.IEntity;
import gtanks.battles.tanks.weapons.IWeapon;
import gtanks.battles.tanks.weapons.WeaponUtils;
import gtanks.battles.tanks.weapons.WeaponWeakeningData;
import gtanks.battles.tanks.weapons.anticheats.FireableWeaponAnticheatModel;
import gtanks.battles.tanks.weapons.smoky.SmokyEntity;
import gtanks.utils.RandomUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@AnticheatModel(name="SmokyModel", actionInfo="Child FireableWeaponAnticheatModel")
public class SmokyModel
extends FireableWeaponAnticheatModel
implements IWeapon {
    private BattlefieldModel bfModel;
    private BattlefieldPlayerController player;
    private SmokyEntity entity;
    private WeaponWeakeningData weakeingData;

    public SmokyModel(SmokyEntity entity, WeaponWeakeningData weakeingData, BattlefieldModel bfModel, BattlefieldPlayerController player) {
        super(entity.getShotData().reloadMsec);
        this.entity = entity;
        this.bfModel = bfModel;
        this.player = player;
        this.weakeingData = weakeingData;
    }

    @Override
    public void fire(String json) {
        JSONParser js = new JSONParser();
        JSONObject jo = null;
        try {
            jo = (JSONObject)js.parse(json);
        } catch (ParseException var5) {
            var5.printStackTrace();
        }
        if (!this.check((int)((Long)jo.get("reloadTime")).longValue())) {
            this.bfModel.cheatDetected(this.player, this.getClass());
        }
        this.bfModel.fire(this.player, json);
        BattlefieldPlayerController victim = this.bfModel.players.get(jo.get("victimId"));
        if (victim != null) {
            this.onTarget(new BattlefieldPlayerController[]{victim}, (int)Double.parseDouble(String.valueOf(jo.get("distance"))));
        }
    }

    @Override
    public void quickFire(String var1) {
    }

    @Override
    public void startFire(String json) {
    }

    @Override
    public void onTarget(BattlefieldPlayerController[] targetsTanks, int distance) {
        if (targetsTanks.length != 0) {
            if (targetsTanks.length > 1) {
                // empty if block
            }
            float damage = RandomUtils.getRandom(this.entity.damage_min, this.entity.damage_max);
            if ((double)distance >= this.weakeingData.minimumDamageRadius) {
                damage = WeaponUtils.calculateDamageFromDistance(damage, (int)this.weakeingData.minimumDamagePercent);
            }
            this.bfModel.tanksKillModel.damageTank(targetsTanks[0], this.player, damage, true);
        }
    }

    @Override
    public IEntity getEntity() {
        return this.entity;
    }

    @Override
    public void stopFire() {
    }
}

