/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.tanks.weapons.isida;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.tanks.weapons.IEntity;
import gtanks.battles.tanks.weapons.IWeapon;
import gtanks.battles.tanks.weapons.anticheats.TickableWeaponAnticheatModel;
import gtanks.battles.tanks.weapons.isida.IsidaEntity;
import gtanks.commands.Type;
import gtanks.services.TanksServices;
import gtanks.services.annotations.ServicesInject;
import gtanks.utils.RandomUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class IsidaTESTModel
extends TickableWeaponAnticheatModel
implements IWeapon {
    @ServicesInject(target=TanksServices.class)
    private static TanksServices tanksServices = TanksServices.getInstance();
    private BattlefieldModel bfModel;
    private BattlefieldPlayerController player;
    private IsidaEntity entity;
    private double accumulatedPointsForHealing = 0.0;
    private static final double HEALER_POINTS_KOEFF = 90.0;

    public IsidaTESTModel(IsidaEntity entity, BattlefieldPlayerController player, BattlefieldModel bfModel) {
        super(entity.tickPeriod);
        this.bfModel = bfModel;
        this.player = player;
        this.entity = entity;
    }

    @Override
    public void startFire(String json) {
        JSONObject obj = new JSONObject();
        JSONObject parser = null;
        try {
            parser = (JSONObject)new JSONParser().parse(json);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String shotType = "";
        String victimId = (String)parser.get("victimId");
        BattlefieldPlayerController victim = this.bfModel.getPlayer(victimId);
        shotType = victimId == null || victimId.isEmpty() ? "idle" : (this.bfModel.battleInfo.team && this.player.playerTeamType.equals(victim.playerTeamType) ? "heal" : "damage");
        obj.put("type", shotType);
        obj.put("shooterId", this.player.getUser().getNickname());
        obj.put("targetId", victimId);
        this.bfModel.sendToAllPlayers(this.player, Type.BATTLE, "start_fire", this.player.getUser().getNickname(), obj.toJSONString());
    }

    @Override
    public void fire(String json) {
        JSONObject parser = null;
        try {
            parser = (JSONObject)new JSONParser().parse(json);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.check((int)((Long)parser.get("tickPeriod")).longValue());
        String victimId = (String)parser.get("victimId");
        if (victimId == null || victimId.isEmpty()) {
            return;
        }
        BattlefieldPlayerController target = this.bfModel.getPlayer(victimId);
        if (target == null) {
            return;
        }
        if ((float)((int)(target.tank.position.distanceTo(this.player.tank.position) / 100.0)) > this.entity.maxRadius) {
            return;
        }
        this.onTarget(new BattlefieldPlayerController[]{target}, (int)((Long)parser.get("distance")).longValue());
    }

    @Override
    public void quickFire(String var1) {
    }

    @Override
    public void stopFire() {
        this.bfModel.stopFire(this.player);
        this.calculateHealedScore();
    }

    private void calculateHealedScore() {
        if (this.accumulatedPointsForHealing > 0.0) {
            tanksServices.addScore(this.player.parentLobby, (int)this.accumulatedPointsForHealing);
        }
        this.player.statistic.addScore((int)this.accumulatedPointsForHealing);
        this.bfModel.statistics.changeStatistic(this.player);
        this.accumulatedPointsForHealing = 0.0;
    }

    private void addScoreForHealing(float healedPoint, BattlefieldPlayerController patient) {
        int patientRating = patient.getUser().getRang();
        int healerRating = this.player.getUser().getRang();
        double scorePoints = Math.atan((patientRating - healerRating) / (healerRating + 1) + 1) / Math.PI * 90.0 * (double)healedPoint / 100.0;
        this.accumulatedPointsForHealing += scorePoints;
    }

    @Override
    public void onTarget(BattlefieldPlayerController[] targetsTanks, int distance) {
        if (distance != 2500) {
            this.bfModel.cheatDetected(this.player, this.getClass());
        }
        float damage = RandomUtils.getRandom(this.entity.damage_min, this.entity.damage_min) / 2.0f;
        if (!this.bfModel.battleInfo.team || !this.player.playerTeamType.equals(targetsTanks[0].playerTeamType)) {
            this.bfModel.tanksKillModel.damageTank(targetsTanks[0], this.player, damage, true);
            this.bfModel.tanksKillModel.healPlayer(this.player, this.player, damage / 2.0f);
        } else if (this.bfModel.tanksKillModel.healPlayer(this.player, targetsTanks[0], damage)) {
            this.addScoreForHealing(damage, targetsTanks[0]);
        }
    }

    @Override
    public IEntity getEntity() {
        return this.entity;
    }
}

