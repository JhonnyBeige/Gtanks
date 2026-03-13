/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.dom;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.maps.parser.map.keypoints.DOMKeypoint;
import gtanks.battles.spectator.SpectatorController;
import gtanks.battles.tanks.math.Vector3;
import gtanks.collections.FastHashMap;
import gtanks.commands.Type;
import gtanks.services.TanksServices;
import gtanks.services.annotations.ServicesInject;
import gtanks.system.destroy.Destroyable;
import gtanks.system.quartz.QuartzService;
import gtanks.system.quartz.TimeType;
import gtanks.system.quartz.impl.QuartzServiceImpl;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DominationModel
        implements Destroyable {
    private static final int POINT_RADIUS = 1000;
    public static final String QUARTZ_GROUP = DominationModel.class.getName();
    public final String QUARTZ_NAME;
    private FastHashMap<String, DominationPoint> points;
    private List<DominationPointHandler> pointsHandlers;
    private final BattlefieldModel bfModel;
    @ServicesInject(target = TanksServices.class)
    private final TanksServices tanksServices = TanksServices.getInstance();
    @ServicesInject(target = QuartzService.class)
    private final QuartzService quartzService = QuartzServiceImpl.inject();
    private float scoreRed = 0.0f;
    private float scoreBlue = 0.0f;

    public DominationModel(BattlefieldModel bfModel) {
        this.bfModel = bfModel;
        this.QUARTZ_NAME = "DominationModel " + this.hashCode() + " battle=" + bfModel.battleInfo.battleId;
        this.points = new FastHashMap();
        this.pointsHandlers = new ArrayList<DominationPointHandler>();
        for (DOMKeypoint keypoint : bfModel.battleInfo.map.domKeypoints) {
            DominationPoint point = new DominationPoint(keypoint.getPointId(), keypoint.getPosition().toVector3(), 1000.0);
            this.points.put(keypoint.getPointId(), point);
            this.pointsHandlers.add(new DominationPointHandler(point));
        }
        this.quartzService.addJobInterval(this.QUARTZ_NAME, QUARTZ_GROUP, e -> this.pointsHandlers.forEach(point -> point.update()), TimeType.MS, 100L);
    }

    public void sendInitData(BattlefieldPlayerController player) {
        JSONObject data = new JSONObject();
        JSONArray pointsData = new JSONArray();
        for (DominationPoint point : this.points.values()) {
            JSONObject obj = new JSONObject();
            obj.put("id", point.getId());
            obj.put("radius", point.getRadius());
            obj.put("x", Float.valueOf(point.getPos().x));
            obj.put("y", Float.valueOf(point.getPos().y));
            obj.put("z", Float.valueOf(point.getPos().z));
            obj.put("score", point.getScore());
            JSONArray users = new JSONArray();
            for (String userId : point.getUserIds()) {
                users.add(userId);
            }
            obj.put("occupated_users", users);
            pointsData.add(obj);
        }
        data.put("points", pointsData);
        player.send(Type.BATTLE, "init_dom_model", data.toJSONString());
    }

    public void sendInitData(SpectatorController player) {
        JSONObject data = new JSONObject();
        JSONArray pointsData = new JSONArray();
        for (DominationPoint point : this.points.values()) {
            JSONObject obj = new JSONObject();
            obj.put("id", point.getId());
            obj.put("radius", point.getRadius());
            obj.put("x", Float.valueOf(point.getPos().x));
            obj.put("y", Float.valueOf(point.getPos().y));
            obj.put("z", Float.valueOf(point.getPos().z));
            obj.put("score", point.getScore());
            JSONArray users = new JSONArray();
            for (String userId : point.getUserIds()) {
                users.add(userId);
            }
            obj.put("occupated_users", users);
            pointsData.add(obj);
        }
        data.put("points", pointsData);
        player.sendCommand(Type.BATTLE, "init_dom_model", data.toJSONString());
    }

    public synchronized void tankCapturingPoint(BattlefieldPlayerController player, String pointId, Vector3 tankPos) {
        double dot;
        DominationPoint point = this.points.get(pointId);
        if (point != null && !((dot = point.getPos().distanceTo(tankPos)) > point.getRadius())) {
            point.addPlayer(player.playerTeamType, player);
            this.bfModel.sendToAllPlayers(player, Type.BATTLE, "tank_capturing_point", String.valueOf(pointId), player.getUser().getNickname());
        }
    }

    public synchronized void tankLeaveCapturingPoint(BattlefieldPlayerController player, String pointId) {
        DominationPoint point = this.points.get(pointId);
        if (point != null && point.getUserIds().contains(player.getUser().getNickname())) {
            this.bfModel.sendToAllPlayers(player, Type.BATTLE, "tank_leave_capturing_point", player.getUser().getNickname(), pointId);
            point.removePlayer(player.playerTeamType, player);
        }
    }

    private void pointCapturedBy(DominationPoint point, String teamType) {
        this.bfModel.sendToAllPlayers(Type.BATTLE, "point_captured_by", teamType, point.getId());
    }

    private void pointLostBy(DominationPoint point, String ownerTeamType) {
        this.bfModel.sendToAllPlayers(Type.BATTLE, "point_lost_by", ownerTeamType, point.getId());
    }

    public Collection<DominationPoint> getPoints() {
        return this.points.values();
    }

    public void restartBattle() {
        for (DominationPoint point : this.points.values()) {
            point.setScore(0.0);
            this.bfModel.sendToAllPlayers(Type.BATTLE, "set_point_score", String.valueOf(point.getId()), String.valueOf((int) point.getScore()));
            point.setCountBlue(0);
            point.setCountRed(0);
            point.setPointCapturedByBlue(false);
            point.setPointCapturedByRed(false);
            point.getBlues().clear();
            point.getReds().clear();
            point.getUserIds().clear();
            this.scoreBlue = 0.0f;
            this.scoreRed = 0.0f;
        }
    }

    private void addPlayerScore(BattlefieldPlayerController player, int score) {
        this.tanksServices.addScore(player.parentLobby, score);
        player.statistic.addScore(score);
        this.bfModel.statistics.changeStatistic(player);
    }

    @Override
    public void destroy() {
        this.quartzService.deleteJob(this.QUARTZ_NAME, QUARTZ_GROUP);
        this.pointsHandlers.clear();
        this.pointsHandlers = null;
        this.points.clear();
        this.points = null;
    }

    class DominationPointHandler {
        private final DominationPoint point;
        public boolean alive = true;
        private boolean sendedZeroSpeedScore = false;

        public DominationPointHandler(DominationPoint point) {
            this.point = point;
            this.point.setTickableHandler(this);
        }

        public void update() {
            // Check if battle information is available and the battle is not finished
            if (DominationModel.this.bfModel.battleInfo != null && !DominationModel.this.bfModel.battleFinish) {

                // Check if any team has captured all flags
                if (DominationModel.this.bfModel.battleInfo.numFlags != 0 &&
                        (DominationModel.this.scoreBlue >= (float) DominationModel.this.bfModel.battleInfo.numFlags ||
                                DominationModel.this.scoreRed >= (float) DominationModel.this.bfModel.battleInfo.numFlags)) {
                    DominationModel.this.bfModel.tanksKillModel.restartBattle(false);
                }

                // Adjust point score boundaries
                if (this.point.getScore() >= 100.0 || this.point.getScore() <= -100.0) {
                    this.point.setScore(this.point.getScore() >= 100.0 ? 100.0 : -100.0);
                }

                // Handle point capture by the blue team
                if (this.point.getScore() == 100.0) {
                    if (!this.point.isPointCapturedByBlue()) {
                        // Calculate score for blue team based on captured point and other team's ranks
                        float score = 0.2f * (float) DominationModel.this.bfModel.battleInfo.redPeople * 10.0f /
                                (float) this.point.getCountBlue();
                        for (BattlefieldPlayerController player : this.point.getBlues()) {
                            DominationModel.this.addPlayerScore(player, Math.round(score));
                        }

                        // Calculate fund based on other team's ranks
                        double fund = 0.0;
                        ArrayList<BattlefieldPlayerController> otherTeam = new ArrayList<BattlefieldPlayerController>();
                        for (BattlefieldPlayerController otherPlayer : DominationModel.this.bfModel.players) {
                            if (otherPlayer.playerTeamType.equals("BLUE") || otherPlayer.playerTeamType.equals("NONE"))
                                continue;
                            otherTeam.add(otherPlayer);
                        }
                        for (BattlefieldPlayerController otherPlayer : otherTeam) {
                            fund += Math.sqrt((double) otherPlayer.getUser().getRang() * 0.25);
                        }

                        // Add fund and mark point as captured by blue
                        DominationModel.this.bfModel.tanksKillModel.addFund(fund);
                        DominationModel.this.pointCapturedBy(this.point, "blue");
                    }
                    this.point.setPointCapturedByBlue(true);
                    this.point.setPointCapturedByRed(false);
                    DominationModel.this.scoreBlue += 0.02f;
                }

                // Handle point capture by the red team
                else if (this.point.getScore() == -100.0) {
                    if (!this.point.isPointCapturedByRed()) {
                        // Calculate score for red team based on captured point and other team's ranks
                        float score = 0.2f * (float) DominationModel.this.bfModel.battleInfo.bluePeople * 10.0f /
                                (float) this.point.getCountRed();
                        for (BattlefieldPlayerController player : this.point.getReds()) {
                            DominationModel.this.addPlayerScore(player, Math.round(score));
                        }

                        // Calculate fund based on other team's ranks
                        double fund = 0.0;
                        ArrayList<BattlefieldPlayerController> otherTeam = new ArrayList<BattlefieldPlayerController>();
                        for (BattlefieldPlayerController otherPlayer : DominationModel.this.bfModel.players) {
                            if (otherPlayer.playerTeamType.equals("RED") || otherPlayer.playerTeamType.equals("NONE"))
                                continue;
                            otherTeam.add(otherPlayer);
                        }
                        for (BattlefieldPlayerController otherPlayer : otherTeam) {
                            fund += Math.sqrt((double) otherPlayer.getUser().getRang() * 0.25);
                        }

                        // Add fund and mark point as captured by red
                        DominationModel.this.bfModel.tanksKillModel.addFund(fund);
                        DominationModel.this.pointCapturedBy(this.point, "red");
                    }
                    this.point.setPointCapturedByRed(true);
                    this.point.setPointCapturedByBlue(false);
                    DominationModel.this.scoreRed += 0.02f;
                }

                // Handle neutral point
                else if (this.point.getScore() == 0.0) {
                    float score;
                    double fund;
                    ArrayList<BattlefieldPlayerController> otherTeam;

                    // Handle blue team losing the point
                    if (this.point.isPointCapturedByBlue()) {
                        score = 0.2f * (float) DominationModel.this.bfModel.battleInfo.bluePeople * 10.0f /
                                (float) this.point.getCountRed();
                        for (BattlefieldPlayerController player : this.point.getReds()) {
                            DominationModel.this.addPlayerScore(player, Math.round(score));
                        }

                        // Calculate fund based on other team's ranks
                        fund = 0.0;
                        otherTeam = new ArrayList<BattlefieldPlayerController>();
                        for (BattlefieldPlayerController otherPlayer : DominationModel.this.bfModel.players) {
                            if (otherPlayer.playerTeamType.equals("RED") || otherPlayer.playerTeamType.equals("NONE"))
                                continue;
                            otherTeam.add(otherPlayer);
                        }
                        for (BattlefieldPlayerController otherPlayer : otherTeam) {
                            fund += Math.sqrt((double) otherPlayer.getUser().getRang() * 0.25);
                        }

                        // Add fund, decrease red score, and mark point as lost by blue
                        DominationModel.this.bfModel.tanksKillModel.addFund(fund);
                        DominationModel.this.scoreRed += 0.02f;
                        DominationModel.this.pointLostBy(this.point, "blue");
                    }

                    // Handle red team losing the point
                    if (this.point.isPointCapturedByRed()) {
                        score = 0.2f * (float) DominationModel.this.bfModel.battleInfo.redPeople * 10.0f /
                                (float) this.point.getCountBlue();
                        for (BattlefieldPlayerController player : this.point.getBlues()) {
                            DominationModel.this.addPlayerScore(player, Math.round(score));
                        }

                        // Calculate fund based on other team's ranks
                        fund = 0.0;
                        otherTeam = new ArrayList<BattlefieldPlayerController>();
                        for (BattlefieldPlayerController otherPlayer : DominationModel.this.bfModel.players) {
                            if (otherPlayer.playerTeamType.equals("BLUE") || otherPlayer.playerTeamType.equals("NONE"))
                                continue;
                            otherTeam.add(otherPlayer);
                        }
                        for (BattlefieldPlayerController otherPlayer : otherTeam) {
                            fund += Math.sqrt((double) otherPlayer.getUser().getRang() * 0.25);
                        }

                        // Add fund, decrease blue score, and mark point as lost by red
                        DominationModel.this.bfModel.tanksKillModel.addFund(fund);
                        DominationModel.this.scoreBlue += 0.02f;
                        DominationModel.this.pointLostBy(this.point, "red");
                    }

                    // Reset point ownership
                    this.point.setPointCapturedByRed(false);
                    this.point.setPointCapturedByBlue(false);
                }

                // Calculate added score based on the difference in team counts
                double addedScore = 0.0;
                if (this.point.getCountBlue() > this.point.getCountRed()) {
                    int countPeople = this.point.getCountBlue() - this.point.getCountRed();
                    addedScore = countPeople;
                } else if (this.point.getCountRed() > this.point.getCountBlue()) {
                    int countPeople = this.point.getCountRed() - this.point.getCountBlue();
                    addedScore = -countPeople;
                } else if (this.point.getScore() > 0.0 || this.point.getScore() < 0.0) {
                    if (this.point.isPointCapturedByBlue()) {
                        if (this.point.getScore() > 0.0) {
                            if (this.point.getCountRed() == 0) {
                                addedScore = 1.0;
                            }
                            if (this.point.getScore() >= 100.0) {
                                addedScore = 0.0;
                            }
                        }
                    } else if (this.point.isPointCapturedByRed()) {
                        if (this.point.getScore() < 0.0) {
                            if (this.point.getCountBlue() == 0) {
                                addedScore = -1.0;
                            }
                            if (this.point.getScore() <= -100.0) {
                                addedScore = 0.0;
                            }
                        }
                    } else {
                        // Handle neutral point with no ownership
                        addedScore = this.point.getCountBlue() == 0 ? (this.point.getScore() > 0.0 ? -1.0 : 1.0) : 0.0;
                    }
                }

                // Update team scores and inform players
                if (DominationModel.this.scoreBlue > 0.0f) {
                    DominationModel.this.bfModel.battleInfo.scoreBlue = (int) DominationModel.this.scoreBlue;
                    DominationModel.this.bfModel.sendToAllPlayers(Type.BATTLE, "change_team_scores", "BLUE",
                            String.valueOf(DominationModel.this.scoreBlue));
                }
                if (DominationModel.this.scoreRed > 0.0f) {
                    DominationModel.this.bfModel.battleInfo.scoreRed = (int) DominationModel.this.scoreRed;
                    DominationModel.this.bfModel.sendToAllPlayers(Type.BATTLE, "change_team_scores", "RED",
                            String.valueOf(DominationModel.this.scoreRed));
                }

                // Reset score if point score exceeds boundaries
                if (this.point.getScore() > 100.0 || this.point.getScore() < -100.0) {
                    addedScore = 0.0;
                }

                // Handle zero speed score
                if (addedScore == 0.0) {
                    if (this.sendedZeroSpeedScore) {
                        return;
                    }
                    this.sendedZeroSpeedScore = true;
                } else {
                    this.sendedZeroSpeedScore = false;
                }

                // Update point score and inform players
                this.point.setScore(this.point.getScore() + addedScore);
                DominationModel.this.bfModel.sendToAllPlayers(Type.BATTLE, "set_point_score",
                        String.valueOf(this.point.getId()), String.valueOf((int) this.point.getScore()),
                        String.valueOf(addedScore));
            }
        }
    }
}