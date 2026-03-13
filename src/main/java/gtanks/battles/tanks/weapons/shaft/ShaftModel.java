/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.tanks.weapons.shaft;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.tanks.weapons.IEntity;
import gtanks.battles.tanks.weapons.IWeapon;
import gtanks.battles.tanks.weapons.WeaponWeakeningData;
import gtanks.battles.tanks.weapons.anticheats.FireableWeaponAnticheatModel;
import gtanks.battles.tanks.weapons.shaft.ShaftEntity;
import gtanks.logger.Logger;
import gtanks.utils.RandomUtils;
import java.util.ArrayList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ShaftModel
extends FireableWeaponAnticheatModel
implements IWeapon {
    private BattlefieldModel bfModel;
    private BattlefieldPlayerController player;
    private ShaftEntity entity;
    private WeaponWeakeningData weakeingData;

    public ShaftModel(ShaftEntity entity, WeaponWeakeningData weakeingData, BattlefieldModel bfModel, BattlefieldPlayerController player) {
        super(entity.getShotData().reloadMsec);
        this.entity = entity;
        this.bfModel = bfModel;
        this.player = player;
        this.weakeingData = weakeingData;
    }

    @Override
    public void fire(String json) {
        JSONParser js = new JSONParser();
        JSONObject target = null;
        Number energy = 0;
        ArrayList<String> ids = null;
        try {
            Object jo = js.parse(json);
            JSONObject jsonObj = (JSONObject)jo;
            JSONArray targets = (JSONArray)jsonObj.get("targets");
            energy = (Number)jsonObj.get("energy");
            ids = new ArrayList<String>();
            for (int i = 0; i < targets.size(); ++i) {
                target = (JSONObject)targets.get(i);
                JSONObject targetObj = (JSONObject)target.get("target");
                String id = targetObj.get("id").toString();
                ids.add(id);
            }
        } catch (ParseException var5) {
            var5.printStackTrace();
        }
        this.bfModel.fire(this.player, json);
        BattlefieldPlayerController[] tanks_array = new BattlefieldPlayerController[ids.size()];
        if (target != null) {
            for (int i = 0; i < ids.size(); ++i) {
                tanks_array[i] = this.bfModel.players.get(ids.get(i));
            }
        }
        this.onTargetDamage(tanks_array, 0, energy.doubleValue());
    }

    @Override
    public void quickFire(String json) {
        JSONParser js = new JSONParser();
        JSONObject target = null;
        ArrayList<String> targetIds = null;
        try {
            Object jo = js.parse(json);
            JSONObject jsonObj = (JSONObject)jo;
            JSONArray targets = (JSONArray)jsonObj.get("targets");
            targetIds = new ArrayList<String>();
            for (int i = 0; i < targets.size(); ++i) {
                target = (JSONObject)targets.get(i);
                String targetId = target.get("target_id").toString();
                targetIds.add(targetId);
            }
        } catch (ParseException var5) {
            var5.printStackTrace();
        }
        this.bfModel.quickFire(this.player, json);
        BattlefieldPlayerController[] tanks_array = new BattlefieldPlayerController[targetIds.size()];
        if (target != null) {
            for (int i = 0; i < targetIds.size(); ++i) {
                tanks_array[i] = this.bfModel.players.get(targetIds.get(i));
            }
        }
        this.onTargetQuickShot(tanks_array, 0);
    }

    @Override
    public void startFire(String json) {
    }

    public void onTargetDamage(BattlefieldPlayerController[] targetsTanks, int distance, double energy) {
        if (targetsTanks.length != 0) {
            if (energy < 0.0) {
                Logger.debug("Nickname " + this.player.getUser().getNickname() + " ANTICHEAT: Shaft energy value is less than 0.0: " + energy + " (Kill tank)");
                this.bfModel.cheatDetected(this.player, this.getClass());
                return;
            }
            float damage = (float)((double)this.entity.fov_damage_max * ((double)this.entity.maxEnergy - energy) / (double)this.entity.maxEnergy);
            if (damage < 30.0f) {
                damage = this.entity.damage_min;
            }
            for (int i = 0; i < targetsTanks.length; ++i) {
                this.bfModel.tanksKillModel.damageTank(targetsTanks[i], this.player, damage, true);
                damage /= 2.0f;
            }
        }
    }

    public void onTargetQuickShot(BattlefieldPlayerController[] targetsTanks, int distance) {
        if (targetsTanks.length != 0) {
            float damage = RandomUtils.getRandom(this.entity.damage_min, this.entity.damage_max);
            for (int i = 0; i < targetsTanks.length; ++i) {
                this.bfModel.tanksKillModel.damageTank(targetsTanks[i], this.player, damage, true);
                damage /= 2.0f;
            }
        }
    }

    @Override
    public IEntity getEntity() {
        return this.entity;
    }

    @Override
    public void stopFire() {
    }

    @Override
    public void onTarget(BattlefieldPlayerController[] var1, int var2) {
    }
}

