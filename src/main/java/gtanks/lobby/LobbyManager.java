package gtanks.lobby;

import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.maps.Map;
import gtanks.battles.maps.MapsLoader;
import gtanks.battles.spectator.SpectatorController;
import gtanks.commands.Command;
import gtanks.commands.Type;
import gtanks.json.JSONUtils;
import gtanks.lobby.battles.BattleInfo;
import gtanks.lobby.battles.BattlesList;
import gtanks.lobby.chat.ChatMessage;
import gtanks.lobby.chat.ChatModel;
import gtanks.lobby.chat.flood.AntiFloodHandler;
import gtanks.logger.Logger;
import gtanks.main.database.DatabaseManager;
import gtanks.main.database.impl.DatabaseManagerImpl;
import gtanks.main.netty.ProtocolTransfer;
import gtanks.main.params.OnlineStats;
import gtanks.network.listeners.DisconnectListener;
import gtanks.services.*;
import gtanks.services.annotations.ServicesInject;
import gtanks.services.email.EmailHandler;
import gtanks.system.dailybonus.DailyBonusService;
import gtanks.services.NewsService;
import gtanks.users.TypeUser;
import gtanks.users.User;
import gtanks.users.garage.GarageItemsLoader;
import gtanks.users.garage.enums.ItemType;
import gtanks.users.garage.items.Item;
import gtanks.users.karma.Karma;
import gtanks.users.locations.UserLocation;
import gtanks.utils.StringUtils;
import java.io.IOException;
import java.util.Date;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class LobbyManager {
    private final User localUser;
    public ProtocolTransfer networker;
    public BattlefieldPlayerController battle;
    public SpectatorController spectatorController;
    @ServicesInject(target=DatabaseManagerImpl.class)
    private static final DatabaseManager database = DatabaseManagerImpl.instance();
    @ServicesInject(target=LobbysServices.class)
    private static final LobbysServices lobbysServices = LobbysServices.getInstance();
    @ServicesInject(target=ChatModel.class)
    private static final ChatModel chatLobby = ChatModel.getInstance();
    @ServicesInject(target=DailyBonusService.class)
    private static final DailyBonusService dailyBonusService = DailyBonusService.instance();
    @ServicesInject(target=NewsService.class)
    private static final NewsService newsService = NewsService.instance();
    @ServicesInject(target=AutoEntryServices.class)
    private final AutoEntryServices autoEntryServices = AutoEntryServices.instance();
    @ServicesInject(target=GiftsServices.class)
    private static final GiftsServices giftsService = GiftsServices.instance();
    @ServicesInject(target=EmailHandler.class)
    private static final EmailHandler emailHandler = EmailHandler.getInstance();
    private AntiFloodHandler chatFloodController;
    public DisconnectListener disconnectListener;
    public long timer;
    PacketLimiterService packetLimitService = new PacketLimiterService();
    private final PacketDelayService packetDelayService = new PacketDelayService();

    public LobbyManager(ProtocolTransfer networker, User localUser) {
        this.networker = networker;
        this.localUser = localUser;
        this.disconnectListener = new DisconnectListener();
        this.setChatFloodController(new AntiFloodHandler());
        this.timer = System.currentTimeMillis();
        this.localUser.setUserLocation(UserLocation.BATTLESELECT);
        lobbysServices.addLobby(this);
        OnlineStats.addOnline();
    }

    public void send(Type type, String ... args) {
        try {
            this.packetDelayService.send(this.networker, type, args);
        } catch (IOException iOException) {
            // empty catch block
        }
    }

    public void executeCommand(Command cmd) {
        try {
            if (!this.packetLimitService.allowPacket()) {
                // empty if block
            }
            switch (cmd.type) {
                case LOBBY_CHAT: {
                    chatLobby.addMessage(new ChatMessage(this.localUser, cmd.args[0], this.stringToBoolean(cmd.args[1]), cmd.args[2].equals("NULL") ? null : database.getUserById(cmd.args[2]), this));
                    break;
                }
                case AUTH: {
                    break;
                }
                case REGISTRATON: {
                    break;
                }
                case GARAGE: {
                    if (cmd.args[0].equals("try_mount_item")) {
                        if (this.localUser.getGarage().mountItem(cmd.args[1])) {
                            this.send(Type.GARAGE, "mount_item", cmd.args[1]);
                            this.localUser.getGarage().parseJSONData();
                            database.update(this.localUser.getGarage());
                        } else {
                            this.send(Type.GARAGE, "try_mount_item_NO");
                        }
                    }
                    if (cmd.args[0].equals("try_update_item")) {
                        this.onTryUpdateItem(cmd.args[1]);
                    }
                    if (cmd.args[0].equals("get_garage_data") && this.localUser.getGarage().mountHull != null && this.localUser.getGarage().mountTurret != null && this.localUser.getGarage().mountColormap != null) {
                        this.send(Type.GARAGE, "init_mounted_item", StringUtils.concatStrings(this.localUser.getGarage().mountHull.id, "_m", String.valueOf(this.localUser.getGarage().mountHull.modificationIndex)));
                        this.send(Type.GARAGE, "init_mounted_item", StringUtils.concatStrings(this.localUser.getGarage().mountTurret.id, "_m", String.valueOf(this.localUser.getGarage().mountTurret.modificationIndex)));
                        this.send(Type.GARAGE, "init_mounted_item", StringUtils.concatStrings(this.localUser.getGarage().mountColormap.id, "_m", String.valueOf(this.localUser.getGarage().mountColormap.modificationIndex)));
                    }
                    if (!cmd.args[0].equals("try_buy_item")) break;
                    this.onTryBuyItem(cmd.args[1], Integer.parseInt(cmd.args[2]));
                    break;
                }
                case CHAT: {
                    break;
                }
                case LOBBY: {
                    String verificationKey;
                    String userEmail;
                    if (cmd.args[0].equals("get_hall_of_fame_data")) {
                        this.localUser.setUserLocation(UserLocation.HALL_OF_FAME);
                        this.initHallOfFameData();
                    }
                    if (cmd.args[0].equals("show_profile")) {
                        this.send(Type.LOBBY, "show_profile", JSONUtils.parseUserEmailToJSON(this.localUser));
                    }
                    if (cmd.args[0].equals("get_garage_data")) {
                        this.sendGarage();
                    }
                    if (cmd.args[0].equals("get_data_init_battle_select")) {
                        this.sendMapsInit();
                    }
                    if (cmd.args[0].equals("check_battleName_for_forbidden_words") && cmd.args.length > 1) {
                        String _name = cmd.args.length > 0 ? cmd.args[1] : "";
                        this.checkBattleName(_name);
                    }
                    if (cmd.args[0].equals("try_create_battle_dm")) {
                        this.tryCreateBattleDM(cmd.args[1], cmd.args[2], Integer.parseInt(cmd.args[3]), Integer.parseInt(cmd.args[4]), Integer.parseInt(cmd.args[5]), Integer.parseInt(cmd.args[6]), Integer.parseInt(cmd.args[7]), this.stringToBoolean(cmd.args[8]), this.stringToBoolean(cmd.args[9]), this.stringToBoolean(cmd.args[10]));
                    }
                    if (cmd.args[0].equals("try_create_battle_tdm")) {
                        this.tryCreateTDMBattle(cmd.args[1]);
                    }
                    if (cmd.args[0].equals("try_create_battle_ctf")) {
                        this.tryCreateCTFBattle(cmd.args[1]);
                    }
                    if (cmd.args[0].equals("try_create_battle_dom")) {
                        this.tryCreateDOMBattle(cmd.args[1]);
                    }
                    if (cmd.args[0].equals("get_show_battle_info")) {
                        this.sendBattleInfo(cmd.args[1]);
                    }
                    if (cmd.args[0].equals("enter_battle")) {
                        this.onEnterInBattle(cmd.args[1]);
                    }
                    if (cmd.args[0].equals("enter_battle_team")) {
                        this.onEnterInTeamBattle(cmd.args[1], Boolean.parseBoolean(cmd.args[2]));
                    }
                    if (cmd.args[0].equals("enter_battle_spectator")) {
                        if (this.getLocalUser().getType() == TypeUser.DEFAULT || this.getLocalUser().getType() == TypeUser.TESTER) {
                            return;
                        }
                        this.enterInBattleBySpectator(cmd.args[1]);
                    }
                    if (cmd.args[0].equals("show_quests")) {
                        // empty if block
                    }
                    if (cmd.args[0].equals("user_inited")) {
                        dailyBonusService.userInited(this);
                        dailyBonusService.userLoaded(this);
                        newsService.userInited(this);
                        newsService.userLoaded(this);
                        if (this.localUser.getFirstPurchase()) {
                            this.send(Type.LOBBY, "show_achievements;{\"ids\":[0,1]}");
                        } else {
                            this.send(Type.LOBBY, "show_achievements;{\"ids\":[1]}");
                        }
                    }
                    if (cmd.args[0].equals("try_open_item")) {
                        giftsService.userOnGiftsWindowOpen(this);
                    }
                    if (cmd.args[0].equals("try_roll_item")) {
                        giftsService.tryRollItem(this);
                    }
                    if (cmd.args[0].equals("try_roll_items")) {
                        giftsService.rollItems(this, Integer.parseInt(cmd.args[1]));
                    }
                    if (cmd.args[0].equals("change_password")) {
                        Logger.debug("User " + this.localUser.getNickname() + " changed password to: " + cmd.args[1] + " previous was: " + this.localUser.getPassword());
                        this.localUser.setPassword(cmd.args[1]);
                        this.sendTableMessage("\u0412\u0430\u0448 \u043f\u0430\u0440\u043e\u043b\u044c \u0438\u0437\u043c\u0435\u043d\u0451\u043d");
                        database.update(this.localUser);
                    }
                    if (cmd.args[0].equals("update_profile")) {
                        userEmail = cmd.args[1];
                        LobbyManager.emailHandler.VerificationKey = verificationKey = emailHandler.generateKey();
                        LobbyManager.emailHandler.toEmail = userEmail;
                        emailHandler.sendEmailAsync(userEmail, verificationKey, this.localUser.getNickname());
                    }
                    if (cmd.args[0].equals("generate_key_email")) {
                        userEmail = this.localUser.getEmail();
                        LobbyManager.emailHandler.VerificationKey = verificationKey = emailHandler.generateKey();
                        LobbyManager.emailHandler.toEmail = userEmail;
                        emailHandler.sendEmailAsync(userEmail, verificationKey, this.localUser.getNickname());
                    }
                    if (cmd.args[0].equals("confirm_email_code_recovery")) {
                        if (cmd.args[1].equals(LobbyManager.emailHandler.VerificationKey)) {
                            this.send(Type.LOBBY, "open_recovery_window");
                        } else {
                            this.sendTableMessage("\u041d\u0435\u0432\u0435\u0440\u043d\u044b\u0439 \u043a\u043b\u044e\u0447 \u043f\u043e\u0434\u0442\u0432\u0435\u0440\u0436\u0434\u0435\u043d\u0438\u044f.");
                        }
                    }
                    if (cmd.args[0].equals("change_pass_email")) {
                        Logger.debug("User " + this.localUser.getNickname() + " has been channed password by email: " + cmd.args[1]);
                        this.localUser.setPassword(cmd.args[1]);
                        if (cmd.args.length > 2 && !cmd.args[2].trim().isEmpty()) {
                            this.localUser.setEmail(cmd.args[2].trim());
                        }
                        this.sendTableMessage("\u0412\u0430\u0448 \u043f\u0430\u0440\u043e\u043b\u044c \u0438\u0437\u043c\u0435\u043d\u0451\u043d");
                        database.update(this.localUser);
                    }
                    if (!cmd.args[0].equals("confirm_email_code")) break;
                    boolean emailExists = database.checkIfEmailExists(LobbyManager.emailHandler.toEmail);
                    if (cmd.args[1].equals(LobbyManager.emailHandler.VerificationKey) && !emailExists && this.localUser.getEmail() == null) {
                        Logger.debug("User " + this.localUser.getNickname() + " confirmed email successfully: " + LobbyManager.emailHandler.toEmail + " Verification Key from Command: " + cmd.args[1]);
                        this.addCrystall(5);
                        this.send(Type.LOBBY, "complete_achievement;1");
                        this.localUser.setEmail(LobbyManager.emailHandler.toEmail);
                        this.sendTableMessage("\u0412\u0430\u0448\u0430 \u043f\u043e\u0447\u0442\u0430 \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u043f\u0440\u0438\u0432\u044f\u0437\u0430\u043d\u0430.");
                        database.update(this.localUser);
                        break;
                    }
                    this.sendTableMessage("\u041d\u0435\u0432\u0435\u0440\u043d\u044b\u0439 \u043a\u043b\u044e\u0447 \u043f\u043e\u0434\u0442\u0432\u0435\u0440\u0436\u0434\u0435\u043d\u0438\u044f.");
                    break;
                }
                case BATTLE: {
                    if (this.battle != null) {
                        this.battle.executeCommand(cmd);
                    }
                    if (this.spectatorController == null) break;
                    this.spectatorController.executeCommand(cmd);
                    break;
                }
                case PING: {
                    break;
                }
                case UNKNOWN: {
                    break;
                }
                case HTTP: {
                    break;
                }
                case SYSTEM: {
                    String data = cmd.args[0];
                    if (!data.equals("c01")) break;
                    this.kick();
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void enterInBattleBySpectator(String battleId) {
        BattleInfo battle = BattlesList.getBattleInfoById(battleId);
        if (battle == null) {
            return;
        }
        this.spectatorController = new SpectatorController(this, battle.model, battle.model.spectatorModel);
        battle.model.spectatorModel.addSpectator(this.spectatorController);
        this.localUser.setUserLocation(UserLocation.BATTLE);
        this.send(Type.BATTLE, "init_battle_model", JSONUtils.parseBattleModelInfo(battle, true));
        Logger.log(String.format("User %s joined the battle as a spectator. Battle ID: %s", this.localUser.getNickname(), battleId));
    }

    private void sendTableMessage(String msg) {
        this.send(Type.LOBBY, "server_message", msg);
    }

    private void tryCreateDOMBattle(String json) {
        Date banTo;
        long currDate;
        long delta;
        if (this.localUser.getRang() < 1) {
            this.sendTableMessage("\u0412\u0430\u0448\u0435 \u0437\u0432\u0430\u043d\u0438\u0435 \u0434\u043e\u043b\u0436\u043d\u043e \u0431\u044b\u0442\u044c \u0431\u043e\u043b\u044c\u0448\u0435 \u0440\u044f\u0434\u043e\u0432\u043e\u0433\u043e.");
            ++this.localUser.getAntiCheatData().countWarningForFludCreateBattle;
            return;
        }
        if (System.currentTimeMillis() - this.localUser.getAntiCheatData().lastTimeCreationBattle <= 300000L) {
            if (this.localUser.getAntiCheatData().countCreatedBattles >= 3) {
                if (this.localUser.getAntiCheatData().countWarningForFludCreateBattle >= 5) {
                    this.kick();
                }
                this.sendTableMessage("\u0412\u044b \u043c\u043e\u0436\u0435\u0442\u0435 \u0441\u043e\u0437\u0434\u0430\u0432\u0430\u0442\u044c \u043d\u0435 \u0431\u043e\u043b\u0435\u0435 \u0442\u0440\u0435\u0445 \u0431\u0438\u0442\u0432 \u0432 \u0442\u0435\u0447\u0435\u043d\u0438\u0438 5 \u043c\u0438\u043d\u0443\u0442.");
                ++this.localUser.getAntiCheatData().countWarningForFludCreateBattle;
                return;
            }
        } else {
            this.localUser.getAntiCheatData().countCreatedBattles = 0;
            this.localUser.getAntiCheatData().countWarningForFludCreateBattle = 0;
        }
        JSONObject parser = null;
        try {
            parser = (JSONObject)new JSONParser().parse(json);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Karma karma = database.getKarmaByUser(this.localUser);
        if (karma.isChatBanned() && (delta = (currDate = System.currentTimeMillis()) - (banTo = karma.getChatBannedBefore()).getTime()) <= 0L) {
            this.sendTableMessage("\u0412\u044b \u043d\u0435 \u043c\u043e\u0436\u0435\u0442\u0435 \u0441\u043e\u0437\u0434\u0430\u0432\u0430\u0442\u044c \u0431\u0438\u0442\u0432\u044b \u0438\u0437-\u0437\u0430 \u0431\u043b\u043e\u043a\u0438\u0440\u043e\u0432\u043a\u0438 \u0447\u0430\u0442\u0430.");
            return;
        }
        BattleInfo battle = new BattleInfo();
        battle.battleType = "DOM";
        battle.withoutBonuses = (Boolean)parser.get("inventory");
        battle.isPaid = (Boolean)parser.get("pay");
        battle.isPrivate = (Boolean)parser.get("privateBattle");
        battle.friendlyFire = (Boolean)parser.get("frielndyFire");
        battle.name = (String)parser.get("gameName");
        battle.map = MapsLoader.maps.get((String)parser.get("mapId"));
        battle.maxPeople = (int)((Long)parser.get("numPlayers")).longValue();
        battle.numFlags = (int)((Long)parser.get("numPointsScore")).longValue();
        battle.minRank = (int)((Long)parser.get("minRang")).longValue();
        battle.maxRank = (int)((Long)parser.get("maxRang")).longValue();
        battle.team = true;
        battle.time = (int)((Long)parser.get("time")).longValue();
        battle.autobalance = (Boolean)parser.get("autoBalance");
        battle.battleCreator = this.localUser.getNickname();
        battle.createdTime = new Date();
        Map map = battle.map;
        if (battle.maxRank < battle.minRank) {
            battle.maxRank = battle.minRank;
        }
        if (battle.maxPeople < 1) {
            battle.maxPeople = 1;
        }
        if (battle.time <= 0 && battle.numFlags <= 0) {
            battle.time = 15;
            battle.numFlags = 0;
        }
        if (battle.maxPeople > map.maxPlayers) {
            battle.maxPeople = map.maxPlayers;
        }
        if (battle.numKills > 999) {
            battle.numKills = 999;
        }
        if (this.localUser.getRang() + 1 < battle.minRank) {
            return;
        }
        if (map.minRank > battle.minRank) {
            return;
        }
        if (battle.time > 59940) {
            battle.time = 59940;
        }
        if (battle.maxRank > 27) {
            return;
        }
        if (battle.name.startsWith("end~")) {
            return;
        }
        BattlesList.tryCreateBatle(battle);
        this.localUser.getAntiCheatData().lastTimeCreationBattle = System.currentTimeMillis();
        ++this.localUser.getAntiCheatData().countCreatedBattles;
    }

    private void tryCreateCTFBattle(String json) {
        Date banTo;
        long currDate;
        long delta;
        if (this.localUser.getRang() < 1) {
            this.sendTableMessage("\u0412\u0430\u0448\u0435 \u0437\u0432\u0430\u043d\u0438\u0435 \u0434\u043e\u043b\u0436\u043d\u043e \u0431\u044b\u0442\u044c \u0431\u043e\u043b\u044c\u0448\u0435 \u0440\u044f\u0434\u043e\u0432\u043e\u0433\u043e.");
            ++this.localUser.getAntiCheatData().countWarningForFludCreateBattle;
            return;
        }
        if (System.currentTimeMillis() - this.localUser.getAntiCheatData().lastTimeCreationBattle <= 300000L) {
            if (this.localUser.getAntiCheatData().countCreatedBattles >= 3) {
                if (this.localUser.getAntiCheatData().countWarningForFludCreateBattle >= 5) {
                    this.kick();
                }
                this.sendTableMessage("\u0412\u044b \u043c\u043e\u0436\u0435\u0442\u0435 \u0441\u043e\u0437\u0434\u0430\u0432\u0430\u0442\u044c \u043d\u0435 \u0431\u043e\u043b\u0435\u0435 \u0442\u0440\u0435\u0445 \u0431\u0438\u0442\u0432 \u0432 \u0442\u0435\u0447\u0435\u043d\u0438\u0438 5 \u043c\u0438\u043d\u0443\u0442.");
                ++this.localUser.getAntiCheatData().countWarningForFludCreateBattle;
                return;
            }
        } else {
            this.localUser.getAntiCheatData().countCreatedBattles = 0;
            this.localUser.getAntiCheatData().countWarningForFludCreateBattle = 0;
        }
        JSONObject parser = null;
        try {
            parser = (JSONObject)new JSONParser().parse(json);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Karma karma = database.getKarmaByUser(this.localUser);
        if (karma.isChatBanned() && (delta = (currDate = System.currentTimeMillis()) - (banTo = karma.getChatBannedBefore()).getTime()) <= 0L) {
            this.sendTableMessage("\u0412\u044b \u043d\u0435 \u043c\u043e\u0436\u0435\u0442\u0435 \u0441\u043e\u0437\u0434\u0430\u0432\u0430\u0442\u044c \u0431\u0438\u0442\u0432\u044b \u0438\u0437-\u0437\u0430 \u0431\u043b\u043e\u043a\u0438\u0440\u043e\u0432\u043a\u0438 \u0447\u0430\u0442\u0430.");
            return;
        }
        BattleInfo battle = new BattleInfo();
        battle.battleType = "CTF";
        battle.withoutBonuses = (Boolean)parser.get("inventory");
        battle.isPaid = (Boolean)parser.get("pay");
        battle.isPrivate = (Boolean)parser.get("privateBattle");
        battle.friendlyFire = (Boolean)parser.get("frielndyFire");
        battle.name = (String)parser.get("gameName");
        battle.map = MapsLoader.maps.get((String)parser.get("mapId"));
        battle.maxPeople = (int)((Long)parser.get("numPlayers")).longValue();
        battle.numFlags = (int)((Long)parser.get("numFlags")).longValue();
        battle.minRank = (int)((Long)parser.get("minRang")).longValue();
        battle.maxRank = (int)((Long)parser.get("maxRang")).longValue();
        battle.team = true;
        battle.time = (int)((Long)parser.get("time")).longValue();
        battle.autobalance = (Boolean)parser.get("autoBalance");
        battle.battleCreator = this.localUser.getNickname();
        battle.createdTime = new Date();
        Map map = battle.map;
        if (battle.maxRank < battle.minRank) {
            battle.maxRank = battle.minRank;
        }
        if (battle.maxPeople < 1) {
            battle.maxPeople = 1;
        }
        if (battle.time <= 0 && battle.numFlags <= 0) {
            battle.time = 15;
            battle.numFlags = 0;
        }
        if (battle.maxPeople > map.maxPlayers) {
            battle.maxPeople = map.maxPlayers;
        }
        if (battle.numFlags > 999) {
            battle.numFlags = 999;
        }
        if (this.localUser.getRang() + 1 < battle.minRank) {
            return;
        }
        if (map.minRank > battle.minRank) {
            return;
        }
        if (battle.time > 59940) {
            battle.time = 59940;
        }
        if (battle.maxRank > 27) {
            return;
        }
        if (battle.name.startsWith("end~")) {
            return;
        }
        BattlesList.tryCreateBatle(battle);
        this.localUser.getAntiCheatData().lastTimeCreationBattle = System.currentTimeMillis();
        ++this.localUser.getAntiCheatData().countCreatedBattles;
    }

    private void tryCreateTDMBattle(String json) {
        Date banTo;
        long currDate;
        long delta;
        if (this.localUser.getRang() < 1) {
            this.sendTableMessage("\u0412\u0430\u0448\u0435 \u0437\u0432\u0430\u043d\u0438\u0435 \u0434\u043e\u043b\u0436\u043d\u043e \u0431\u044b\u0442\u044c \u0431\u043e\u043b\u044c\u0448\u0435 \u0440\u044f\u0434\u043e\u0432\u043e\u0433\u043e.");
            ++this.localUser.getAntiCheatData().countWarningForFludCreateBattle;
            return;
        }
        if (System.currentTimeMillis() - this.localUser.getAntiCheatData().lastTimeCreationBattle <= 300000L) {
            if (this.localUser.getAntiCheatData().countCreatedBattles >= 3) {
                if (this.localUser.getAntiCheatData().countWarningForFludCreateBattle >= 5) {
                    this.kick();
                }
                this.sendTableMessage("\u0412\u044b \u043c\u043e\u0436\u0435\u0442\u0435 \u0441\u043e\u0437\u0434\u0430\u0432\u0430\u0442\u044c \u043d\u0435 \u0431\u043e\u043b\u0435\u0435 \u0442\u0440\u0435\u0445 \u0431\u0438\u0442\u0432 \u0432 \u0442\u0435\u0447\u0435\u043d\u0438\u0438 5 \u043c\u0438\u043d\u0443\u0442.");
                ++this.localUser.getAntiCheatData().countWarningForFludCreateBattle;
                return;
            }
        } else {
            this.localUser.getAntiCheatData().countCreatedBattles = 0;
            this.localUser.getAntiCheatData().countWarningForFludCreateBattle = 0;
        }
        JSONObject parser = null;
        try {
            parser = (JSONObject)new JSONParser().parse(json);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Karma karma = database.getKarmaByUser(this.localUser);
        if (karma.isChatBanned() && (delta = (currDate = System.currentTimeMillis()) - (banTo = karma.getChatBannedBefore()).getTime()) <= 0L) {
            this.sendTableMessage("\u0412\u044b \u043d\u0435 \u043c\u043e\u0436\u0435\u0442\u0435 \u0441\u043e\u0437\u0434\u0430\u0432\u0430\u0442\u044c \u0431\u0438\u0442\u0432\u044b \u0438\u0437-\u0437\u0430 \u0431\u043b\u043e\u043a\u0438\u0440\u043e\u0432\u043a\u0438 \u0447\u0430\u0442\u0430.");
            return;
        }
        BattleInfo battle = new BattleInfo();
        battle.battleType = "TDM";
        battle.withoutBonuses = (Boolean)parser.get("inventory");
        battle.isPaid = (Boolean)parser.get("pay");
        battle.isPrivate = (Boolean)parser.get("privateBattle");
        battle.friendlyFire = (Boolean)parser.get("frielndyFire");
        battle.name = (String)parser.get("gameName");
        battle.map = MapsLoader.maps.get((String)parser.get("mapId"));
        battle.maxPeople = (int)((Long)parser.get("numPlayers")).longValue();
        battle.numKills = (int)((Long)parser.get("numKills")).longValue();
        battle.minRank = (int)((Long)parser.get("minRang")).longValue();
        battle.maxRank = (int)((Long)parser.get("maxRang")).longValue();
        battle.team = true;
        battle.time = (int)((Long)parser.get("time")).longValue();
        battle.autobalance = (Boolean)parser.get("autoBalance");
        battle.battleCreator = this.localUser.getNickname();
        battle.createdTime = new Date();
        Map map = battle.map;
        if (battle.maxRank < battle.minRank) {
            battle.maxRank = battle.minRank;
        }
        if (battle.maxPeople < 1) {
            battle.maxPeople = 1;
        }
        if (battle.time <= 0 && battle.numKills <= 0) {
            battle.time = 900;
            battle.numKills = 0;
        }
        if (battle.maxPeople > map.maxPlayers) {
            battle.maxPeople = map.maxPlayers;
        }
        if (battle.numKills > 999) {
            battle.numKills = 999;
        }
        if (this.localUser.getRang() + 1 < battle.minRank) {
            return;
        }
        if (battle.time > 59940) {
            battle.time = 59940;
        }
        if (battle.maxRank > 27) {
            return;
        }
        if (map.minRank > battle.minRank) {
            return;
        }
        if (battle.name.startsWith("end~")) {
            return;
        }
        BattlesList.tryCreateBatle(battle);
        this.localUser.getAntiCheatData().lastTimeCreationBattle = System.currentTimeMillis();
        ++this.localUser.getAntiCheatData().countCreatedBattles;
    }

    public void onExitFromBattle() {
        if (this.battle != null) {
            this.battle.destroy(this.autoEntryServices.removePlayer(this.battle.battle, this.getLocalUser().getNickname(), this.battle.playerTeamType, this.battle.battle.battleInfo.team));
            this.battle = null;
            this.disconnectListener.removeListener(this.battle);
            OnlineStats.removeInBattleOnline();
        }
        if (this.spectatorController != null) {
            this.spectatorController.onDisconnect();
            this.spectatorController = null;
        }
        this.send(Type.LOBBY_CHAT, "init_messages", JSONUtils.parseChatLobbyMessages(chatLobby.getMessages()));
    }

    public void onExitFromStatistic() {
        this.onExitFromBattle();
        this.sendMapsInit();
    }

    private void onEnterInTeamBattle(String battleId, boolean red) {
        BattleInfo battleInfo = BattlesList.getBattleInfoById(battleId);
        if (this.getLocalUser().getRang() + 1 < battleInfo.minRank) {
            return;
        }
        if (this.getLocalUser().getRang() - 1 > battleInfo.maxRank) {
            return;
        }
        if (battleInfo.battleType.equals("DM")) {
            return;
        }
        this.localUser.setUserLocation(UserLocation.BATTLE);
        if (this.battle != null) {
            return;
        }
        if (battleInfo == null) {
            return;
        }
        if (battleInfo.model.players.size() >= battleInfo.maxPeople * 2) {
            return;
        }
        if (red) {
            ++battleInfo.redPeople;
        } else {
            ++battleInfo.bluePeople;
        }
        if (battleInfo.isPaid && !this.localUser.getGarage().containsItem("no_supplies")) {
            if (this.getLocalUser().getCrystall() < 5) {
                Logger.debug("Detected User " + this.localUser.getNickname() + " tried to join paid battle without required money.");
                return;
            }
            this.addCrystall(-5);
        }
        this.battle = new BattlefieldPlayerController(this, battleInfo.model, red ? "RED" : "BLUE");
        this.disconnectListener.addListener(this.battle);
        lobbysServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.BATTLESELECT, "update_count_users_in_team_battle", JSONUtils.parseUpdateCoundPeoplesCommand(battleInfo));
        this.send(Type.BATTLE, "init_battle_model", JSONUtils.parseBattleModelInfo(battleInfo, false));
        lobbysServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.BATTLESELECT, "add_player_to_battle", JSONUtils.parseAddPlayerComand(this.battle, battleInfo));
    }

    public void onEnterInBattle(String battleId) {
        BattleInfo battleInfo = BattlesList.getBattleInfoById(battleId);
        if (this.getLocalUser().getRang() + 1 < battleInfo.minRank) {
            Logger.debug("Detected User " + this.localUser.getNickname() + " Attempted to join in battle with low rank");
            return;
        }
        if (this.getLocalUser().getRang() - 1 > battleInfo.maxRank) {
            Logger.debug("Detected User " + this.localUser.getNickname() + " Attempted to join in battle with High rank");
            return;
        }
        if (battleInfo.team) {
            Logger.debug("User " + this.localUser.getNickname() + " attempted to join in DM battle like team player");
            return;
        }
        this.localUser.setUserLocation(UserLocation.BATTLE);
        this.autoEntryServices.removePlayer(this.getLocalUser().getNickname());
        if (this.battle != null) {
            return;
        }
        if (battleInfo == null) {
            return;
        }
        if (battleInfo.model.players.size() >= battleInfo.maxPeople) {
            return;
        }
        if (battleInfo.isPaid && !this.localUser.getGarage().containsItem("no_supplies")) {
            if (this.getLocalUser().getCrystall() < 5) {
                Logger.log("Detected User " + this.localUser.getNickname() + " tried to join paid battle without required money.");
                return;
            }
            this.addCrystall(-5);
        }
        this.battle = new BattlefieldPlayerController(this, battleInfo.model, "NONE");
        this.disconnectListener.addListener(this.battle);
        ++battleInfo.countPeople;
        if (!battleInfo.team) {
            lobbysServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.BATTLESELECT, StringUtils.concatStrings("update_count_users_in_dm_battle", ";", battleId, ";", String.valueOf(this.battle.battle.battleInfo.countPeople)));
        } else {
            lobbysServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.BATTLESELECT, "update_count_users_in_team_battle", JSONUtils.parseUpdateCoundPeoplesCommand(battleInfo));
        }
        this.send(Type.BATTLE, "init_battle_model", JSONUtils.parseBattleModelInfo(battleInfo, false));
        lobbysServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.BATTLESELECT, "add_player_to_battle", JSONUtils.parseAddPlayerComand(this.battle, battleInfo));
    }

    private void sendBattleInfo(String id) {
        this.send(Type.LOBBY, "show_battle_info", JSONUtils.parseBattleInfoShow(BattlesList.getBattleInfoById(id), this.getLocalUser().getType() != TypeUser.DEFAULT && this.getLocalUser().getType() != TypeUser.TESTER, this.localUser));
    }

    private void tryCreateBattleDM(String gameName, String mapId, int time, int kills, int maxPlayers, int minRang, int maxRang, boolean isPrivate, boolean pay, boolean d) {
        Date banTo;
        long currDate;
        long delta;
        Karma karma;
        if (this.localUser.getRang() < 1) {
            this.sendTableMessage("\u0412\u0430\u0448\u0435 \u0437\u0432\u0430\u043d\u0438\u0435 \u0434\u043e\u043b\u0436\u043d\u043e \u0431\u044b\u0442\u044c \u0431\u043e\u043b\u044c\u0448\u0435 \u0440\u044f\u0434\u043e\u0432\u043e\u0433\u043e.");
            ++this.localUser.getAntiCheatData().countWarningForFludCreateBattle;
            return;
        }
        if (System.currentTimeMillis() - this.localUser.getAntiCheatData().lastTimeCreationBattle <= 300000L) {
            if (this.localUser.getAntiCheatData().countCreatedBattles >= 3) {
                if (this.localUser.getAntiCheatData().countWarningForFludCreateBattle >= 5) {
                    this.kick();
                }
                this.sendTableMessage("\u0412\u044b \u043c\u043e\u0436\u0435\u0442\u0435 \u0441\u043e\u0437\u0434\u0430\u0432\u0430\u0442\u044c \u043d\u0435 \u0431\u043e\u043b\u0435\u0435 \u0442\u0440\u0435\u0445 \u0431\u0438\u0442\u0432 \u0432 \u0442\u0435\u0447\u0435\u043d\u0438\u0438 5 \u043c\u0438\u043d\u0443\u0442.");
                ++this.localUser.getAntiCheatData().countWarningForFludCreateBattle;
                return;
            }
        } else {
            this.localUser.getAntiCheatData().countCreatedBattles = 0;
            this.localUser.getAntiCheatData().countWarningForFludCreateBattle = 0;
        }
        if ((karma = database.getKarmaByUser(this.localUser)).isChatBanned() && (delta = (currDate = System.currentTimeMillis()) - (banTo = karma.getChatBannedBefore()).getTime()) <= 0L) {
            this.sendTableMessage("\u0412\u044b \u043d\u0435 \u043c\u043e\u0436\u0435\u0442\u0435 \u0441\u043e\u0437\u0434\u0430\u0432\u0430\u0442\u044c \u0431\u0438\u0442\u0432\u044b \u0438\u0437-\u0437\u0430 \u0431\u043b\u043e\u043a\u0438\u0440\u043e\u0432\u043a\u0438 \u0447\u0430\u0442\u0430.");
            return;
        }
        BattleInfo battle = new BattleInfo();
        Map map = MapsLoader.maps.get(mapId);
        if (maxRang < minRang) {
            maxRang = minRang;
        }
        if (maxPlayers < 2) {
            maxPlayers = 2;
        }
        if (time <= 0 && kills <= 0) {
            time = 900;
            kills = 0;
        }
        if (maxPlayers > map.maxPlayers) {
            maxPlayers = map.maxPlayers;
        }
        if (kills > 999) {
            kills = 999;
        }
        if (this.localUser.getRang() + 1 < battle.minRank) {
            return;
        }
        if (battle.time > 59940) {
            battle.time = 59940;
        }
        if (battle.maxRank > 27) {
            return;
        }
        if (map.minRank > minRang) {
            return;
        }
        if (gameName.startsWith("end~")) {
            return;
        }
        battle.name = gameName;
        battle.map = MapsLoader.maps.get(mapId);
        battle.time = time;
        battle.numKills = kills;
        battle.maxPeople = maxPlayers;
        battle.minRank = minRang;
        battle.countPeople = 0;
        battle.maxRank = maxRang;
        battle.team = false;
        battle.isPrivate = isPrivate;
        battle.isPaid = pay;
        battle.withoutBonuses = d;
        battle.battleCreator = this.localUser.getNickname();
        battle.createdTime = new Date();
        BattlesList.tryCreateBatle(battle);
        this.localUser.getAntiCheatData().lastTimeCreationBattle = System.currentTimeMillis();
        ++this.localUser.getAntiCheatData().countCreatedBattles;
    }

    private void checkBattleName(String name) {
        this.send(Type.LOBBY, "check_battle_name", name);
    }

    private void sendMapsInit() {
        this.localUser.setUserLocation(UserLocation.BATTLESELECT);
        this.send(Type.LOBBY, "init_battle_select", JSONUtils.parseBattleMapList(this.localUser));
    }

    private void sendGarage() {
        this.localUser.setUserLocation(UserLocation.GARAGE);
        this.send(Type.GARAGE, "init_garage_items", JSONUtils.parseGarageUser(this.localUser));
        this.send(Type.GARAGE, "init_market", JSONUtils.parseMarketItems(this.localUser));
    }

    public void initHallOfFameData() {
    }

    public synchronized void onTryUpdateItem(String id) {
        Item item = this.localUser.getGarage().getItemById(id.substring(0, id.length() - 3));
        int modificationID = Integer.parseInt(id.substring(id.length() - 1));
        if (this.checkMoney(item.modifications[modificationID + 1].price)) {
            if (this.getLocalUser().getRang() + 1 < item.modifications[modificationID + 1].rank) {
                Logger.log("Player " + this.localUser.getNickname() + " tried to buy item: " + item.id + " item rank: " + item.rankId + " but does not has enough rank (user: " + this.localUser.getRang() + " rang)");
                return;
            }
            if (this.getLocalUser().getCrystall() < item.price) {
                Logger.log("Player " + this.localUser.getNickname() + " tried to buy item: " + item.id + " for " + item.price + " but does not has enough crystals (user: " + this.localUser.getCrystall() + " crystals)");
                return;
            }
            if (this.localUser.getGarage().updateItem(id)) {
                this.send(Type.GARAGE, "update_item", id);
                this.addCrystall(-item.modifications[modificationID + 1].price);
                this.localUser.getGarage().parseJSONData();
                database.update(this.localUser.getGarage());
                if (this.localUser.getFirstPurchase()) {
                    this.localUser.setFirstPurchase(false);
                    this.addCrystall(5);
                    this.send(Type.LOBBY, "complete_achievement;0");
                    Logger.log("[FirstPurchaseCompleted]: [" + this.localUser.getNickname() + "] buyed first purchase");
                }
                Logger.log("User " + this.localUser.getNickname() + " Upgraded " + item.id + " -> modification: M" + item.modificationIndex + " Price: " + item.modifications[modificationID + 1].price);
            }
        } else {
            this.send(Type.GARAGE, "try_update_NO");
        }
    }

    public synchronized void onTryBuyItem(String itemId, int count) {
        if (count <= 0 || count > 9999) {
            this.crystallToZero();
            return;
        }
        Item item = GarageItemsLoader.items.get(itemId.substring(0, itemId.length() - 3));
        int price = item.price * count;
        int itemRang = item.modifications[0].rank;
        if (this.checkMoney(price)) {
            Item fromUser;
            if (this.getLocalUser().getRang() + 1 < itemRang) {
                Logger.log("Player " + this.localUser.getNickname() + " tried to buy item: " + itemId + " item rank: " + item.rankId + " but does not has enough rank (user: " + this.localUser.getRang() + " rang)");
                return;
            }
            if (this.getLocalUser().getCrystall() < price) {
                Logger.log("Player " + this.localUser.getNickname() + " tried to buy item: " + itemId + " for " + item.price + " but does not has enough crystals (user: " + this.localUser.getCrystall() + " crystals)");
                return;
            }
            if (item.itemType == ItemType.PLUGIN) {
                this.send(Type.LOBBY, "set_reamining_time", item.id + "_m0", String.valueOf(item.timeRemaining));
            }
            if (item.id.equals("1000_scores")) {
                TanksServices.getInstance().addScore(this.networker.lobby, 1000 * count);
            }
            if ((fromUser = this.localUser.getGarage().buyItem(itemId, count, this.localUser.getType())) != null) {
                this.send(Type.GARAGE, "buy_item", StringUtils.concatStrings(item.id, "_m", String.valueOf(item.modificationIndex)), JSONUtils.parseItemInfo(fromUser));
                this.addCrystall(-price);
                if (count > 1) {
                    Logger.log("User " + this.localUser.getNickname() + " Bought supply " + item.id + "(count: " + count + " price: " + item.price * count + " crystals)");
                } else {
                    Logger.log("User " + this.localUser.getNickname() + " Bought item " + itemId + " price: " + item.price);
                }
                this.localUser.getGarage().parseJSONData();
                database.update(this.localUser.getGarage());
                if (this.localUser.getFirstPurchase()) {
                    this.localUser.setFirstPurchase(false);
                    this.addCrystall(5);
                    this.send(Type.LOBBY, "complete_achievement;0");
                    Logger.log("[FirstPurchaseCompleted]: [" + this.localUser.getNickname() + "] buyed first purchase");
                }
            } else {
                this.send(Type.GARAGE, "try_buy_item_NO");
            }
        }
    }

    private boolean checkMoney(int buyValue) {
        return this.localUser.getCrystall() - buyValue >= 0;
    }

    public synchronized void addCrystall(int value) {
        this.localUser.addCrystall(value);
        this.send(Type.LOBBY, "add_crystall", String.valueOf(this.localUser.getCrystall()));
        database.update(this.localUser);
    }

    public synchronized void dummyAddCrystall(int value) {
        this.localUser.addCrystall(value);
        database.update(this.localUser);
    }

    public void crystallToZero() {
        this.localUser.setCrystall(0);
        this.send(Type.LOBBY, "add_crystall", String.valueOf(this.localUser.getCrystall()));
        database.update(this.localUser);
    }

    private boolean stringToBoolean(String src) {
        return src.equalsIgnoreCase("true");
    }

    public void onDisconnect() {
        database.uncache(this.localUser.getNickname());
        lobbysServices.removeLobby(this);
        OnlineStats.removeOnline();
        if (this.spectatorController != null) {
            this.spectatorController.onDisconnect();
            this.spectatorController = null;
        }
        if (this.battle != null) {
            this.battle.onDisconnect();
            this.battle = null;
        }
        this.localUser.session = null;
        this.packetDelayService.shutdown();
    }

    public void kick() {
        this.onExitFromBattle();
        this.networker.closeConnection();
    }

    public User getLocalUser() {
        return this.localUser;
    }

    public AntiFloodHandler getChatFloodController() {
        return this.chatFloodController;
    }

    public void setChatFloodController(AntiFloodHandler chatFloodController) {
        this.chatFloodController = chatFloodController;
    }
}