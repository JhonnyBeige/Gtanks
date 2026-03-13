package gtanks.services;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.tanks.statistic.PlayerStatistic;
import gtanks.collections.FastHashMap;
import gtanks.commands.Type;
import gtanks.json.JSONUtils;
import gtanks.lobby.LobbyManager;
import gtanks.lobby.battles.BattleInfo;
import gtanks.lobby.chat.ChatModel;
import gtanks.services.LobbysServices;
import gtanks.services.annotations.ServicesInject;
import gtanks.system.quartz.QuartzService;
import gtanks.system.quartz.TimeType;
import gtanks.system.quartz.impl.QuartzServiceImpl;
import gtanks.users.User;
import gtanks.users.locations.UserLocation;
import gtanks.utils.StringUtils;
import java.util.ArrayList;
import java.util.List;

public class AutoEntryServices {
    private static final AutoEntryServices instance = new AutoEntryServices();
    private static final String QUARTZ_NAME = "AutoEntryServices GC";
    private static final String QUARTZ_GROUP = "runner";
    @ServicesInject(target=QuartzService.class)
    private final QuartzService quartzService = QuartzServiceImpl.inject();
    @ServicesInject(target=ChatModel.class)
    private final ChatModel chatLobby = ChatModel.getInstance();
    @ServicesInject(target=LobbysServices.class)
    private final LobbysServices lobbysServices = LobbysServices.getInstance();
    public FastHashMap<String, Data> playersForAutoEntry = new FastHashMap();

    private AutoEntryServices() {
        this.quartzService.addJobInterval(QUARTZ_NAME, QUARTZ_GROUP, e -> {
            long currentTime = System.currentTimeMillis();
            for (Data data : this.playersForAutoEntry.values()) {
                if (currentTime - data.createdTime < 120000L) continue;
                this.removePlayer(data.battle, data.userId, data.teamType, data.battle.battleInfo.team);
            }
        }, TimeType.SEC, 30L);
    }

    public void removePlayer(String userId) {
        this.playersForAutoEntry.remove(userId);
    }

    public boolean removePlayer(BattlefieldModel data, String userId, String teamType, boolean team) {
        if (this.playersForAutoEntry.get(userId) == null) {
            return false;
        }
        BattlefieldModel battle = data;
        this.lobbysServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.BATTLESELECT, "remove_player_from_battle", JSONUtils.parseRemovePlayerComand(userId, battle.battleInfo.battleId));
        if (!team) {
            --battle.battleInfo.countPeople;
            this.lobbysServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.BATTLESELECT, StringUtils.concatStrings("update_count_users_in_dm_battle", ";", battle.battleInfo.battleId, ";", String.valueOf(battle.battleInfo.countPeople)));
        } else {
            if (teamType.equals("RED")) {
                --battle.battleInfo.redPeople;
            } else {
                --battle.battleInfo.bluePeople;
            }
            this.lobbysServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.BATTLESELECT, "update_count_users_in_team_battle", JSONUtils.parseUpdateCoundPeoplesCommand(battle.battleInfo));
        }
        this.playersForAutoEntry.remove(userId);
        return true;
    }

    public void prepareToEnter(LobbyManager lobby) {
        Data data = this.playersForAutoEntry.get(lobby.getLocalUser().getNickname());
        if (data == null) {
            this.transmitToLobby(lobby);
            return;
        }
        BattlefieldModel bModel = data.battle;
        if (bModel == null) {
            this.transmitToLobby(lobby);
            return;
        }
        this.removePlayer(lobby.getLocalUser().getNickname());
        PlayerStatistic statistic = data.statistic;
        BattleInfo battleInfo = bModel.battleInfo;
        lobby.getLocalUser().setUserLocation(UserLocation.BATTLE);
        lobby.battle = new BattlefieldPlayerController(lobby, bModel, data.teamType);
        lobby.battle.statistic = statistic;
        lobby.disconnectListener.addListener(lobby.battle);
        lobby.send(Type.BATTLE, "init_battle_model", JSONUtils.parseBattleModelInfo(battleInfo, false));
        //lobby.send(Type.BATTLE, "init_battle_model", JSONUtils.parseBattleModelInfo(battleInfo, false, lobby.getLocalUser().getGarage()));
    }

    private void transmitToLobby(LobbyManager lobby) {
        lobby.send(Type.GARAGE, "init_garage_items", JSONUtils.parseGarageUser(lobby.getLocalUser()));
        lobby.send(Type.GARAGE, "init_market", JSONUtils.parseMarketItems(lobby.getLocalUser()));
        lobby.send(Type.LOBBY_CHAT, "init_chat");
        lobby.send(Type.LOBBY_CHAT, "init_messages", JSONUtils.parseChatLobbyMessages(this.chatLobby.getMessages()));
    }

    public boolean needEnterToBattle(User user) {
        return this.playersForAutoEntry.get(user.getNickname()) != null;
    }

    public void userExit(BattlefieldPlayerController player) {
        Data data = new Data(this, this);
        data.battle = player.battle;
        data.statistic = player.statistic;
        data.createdTime = System.currentTimeMillis();
        data.teamType = player.playerTeamType;
        data.userId = player.getUser().getNickname();
        this.playersForAutoEntry.put(player.getUser().getNickname(), data);
    }

    public List<Data> getPlayersByBattle(BattlefieldModel battle) {
        ArrayList<Data> players = new ArrayList<Data>();
        for (Data data : this.playersForAutoEntry.values()) {
            if (data.battle == null || data.battle != battle) continue;
            players.add(data);
        }
        return players;
    }

    public void battleRestarted(BattlefieldModel battle) {
        for (Data data : this.playersForAutoEntry.values()) {
            if (data.battle == null || data.battle != battle) continue;
            data.statistic.clear();
        }
    }

    public void battleDisposed(BattlefieldModel battle) {
        for (Data data : this.playersForAutoEntry.values()) {
            if (data.battle == null || data.battle != battle) continue;
            this.playersForAutoEntry.remove(data.userId);
        }
    }

    public static AutoEntryServices instance() {
        return instance;
    }

    public class Data {
        public BattlefieldModel battle;
        public PlayerStatistic statistic;
        public String teamType;
        public long createdTime;
        public String userId;

        public Data(AutoEntryServices this$0, AutoEntryServices this$02) {
        }
    }
}