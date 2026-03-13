/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles;

import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.BotDamageHandler;
import gtanks.battles.TankKillModel;
import gtanks.battles.anticheats.AnticheatModel;
import gtanks.battles.bonuses.Bonus;
import gtanks.battles.bonuses.BonusesSpawnService;
import gtanks.battles.bonuses.model.BonusTakeModel;
import gtanks.battles.chat.BattlefieldChatModel;
import gtanks.battles.ctf.CTFModel;
import gtanks.battles.ctf.flags.FlagState;
import gtanks.battles.dom.DominationModel;
import gtanks.battles.dom.DominationPoint;
import gtanks.battles.effects.model.EffectsVisualizationModel;
import gtanks.battles.managers.SpawnManager;
import gtanks.battles.maps.MapChecksumModel;
import gtanks.battles.mines.model.BattleMinesModel;
import gtanks.battles.spectator.SpectatorController;
import gtanks.battles.spectator.SpectatorModel;
import gtanks.battles.tanks.Tank;
import gtanks.battles.tanks.math.Vector3;
import gtanks.battles.tanks.statistic.PlayersStatisticModel;
import gtanks.battles.timer.bonuses.BonusesScheduler;
import gtanks.battles.timer.runtime.TankRespawnScheduler;
import gtanks.collections.FastHashMap;
import gtanks.commands.Type;
import gtanks.json.JSONUtils;
import gtanks.lobby.battles.BattleInfo;
import gtanks.lobby.battles.BattlesList;
import gtanks.logger.Logger;
import gtanks.services.AutoEntryServices;
import gtanks.services.TanksServices;
import gtanks.services.annotations.ServicesInject;
import gtanks.system.BattlesGC;
import gtanks.system.quartz.QuartzService;
import gtanks.system.quartz.TimeType;
import gtanks.system.quartz.impl.QuartzServiceImpl;
import gtanks.utils.ResourceUtils;
import gtanks.utils.StringUtils;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class BattlefieldModel {
    public static final String QUARTZ_GROUP = BattlefieldModel.class.getName();
    public final String QUARTZ_NAME;
    public final String QUARTZ_RESTART_NAME;
    @ServicesInject(target=QuartzService.class)
    private final QuartzService quartzService = QuartzServiceImpl.inject();
    @ServicesInject(target=AutoEntryServices.class)
    private final AutoEntryServices autoEntryServices = AutoEntryServices.instance();
    @ServicesInject(target=TanksServices.class)
    private TanksServices tanksServices = TanksServices.getInstance();
    public FastHashMap<String, BattlefieldPlayerController> players;
    public HashMap<String, Bonus> activeBonuses;
    public BattleInfo battleInfo;
    public int incration = 0;
    public boolean battleFinish = false;
    private long endBattleTime = 0L;
    private final JSONParser jsonParser = new JSONParser();
    public PlayersStatisticModel statistics;
    public TankKillModel tanksKillModel;
    public BotDamageHandler botkill;
    public CTFModel ctfModel;
    public DominationModel domModel;
    public BattlefieldChatModel chatModel;
    public BonusesSpawnService bonusesSpawnService;
    public SpectatorModel spectatorModel;
    public EffectsVisualizationModel effectsModel;
    public BonusTakeModel bonusTakeModel;
    public BattleMinesModel battleMinesModel;
    public MapChecksumModel mapChecksumModel;

    public BattlefieldModel(BattleInfo battleInfo) {
        this.battleInfo = battleInfo;
        this.statistics = new PlayersStatisticModel(this);
        this.tanksKillModel = new TankKillModel(this);
        this.botkill = new BotDamageHandler(this, 1000);
        this.chatModel = new BattlefieldChatModel(this);
        this.spectatorModel = new SpectatorModel(this);
        this.effectsModel = new EffectsVisualizationModel(this);
        this.bonusTakeModel = new BonusTakeModel(this);
        this.battleMinesModel = new BattleMinesModel(this);
        this.mapChecksumModel = new MapChecksumModel(this);
        this.QUARTZ_NAME = "TimeBattle " + this.hashCode() + " battle=" + battleInfo.battleId;
        this.QUARTZ_RESTART_NAME = "RestartBattle  battle=" + battleInfo.battleId;
        if (battleInfo.time > 0) {
            this.startTimeBattle();
        }
        if (battleInfo.battleType.equals("CTF")) {
            this.ctfModel = new CTFModel(this);
        }
        if (battleInfo.battleType.equals("DOM")) {
            this.domModel = new DominationModel(this);
        }
        this.players = new FastHashMap();
        this.activeBonuses = new HashMap();
        this.bonusesSpawnService = new BonusesSpawnService(this);
        new Thread(this.bonusesSpawnService).start();
        BattlesGC.addBattleForRemove(this);
    }

    private void startTimeBattle() {
        this.endBattleTime = System.currentTimeMillis() + (long)(this.battleInfo.time * 1000);
        this.quartzService.addJob(this.QUARTZ_NAME, QUARTZ_GROUP, e -> this.tanksKillModel.restartBattle(true), TimeType.SEC, this.battleInfo.time);
    }

    public void sendTableMessageToPlayers(String msg) {
        this.sendToAllPlayers(Type.BATTLE, "show_warning_table", msg);
    }

    public void battleRestart() {
        if (this.battleInfo.team) {
            this.sendToAllPlayers(Type.BATTLE, "change_team_scores", "RED", String.valueOf(this.battleInfo.scoreRed));
            this.sendToAllPlayers(Type.BATTLE, "change_team_scores", "BLUE", String.valueOf(this.battleInfo.scoreBlue));
        }
        this.battleFinish = false;
        for (BattlefieldPlayerController player : this.players.values()) {
            if (player == null) continue;
            player.statistic.clear();
            player.clearEffects();
            this.respawnPlayer(player, false);
        }
        long currentTimeMillis = System.currentTimeMillis();
        int prepareTimeLeft = (int)((currentTimeMillis + (long)(this.battleInfo.time * 1000) - currentTimeMillis) / 1000L);
        this.sendToAllPlayers(Type.BATTLE, "battle_restart", String.valueOf(prepareTimeLeft));
        this.autoEntryServices.battleRestarted(this);
        if (this.battleInfo.time > 0) {
            this.startTimeBattle();
        }
    }

    public void battleFinish() {
        if (this.players == null) {
            return;
        }
        this.battleFinish = true;
        if (this.activeBonuses != null) {
            this.activeBonuses.clear();
        }
        if (this.battleInfo.battleType.equals("CTF")) {
            if (this.ctfModel.getBlueFlag().state == FlagState.TAKEN_BY && this.ctfModel.getBlueFlag().owner != null) {
                this.ctfModel.getBlueFlag().owner.flag = null;
                this.ctfModel.getBlueFlag().owner = null;
            }
            if (this.ctfModel.getRedFlag().state == FlagState.TAKEN_BY && this.ctfModel.getRedFlag().owner != null) {
                this.ctfModel.getRedFlag().owner.flag = null;
                this.ctfModel.getRedFlag().owner = null;
            }
        }
        this.bonusesSpawnService.battleFinished();
        this.tanksKillModel.setBattleFund(0);
        this.battleInfo.scoreBlue = 0;
        this.battleInfo.scoreRed = 0;
        for (BattlefieldPlayerController player : this.players.values()) {
            if (player == null) continue;
            TankRespawnScheduler.cancelRespawn(player);
        }
        this.autoEntryServices.battleRestarted(this);
    }

    public int getTimeLeft() {
        return (int)((this.endBattleTime - System.currentTimeMillis()) / 1000L);
    }

    public void fire(BattlefieldPlayerController tank, String json) {
        this.sendToAllPlayers(tank, Type.BATTLE, "fire", tank.tank.id, json);
    }

    public void quickFire(BattlefieldPlayerController tank, String json) {
        this.sendToAllPlayers(tank, Type.BATTLE, "shaft_quick_shot", tank.tank.id, json);
    }

    public void startFire(BattlefieldPlayerController tank) {
        this.sendToAllPlayers(tank, Type.BATTLE, "start_fire", tank.tank.id);
    }

    public void stopFire(BattlefieldPlayerController tank) {
        this.sendToAllPlayers(tank, Type.BATTLE, "stop_fire", tank.tank.id);
    }

    public synchronized void onTakeBonus(BattlefieldPlayerController controller, String json) {
        try {
            JSONObject obj = (JSONObject)this.jsonParser.parse(json);
            JSONObject posObj = (JSONObject)obj.get("real_tank_position");
            String bonusId = (String)obj.get("bonus_id");
            Vector3 realPosTank = new Vector3((float)((Double)posObj.get("x")).doubleValue(), (float)((Double)posObj.get("y")).doubleValue(), (float)((Double)posObj.get("z")).doubleValue());
            Bonus bonus = this.activeBonuses.get(bonusId);
            if (bonus == null) {
                return;
            }
            if (this.bonusTakeModel.onTakeBonus(bonus, controller)) {
                this.sendToAllPlayers(Type.BATTLE, "take_bonus_by", bonusId);
                this.activeBonuses.remove(bonusId);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void spawnBonus(Bonus bonus, int inc, int disappearingTime) {
        if (bonus.position.x == 0.0f && bonus.position.y == 0.0f && bonus.position.z == 0.0f) {
            return;
        }
        String id = StringUtils.concatStrings(bonus.type.toString(), "_", String.valueOf(inc));
        this.activeBonuses.put(id, bonus);
        BonusesScheduler.runRemoveTask(this, id, disappearingTime);
        this.sendToAllPlayers(Type.BATTLE, "spawn_bonus", JSONUtils.parseBonusInfo(bonus, inc, disappearingTime));
    }

    public void respawnPlayer(BattlefieldPlayerController controller, boolean kill) {
        if (this.battleFinish) {
            return;
        }
        controller.send(Type.BATTLE, "local_user_killed");
        this.battleMinesModel.playerDied(controller);
        if (kill) {
            controller.clearEffects();
            this.sendToAllPlayers(Type.BATTLE, "kill_tank", controller.tank.id, "suicide");
            controller.statistic.addDeaths();
            controller.getUser().setDeaths(controller.getUser().getDeaths() + 1);
            this.tanksServices.updateRatingData(controller.parentLobby);
            this.statistics.changeStatistic(controller);
            if (this.ctfModel != null && controller.flag != null) {
                this.ctfModel.dropFlag(controller, controller.tank.position);
            }
        }
        controller.tank.state = "suicide";
        TankRespawnScheduler.startRespawn(controller, false);
    }

    public void moveTank(BattlefieldPlayerController controller) {
        String json = JSONUtils.parseMoveCommand(controller);
        this.sendToAllPlayers(controller, Type.BATTLE, "move", json);
    }

    public void spawnPlayer(BattlefieldPlayerController controller) {
        if (this.battleFinish) {
            return;
        }
        TankRespawnScheduler.startRespawn(controller, true);
    }

    public void setupTank(BattlefieldPlayerController controller) {
        controller.tank.id = controller.parentLobby.getLocalUser().getNickname();
    }

    public void addPlayer(BattlefieldPlayerController controller) {
        this.setupTank(controller);
        this.players.put(controller.tank.id, controller);
        ++this.incration;
        BattlesGC.cancelRemoving(this);
    }

    public void removeUser(BattlefieldPlayerController controller, boolean cache) {
        controller.clearEffects();
        this.battleMinesModel.playerDied(controller);
        this.players.remove(controller.parentLobby.getLocalUser().getNickname(), controller);
        if (!cache) {
            if (!this.battleInfo.team) {
                --BattlesList.getBattleInfoById((String)this.battleInfo.battleId).countPeople;
            } else if (controller.playerTeamType.equals("RED")) {
                --BattlesList.getBattleInfoById((String)this.battleInfo.battleId).redPeople;
            } else {
                --BattlesList.getBattleInfoById((String)this.battleInfo.battleId).bluePeople;
            }
        }
        if (this.ctfModel != null && controller.flag != null) {
            this.ctfModel.dropFlag(controller, controller.tank.position);
        }
        if (this.domModel != null) {
            String pointId = null;
            for (DominationPoint point : this.domModel.getPoints()) {
                if (!point.getUserIds().contains(controller.getUser().getNickname())) continue;
                pointId = point.getId();
                break;
            }
            if (pointId != null) {
                this.domModel.tankLeaveCapturingPoint(controller, pointId);
            }
        }
        this.sendToAllPlayers(Type.BATTLE, "remove_user", controller.tank.id);
        if (this.players.size() == 0) {
            BattlesGC.addBattleForRemove(this);
        }
    }

    public void initLocalTank(BattlefieldPlayerController controller) {
        if (controller.userInited) {
            Logger.debug(controller.parentLobby.getLocalUser().getNickname() + " ANTICHEAT: user already exists in battle");
            return;
        }
        controller.userInited = true;
        Vector3 pos = SpawnManager.getSpawnState(this.battleInfo.map, controller.playerTeamType);
        if (this.battleInfo.battleType.equals("CTF")) {
            controller.send(Type.BATTLE, "init_ctf_model", JSONUtils.parseCTFModelData(this));
        }
        if (this.battleInfo.battleType.equals("DOM")) {
            this.domModel.sendInitData(controller);
        }
        controller.send(Type.BATTLE, "init_gui_model", JSONUtils.parseBattleData(this));
        controller.inventory.init();
        this.battleMinesModel.initModel(controller);
        this.battleMinesModel.sendMines(controller);
        this.initWeaponsLightning(controller);
        this.sendAllTanks(controller);
        this.sendToAllPlayers(Type.BATTLE, "init_tank", JSONUtils.parseTankData(this, controller, controller.parentLobby.getLocalUser().getGarage(), pos, true, this.incration, controller.tank.id, controller.parentLobby.getLocalUser().getNickname(), controller.parentLobby.getLocalUser().getRang()));
        this.statistics.changeStatistic(controller);
        this.effectsModel.sendInitData(controller);
        this.spawnPlayer(controller);
    }

    public void sendUserLogMessage(String idUser, String message) {
        this.sendToAllPlayers(Type.BATTLE, "user_log", idUser, message);
    }

    public void initWeaponsLightning(BattlefieldPlayerController controller) {
        try {
            String jsonConfig = new String(Files.readAllBytes(Paths.get(ResourceUtils.data("json/turretsLightingData.json"), new String[0])));
            controller.send(Type.BATTLE, "init_turret_sfx_lighting", jsonConfig);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendAllTanks(BattlefieldPlayerController controller) {
        for (BattlefieldPlayerController player : this.players.values()) {
            if (player == controller || !player.userInited) continue;
            controller.send(Type.BATTLE, "init_tank", JSONUtils.parseTankData(this, player, player.parentLobby.getLocalUser().getGarage(), player.tank.position, false, this.incration, player.tank.id, player.parentLobby.getLocalUser().getNickname(), player.parentLobby.getLocalUser().getRang()));
            this.statistics.changeStatistic(player);
        }
    }

    public void sendAllTanks(SpectatorController controller) {
        for (BattlefieldPlayerController player : this.players.values()) {
            if (!player.userInited) continue;
            controller.sendCommand(Type.BATTLE, "init_tank", JSONUtils.parseTankData(this, player, player.parentLobby.getLocalUser().getGarage(), player.tank.position, false, this.incration, player.tank.id, player.parentLobby.getLocalUser().getNickname(), player.parentLobby.getLocalUser().getRang()));
            this.statistics.changeStatistic(player);
        }
    }

    public void activateTank(BattlefieldPlayerController player) {
        player.tank.state = "active";
        this.sendToAllPlayers(Type.BATTLE, "activate_tank", player.tank.id);
    }

    public BattlefieldPlayerController getPlayer(String id) {
        return this.players.get(id);
    }

    public void sendToAllPlayers(Type type, String ... args) {
        if (this.players == null) {
            return;
        }
        if (this.players.size() != 0) {
            for (BattlefieldPlayerController player : this.players.values()) {
                if (!player.userInited) continue;
                player.send(type, args);
            }
        }
        this.spectatorModel.sendCommandToSpectators(type, args);
    }

    public void sendToRedTeamPlayers(Type type, String ... args) {
        if (this.players == null) {
            return;
        }
        if (this.players.size() != 0) {
            for (BattlefieldPlayerController player : this.players.values()) {
                if (!player.playerTeamType.equals("RED") || !player.userInited) continue;
                player.send(type, args);
            }
        }
        this.spectatorModel.sendCommandToSpectators(type, args);
    }

    public void sendToBlueTeamPlayers(Type type, String ... args) {
        if (this.players == null) {
            return;
        }
        if (this.players.size() != 0) {
            for (BattlefieldPlayerController player : this.players.values()) {
                if (!player.playerTeamType.equals("BLUE") || !player.userInited) continue;
                player.send(type, args);
            }
        }
        this.spectatorModel.sendCommandToSpectators(type, args);
    }

    public void sendToAllPlayers(BattlefieldPlayerController other, Type type, String ... args) {
        if (this.players.size() != 0) {
            for (BattlefieldPlayerController player : this.players.values()) {
                if (!player.userInited || player == other) continue;
                player.send(type, args);
            }
        }
        this.spectatorModel.sendCommandToSpectators(type, args);
    }

    public void cheatDetected(BattlefieldPlayerController player, Class<?> anticheat) {
        AnticheatModel[] model = (AnticheatModel[])anticheat.getAnnotationsByType(AnticheatModel.class);
        if (model != null && model.length >= 1) {
            Logger.log("Detected cheater![" + model[0].name() + "] " + player.getUser().getNickname() + " " + player.parentLobby.networker.getIP());
        }
        this.kickPlayer(player);
    }

    public void kickPlayer(BattlefieldPlayerController player) {
        player.send(Type.BATTLE, "kick_for_cheats");
        player.parentLobby.networker.closeConnection();
    }

    public void setTank(BattlefieldPlayerController player, Tank newTank) {
        this.players.get((Object)player.parentLobby.getLocalUser().getNickname()).tank = newTank;
    }

    public void destroy() {
        this.players.clear();
        this.activeBonuses.clear();
        this.quartzService.deleteJob(this.QUARTZ_NAME, QUARTZ_GROUP);
        this.tanksKillModel.destroy();
        this.tanksKillModel = null;
        this.players = null;
        this.activeBonuses = null;
        this.battleInfo = null;
        this.spectatorModel = null;
        if (this.domModel != null) {
            this.domModel.destroy();
        }
    }
}

