/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.dom.DominationPoint;
import gtanks.battles.effects.EffectType;
import gtanks.battles.tanks.Tank;
import gtanks.battles.tanks.data.DamageTankData;
import gtanks.battles.tanks.statistic.prizes.BattlePrizeCalculate;
import gtanks.battles.tanks.weapons.WeaponUtils;
import gtanks.commands.Type;
import gtanks.json.JSONUtils;
import gtanks.lobby.battles.BattleInfo;
import gtanks.services.TanksServices;
import gtanks.services.annotations.ServicesInject;
import gtanks.system.destroy.Destroyable;
import gtanks.system.quartz.QuartzService;
import gtanks.system.quartz.TimeType;
import gtanks.system.quartz.impl.QuartzServiceImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class TankKillModel
implements Destroyable {
    private static final String QUARTZ_GROUP = TankKillModel.class.getName();
    private final String QUARTZ_NAME;
    private BattlefieldModel bfModel;
    @ServicesInject(target=TanksServices.class)
    private TanksServices tanksServices = TanksServices.getInstance();
    @ServicesInject(target=QuartzService.class)
    private QuartzService quartzService = QuartzServiceImpl.inject();
    private double battleFund;
    private BattleInfo battleInfo;
    private boolean battleEnded = false;

    public TankKillModel(BattlefieldModel bfModel) {
        this.bfModel = bfModel;
        this.battleInfo = bfModel.battleInfo;
        this.QUARTZ_NAME = "TankKillModel " + this.hashCode() + " battle=" + this.battleInfo.battleId;
    }

    public synchronized void damageTank(BattlefieldPlayerController controller, BattlefieldPlayerController damager, float damage, boolean considerDD) {
        if (controller == null || damager == null) {
            return;
        }
        Tank tank = controller.tank;
        if (tank.state.equals("newcome") || tank.state.equals("suicide")) {
            return;
        }
        if (this.bfModel.battleInfo.team && controller != damager && controller.playerTeamType.equals(damager.playerTeamType) && !this.bfModel.battleInfo.friendlyFire) {
            return;
        }
        Integer resistance = controller.tank.getColormap().getResistance(damager.tank.getWeapon().getEntity().getType());
        damage = WeaponUtils.calculateDamageWithResistance(damage, resistance == null ? 0 : resistance);
        if (tank.isUsedEffect(EffectType.ARMOR)) {
            damage /= 2.0f;
        }
        if (damager.tank.isUsedEffect(EffectType.DAMAGE) && considerDD) {
            damage *= 2.0f;
        }
        DamageTankData lastDamage = tank.lastDamagers.get(damager);
        DamageTankData damageData = new DamageTankData();
        damageData.damage = damage;
        damageData.timeDamage = System.currentTimeMillis();
        damageData.damager = damager;
        if (lastDamage != null) {
            damageData.damage += lastDamage.damage;
        }
        if (damager.tank.isUsedEffect(EffectType.DAMAGE) && considerDD) {
            damageData.damage /= 2.0f;
        }
        if (controller != damager) {
            tank.lastDamagers.put(damager, damageData);
        }
        tank.health -= WeaponUtils.calculateHealth(tank, damage);
        this.changeHealth(tank, tank.health);
        if (tank.health <= 0) {
            tank.health = 0;
            this.killTank(controller, damager);
        }
    }

    public boolean healPlayer(BattlefieldPlayerController healer, BattlefieldPlayerController target, float addHeal) {
        Tank targetTank = target.tank;
        if (targetTank.state.equals("newcome") || targetTank.state.equals("suicide")) {
            return false;
        }
        if (targetTank.health >= 10000) {
            return false;
        }
        targetTank.health += WeaponUtils.calculateHealth(targetTank, addHeal);
        if (targetTank.health >= 10000) {
            targetTank.health = 10000;
        }
        this.changeHealth(targetTank, targetTank.health);
        return true;
    }

    public void changeHealth(Tank tank, int value) {
        if (tank != null) {
            tank.health = value;
            this.bfModel.sendToAllPlayers(Type.BATTLE, "change_health", tank.id, String.valueOf(tank.health));
        }
    }

    public synchronized void killTank(BattlefieldPlayerController controller, BattlefieldPlayerController killer) {
        Tank tank = controller.tank;
        tank.state = "suicide";
        tank.getWeapon().stopFire();
        controller.clearEffects();
        controller.send(Type.BATTLE, "local_user_killed");
        if (killer == null) {
            this.bfModel.sendToAllPlayers(Type.BATTLE, "kill_tank", tank.id, "suicide");
            controller.getUser().setDeaths(controller.getUser().getDeaths() + 1);
            this.tanksServices.updateRatingData(controller.parentLobby);
        } else {
            this.bfModel.sendToAllPlayers(Type.BATTLE, "kill_tank", tank.id, "killed", killer.tank.id);
            killer.getUser().setKills(killer.getUser().getKills() + 1);
            this.tanksServices.updateRatingData(killer.parentLobby);
            if (controller == killer) {
                controller.statistic.addDeaths();
                controller.getUser().setDeaths(controller.getUser().getDeaths() + 1);
                this.tanksServices.updateRatingData(controller.parentLobby);
                this.bfModel.statistics.changeStatistic(controller);
            } else {
                if (this.bfModel.battleInfo.team) {
                    if (controller.playerTeamType.equals(killer.playerTeamType)) {
                        if (this.bfModel.battleInfo.friendlyFire) {
                            controller.statistic.addDeaths();
                            controller.getUser().setDeaths(controller.getUser().getDeaths() + 1);
                            this.tanksServices.updateRatingData(controller.parentLobby);
                            this.bfModel.statistics.changeStatistic(controller);
                        }
                    } else {
                        killer.statistic.addKills(false);
                        controller.statistic.addDeaths();
                        controller.getUser().setDeaths(controller.getUser().getDeaths() + 1);
                        this.tanksServices.updateRatingData(controller.parentLobby);
                        if (killer.playerTeamType.equals("BLUE")) {
                            if (!this.battleInfo.battleType.equals("CTF") && !this.battleInfo.battleType.equals("DOM")) {
                                ++this.bfModel.battleInfo.scoreBlue;
                            }
                            if (this.battleInfo.battleType.equals("TDM")) {
                                this.bfModel.sendToAllPlayers(Type.BATTLE, "change_team_scores", "BLUE", String.valueOf(this.bfModel.battleInfo.scoreBlue));
                            }
                        } else if (killer.playerTeamType.equals("RED")) {
                            if (!this.battleInfo.battleType.equals("CTF") && !this.battleInfo.battleType.equals("DOM")) {
                                ++this.bfModel.battleInfo.scoreRed;
                            }
                            if (this.battleInfo.battleType.equals("TDM")) {
                                this.bfModel.sendToAllPlayers(Type.BATTLE, "change_team_scores", "RED", String.valueOf(this.bfModel.battleInfo.scoreRed));
                            }
                        }
                        this.bfModel.statistics.changeStatistic(killer);
                        this.bfModel.statistics.changeStatistic(controller);
                    }
                } else {
                    killer.statistic.addKills(true);
                    controller.statistic.addDeaths();
                    this.bfModel.statistics.changeStatistic(killer);
                    this.bfModel.statistics.changeStatistic(controller);
                    controller.getUser().setDeaths(controller.getUser().getDeaths() + 1);
                    this.tanksServices.updateRatingData(controller.parentLobby);
                }
                if (this.bfModel.battleInfo.team) {
                    LinkedHashMap<BattlefieldPlayerController, DamageTankData> lastDamagers = controller.tank.lastDamagers;
                    if (lastDamagers.size() <= 1) {
                        this.tanksServices.addScore(killer.parentLobby, 10);
                        if (this.bfModel.battleInfo.team) {
                            killer.statistic.addScore(10);
                            this.bfModel.statistics.changeStatistic(killer);
                        }
                    } else {
                        DamageTankData damager1 = (DamageTankData)((HashMap)lastDamagers).get(((HashMap)lastDamagers).keySet().toArray()[lastDamagers.size() - 1]);
                        DamageTankData damager2 = (DamageTankData)((HashMap)lastDamagers).get(((HashMap)lastDamagers).keySet().toArray()[lastDamagers.size() - 2]);
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - damager1.timeDamage <= 10000L && currentTime - damager2.timeDamage <= 10000L) {
                            int score1 = 0;
                            int score2 = 0;
                            if (damager1.damage > damager2.damage) {
                                score1 = (int)(0.15 * (double)(100.0f / (controller.tank.getHull().hp / damager1.damage)));
                                score2 = 15 - score1;
                            } else if (damager2.damage > damager1.damage) {
                                score2 = (int)(0.15 * (double)(100.0f / (controller.tank.getHull().hp / damager2.damage)));
                                score1 = 15 - score2;
                            } else {
                                score1 = 7;
                                score2 = 7;
                            }
                            score1 = Math.abs(score1);
                            score2 = Math.abs(score2);
                            if (this.bfModel.battleInfo.team) {
                                damager1.damager.statistic.addScore(score1);
                                damager2.damager.statistic.addScore(score2);
                                this.bfModel.statistics.changeStatistic(damager1.damager);
                                this.bfModel.statistics.changeStatistic(damager2.damager);
                            }
                            this.tanksServices.addScore(damager1.damager.parentLobby, score1);
                            this.tanksServices.addScore(damager2.damager.parentLobby, score2);
                        } else {
                            killer.statistic.addScore(10);
                            this.bfModel.statistics.changeStatistic(killer);
                            this.tanksServices.addScore(killer.parentLobby, 10);
                        }
                    }
                } else {
                    int killScore = 10;
                    if (controller.flag != null) {
                        killScore *= 2;
                    }
                    this.tanksServices.addScore(killer.parentLobby, killScore);
                    if (this.battleInfo.battleType.equals("DM")) {
                        if (killer.getGarage().containsItem("up_score_start")) {
                            this.tanksServices.addScore(killer.parentLobby, 3);
                        } else if (killer.getGarage().containsItem("up_score")) {
                            this.tanksServices.addScore(killer.parentLobby, 3);
                        }
                    }
                }
                this.addFund(0.087 * (double)(killer.getUser().getRang() + 1) + 0.01);
            }
            if (this.battleInfo.numKills > 0 && killer.statistic.getKills() >= (long)this.battleInfo.numKills) {
                this.restartBattle(false);
            }
            if (controller.flag != null) {
                this.bfModel.ctfModel.dropFlag(controller, controller.tank.position);
            }
            if (this.bfModel.domModel != null) {
                String pointId = null;
                for (DominationPoint point : this.bfModel.domModel.getPoints()) {
                    if (!point.getUserIds().contains(controller.getUser().getNickname())) continue;
                    pointId = point.getId();
                    break;
                }
                if (pointId != null) {
                    this.bfModel.domModel.tankLeaveCapturingPoint(controller, pointId);
                }
            }
        }
        this.bfModel.statistics.changeStatistic(controller);
        this.bfModel.respawnPlayer(controller, false);
        controller.tank.lastDamagers.clear();
    }

    public void restartBattle(boolean timeLimitFinish) {
        if (!this.battleEnded) {
            if (!timeLimitFinish && this.battleInfo.time > 0) {
                this.quartzService.deleteJob(this.bfModel.QUARTZ_NAME, BattlefieldModel.QUARTZ_GROUP);
            }
            this.calculatePrizes();
            this.bfModel.battleFinish();
            this.bfModel.sendToAllPlayers(Type.BATTLE, "battle_finish", JSONUtils.parseFishishBattle(this.bfModel.players, 10000));
            this.quartzService.addJob(this.QUARTZ_NAME, QUARTZ_GROUP, e -> {
                this.bfModel.battleRestart();
                this.battleEnded = false;
            }, TimeType.MS, 10000L);
            if (this.bfModel.domModel != null) {
                this.bfModel.domModel.restartBattle();
            }
            this.battleEnded = true;
        }
    }

    private void calculatePrizes() {
        if (this.bfModel == null || this.bfModel.players == null || this.bfModel.players.size() <= 0) {
            return;
        }
        ArrayList<BattlefieldPlayerController> users = new ArrayList<BattlefieldPlayerController>(this.bfModel.players.values());
        if (!this.bfModel.battleInfo.team) {
            Collections.sort(users);
            BattlePrizeCalculate.calc(users, (int)this.battleFund);
        } else {
            ArrayList<BattlefieldPlayerController> redTeam = new ArrayList<BattlefieldPlayerController>();
            ArrayList<BattlefieldPlayerController> blueTeam = new ArrayList<BattlefieldPlayerController>();
            for (BattlefieldPlayerController player : users) {
                if (player.playerTeamType.equals("RED")) {
                    redTeam.add(player);
                    continue;
                }
                if (!player.playerTeamType.equals("BLUE")) continue;
                blueTeam.add(player);
            }
            BattlePrizeCalculate.calculateForTeam(redTeam, blueTeam, this.bfModel.battleInfo.scoreRed, this.bfModel.battleInfo.scoreBlue, 0.4, (int)this.battleFund);
        }
    }

    public void addFund(double value) {
        this.battleFund += value * 2.0;
        this.bfModel.sendToAllPlayers(Type.BATTLE, "change_fund", String.valueOf((int)this.battleFund));
        this.bfModel.bonusesSpawnService.updatedFund();
    }

    public double getBattleFund() {
        return this.battleFund;
    }

    public void setBattleFund(int value) {
        this.battleFund = value;
    }

    @Override
    public void destroy() {
        this.quartzService.deleteJob(this.QUARTZ_NAME, QUARTZ_GROUP);
    }
}

