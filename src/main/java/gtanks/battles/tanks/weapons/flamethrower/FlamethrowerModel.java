/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.tanks.weapons.flamethrower;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.tanks.weapons.IEntity;
import gtanks.battles.tanks.weapons.IWeapon;
import gtanks.battles.tanks.weapons.anticheats.TickableWeaponAnticheatModel;
import gtanks.battles.tanks.weapons.flamethrower.FlamethrowerEntity;
import gtanks.battles.tanks.weapons.flamethrower.effects.FlamethrowerEffectModel;
import gtanks.utils.RandomUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class FlamethrowerModel
        extends TickableWeaponAnticheatModel
        implements IWeapon {
    private FlamethrowerEntity entity;
    public BattlefieldModel bfModel;
    public BattlefieldPlayerController player;
    private boolean hasFired = false;

    public FlamethrowerModel(FlamethrowerEntity entity, BattlefieldModel bfModel, BattlefieldPlayerController player) {
        super(entity.targetDetectionInterval);
        this.entity = entity;
        this.bfModel = bfModel;
        this.player = player;
    }

    @Override
    public void startFire(String json) {
        if (this.hasFired) {
            this.bfModel.cheatDetected(this.player, this.getClass());
            return;
        }
        this.hasFired = true;
        this.bfModel.startFire(this.player);
    }

    @Override
    public void stopFire() {
        this.hasFired = false;
        this.bfModel.stopFire(this.player);
    }

    @Override
    public void fire(String json) {
        try {
            JSONObject parser = (JSONObject)new JSONParser().parse(json);
            JSONArray arrayTanks = (JSONArray)parser.get("targetsIds");
            if (!this.check((int)((Long)parser.get("tickPeriod")).longValue())) {
                this.bfModel.cheatDetected(this.player, this.getClass());
                return;
            }
            if (arrayTanks.size() == 0) {
                return;
            }
            BattlefieldPlayerController[] targetVictim = new BattlefieldPlayerController[arrayTanks.size()];
            for (int i = 0; i < arrayTanks.size(); ++i) {
                BattlefieldPlayerController target = this.bfModel.getPlayer((String)arrayTanks.get(i));
                if (target == null || (float)((int)(target.tank.position.distanceTo(this.player.tank.position) / 100.0)) > this.entity.range) continue;
                targetVictim[i] = target;
            }
            this.onTarget(targetVictim, 0);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void quickFire(String var1) {
    }

    @Override
    public void onTarget(BattlefieldPlayerController[] targetsTanks, int distance) {
        float damage = RandomUtils.getRandom(this.entity.damage_min, this.entity.damage_min) / 2.0f;
        this.bfModel.tanksKillModel.damageTank(targetsTanks[0], this.player, damage, true);
        BattlefieldPlayerController victim = targetsTanks[0];
        FlamethrowerEffectModel.player = this.player;
        FlamethrowerEffectModel.victim = victim;
        if (victim != null && victim.tank != null) {
            boolean canFlame = true;
            if (this.bfModel.battleInfo.team) {
                boolean bl = canFlame = !this.player.playerTeamType.equals(victim.playerTeamType);
            }
            if (canFlame) {
                if (victim.tank.flameEffect == null) {
                    victim.tank.flameEffect = new FlamethrowerEffectModel(this.entity.coolingSpeed, victim.tank, this.bfModel);
                }
                victim.tank.flameEffect.update();
            }
        }
    }

    @Override
    public IEntity getEntity() {
        return this.entity;
    }
}

