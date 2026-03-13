/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.tanks.weapons.thunder;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.anticheats.AnticheatModel;
import gtanks.battles.tanks.weapons.IEntity;
import gtanks.battles.tanks.weapons.IWeapon;
import gtanks.battles.tanks.weapons.anticheats.FireableWeaponAnticheatModel;
import gtanks.battles.tanks.weapons.thunder.ThunderEntity;
import gtanks.commands.Type;
import gtanks.utils.RandomUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@AnticheatModel(name="ThunderModel", actionInfo="Child FireableWeaponAnticheatModel")
public class ThunderModel
extends FireableWeaponAnticheatModel
implements IWeapon {
    private ThunderEntity entity;
    private BattlefieldModel bfModel;
    private BattlefieldPlayerController player;

    public ThunderModel(ThunderEntity entity, BattlefieldModel bfModel, BattlefieldPlayerController player) {
        super(entity.getShotData().reloadMsec);
        this.entity = entity;
        this.bfModel = bfModel;
        this.player = player;
    }

    @Override
    public void fire(String json) {
        JSONObject parser = null;
        try {
            parser = (JSONObject)new JSONParser().parse(json);
        } catch (ParseException var10) {
            var10.printStackTrace();
        }
        if (!this.check((int)((Long)parser.get("reloadTime")).longValue())) {
            this.bfModel.cheatDetected(this.player, this.getClass());
        } else {
            this.bfModel.sendToAllPlayers(this.player, Type.BATTLE, "fire", this.player.tank.id, json);
            String mainTargetId = (String)parser.get("mainTargetId");
            if (mainTargetId != null) {
                this.onTarget(new BattlefieldPlayerController[]{this.bfModel.getPlayer(mainTargetId)}, (int)((Long)parser.get("distance")).longValue());
            }
            JSONArray splashVictims = (JSONArray)parser.get("splashTargetIds");
            JSONArray splashVictimsDistances = (JSONArray)parser.get("splashTargetDistances");
            if (splashVictimsDistances != null) {
                for (Object obj : splashVictimsDistances) {
                    if (!(obj instanceof Number) || ((Number)obj).doubleValue() % 1.0 != 0.0) continue;
                    this.bfModel.cheatDetected(this.player, this.getClass());
                    break;
                }
            }
            if (splashVictims != null && splashVictims.size() > 0) {
                for (int i = 0; i < splashVictims.size(); ++i) {
                    String victimid = (String)splashVictims.get(i);
                    float distance = (float)((Double)splashVictimsDistances.get(i)).doubleValue();
                    float damage = RandomUtils.getRandom(this.entity.damage_min, this.entity.damage_min);
                    if (distance >= this.entity.minSplashDamageRadius && damage <= this.entity.maxSplashDamageRadius) {
                        damage -= damage / 100.0f * 25.0f;
                    }
                    if (!(distance <= this.entity.maxSplashDamageRadius)) continue;
                    this.bfModel.tanksKillModel.damageTank(this.bfModel.getPlayer(victimid), this.player, damage, true);
                }
            }
        }
    }

    @Override
    public void quickFire(String var1) {
    }

    @Override
    public void onTarget(BattlefieldPlayerController[] targetsTanks, int distance) {
        float damage = RandomUtils.getRandom(this.entity.damage_min, this.entity.damage_max);
        this.bfModel.tanksKillModel.damageTank(targetsTanks[0], this.player, damage, true);
    }

    @Override
    public IEntity getEntity() {
        return this.entity;
    }

    @Override
    public void startFire(String json) {
    }

    @Override
    public void stopFire() {
    }
}

