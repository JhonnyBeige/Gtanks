/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.lobby.chat;

import gtanks.battles.maps.MapsLoader;
import gtanks.commands.Type;
import gtanks.json.JSONUtils;
import gtanks.lobby.LobbyManager;
import gtanks.services.PromocodeService;
import gtanks.lobby.battles.BattleInfo;
import gtanks.lobby.battles.BattlesList;
import gtanks.logger.Logger;
import gtanks.main.database.DatabaseManager;
import gtanks.main.database.impl.DatabaseManagerImpl;
import gtanks.main.netty.NettyUsersHandler;
import gtanks.main.params.OnlineStats;
import gtanks.services.BanServices;
import gtanks.services.LobbysServices;
import gtanks.services.TanksServices;
import gtanks.services.annotations.ServicesInject;
import gtanks.services.ban.BanChatCommads;
import gtanks.services.ban.BanTimeType;
import gtanks.services.ban.BanType;
import gtanks.services.ban.DateFormater;
import gtanks.services.ban.block.BlockGameReason;
import gtanks.system.timers.SystemTimerScheduler;
import gtanks.users.User;
import gtanks.users.karma.Karma;
import gtanks.users.locations.UserLocation;
import gtanks.utils.ResourceUtils;
import gtanks.utils.StringUtils;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Predicate;

public class ChatModel {
    private static final String SYSTEM_TIPS_FILE = ResourceUtils.data("txt/systemTips.txt");
    private static final ChatModel instance = new ChatModel();
    private final ArrayList<ChatMessage> chatMessages;
    private boolean stoped = false;
    @ServicesInject(target=TanksServices.class)
    private final TanksServices tanksServices = TanksServices.getInstance();
    @ServicesInject(target=LobbysServices.class)
    private final LobbysServices lobbyServices = LobbysServices.getInstance();
    @ServicesInject(target=DatabaseManagerImpl.class)
    private final DatabaseManager database = DatabaseManagerImpl.instance();
    @ServicesInject(target=BanServices.class)
    private final BanServices banServices = BanServices.getInstance();
    @ServicesInject(target= PromocodeService.class)
    private final PromocodeService promocodeService = PromocodeService.instance();
    private long currDate;
    private static List<String> forbiddenWordParts;
    private static final long SYSTEM_TIPS_PERIOD_MS = 60000L;
    private volatile List<String> systemTipsMessages = new ArrayList<String>();
    private int systemTipIndex = 0;

    public static ChatModel getInstance() {
        return instance;
    }

    private ChatModel() {
        this.chatMessages = new ArrayList();
        forbiddenWordParts = ChatModel.loadForbiddenWordParts();
        this.systemTipsMessages = this.loadSystemTipsMessages();
        this.startSystemTipsBroadcastTask();
    }

    private void startSystemTipsBroadcastTask() {
        Timer timer = new Timer("LobbySystemTipsTimer", true);
        timer.scheduleAtFixedRate(new TimerTask(){

            @Override
            public void run() {
                ChatModel.this.broadcastSystemTip();
            }
        }, SYSTEM_TIPS_PERIOD_MS, SYSTEM_TIPS_PERIOD_MS); // первый SYSTEM_TIPS_PERIOD_MS для первого дропа типа при запуске серва через 1 мин,если надо сразу то меняем SYSTEM_TIPS_PERIOD_MS на 0L
    }

    private synchronized void broadcastSystemTip() {
        this.systemTipsMessages = this.loadSystemTipsMessages();
        if (this.systemTipsMessages.isEmpty()) {
            return;
        }
        if (this.systemTipIndex >= this.systemTipsMessages.size()) {
            this.systemTipIndex = 0;
        }
        String tipMessage = this.systemTipsMessages.get(this.systemTipIndex);
        ++this.systemTipIndex;
        this.sendSystemMessageToAll(tipMessage, true);
    }

    private List<String> loadSystemTipsMessages() {
        ArrayList<String> messages = new ArrayList<String>();
        try {
            String content = new String(Files.readAllBytes(Paths.get(SYSTEM_TIPS_FILE)), StandardCharsets.UTF_8).trim();
            if (content.isEmpty()) {
                return messages;
            }
            content = content.replace("\r\n", "\n").replace("\r", "\n").replace("\\n", "\n");
            String[] tips = content.split("\\n");
            for (String rawTip : tips) {
                String tip = rawTip.trim();
                if (tip.isEmpty()) continue;
                messages.add(tip);
            }
        }
        catch (IOException e) {
            Logger.log("Failed to load system tips from file: " + e.getMessage());
        }
        return messages;
    }

    public static List<String> loadForbiddenWordParts() {
        Logger.log("Antimat Service has been configurated");
        ArrayList<String> forbiddenWordParts = new ArrayList<String>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(ResourceUtils.data("txt/antimat.txt")), StandardCharsets.UTF_8));){
            String line;
            while ((line = reader.readLine()) != null) {
                forbiddenWordParts.add(line.trim());
            }
        } catch (IOException e) {
            Logger.log("Failed to load forbidden words from file: " + e.getMessage());
        }
        return forbiddenWordParts;
    }

    public void addMessage(ChatMessage msg) {
        this.checkSyntax(msg);
    }

    public void checkSyntax(ChatMessage msg) {
        long delta;
        if (msg.message.startsWith("/code")) {
            try {
                String promoCode = msg.message.substring(6);
                this.promocodeService.checkPromoCode(msg.localLobby, promoCode);
            } catch (Exception promoCode) {
                // empty catch block
            }
            return;
        }
        if (msg.localLobby.getLocalUser().getRang() < 1) {
            msg.localLobby.send(Type.LOBBY_CHAT, "system", "\u0427\u0430\u0442 \u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d, \u043d\u0430\u0447\u0438\u043d\u0430\u044f \u0441\u043e \u0437\u0432\u0430\u043d\u0438\u044f \u0420\u044f\u0434\u043e\u0432\u043e\u0439");
            return;
        }
        if (msg.message.contains("end~")) {
            return;
        }
        Logger.log("[LOBBY_CHAT_LOG_OUT]: Nickname " + msg.user.getNickname() + " said: \"" + msg.message + "\"");
        msg.message = ChatModel.filter(msg.message.trim());
        Karma karma = this.database.getKarmaByUser(msg.user);
        if (karma.isChatBanned() && (delta = this.setCurrDate(System.currentTimeMillis()) - this.setBanTo(karma.getChatBannedBefore()).getTime()) <= 0L) {
            msg.localLobby.send(Type.LOBBY_CHAT, "system", StringUtils.concatStrings("\u0412\u044b \u043e\u0442\u043a\u043b\u044e\u0447\u0435\u043d\u044b \u043e\u0442 \u0447\u0430\u0442\u0430. \u0412\u044b \u0432\u0435\u0440\u043d\u0451\u0442\u0435\u0441\u044c \u0432 \u042d\u0424\u0418\u0420 \u0447\u0435\u0440\u0435\u0437 ", DateFormater.formatTimeToUnban(delta), ". \u041f\u0440\u0438\u0447\u0438\u043d\u0430: " + karma.getReasonChatBan()));
            return;
        }
        if (msg.message.startsWith("/")) {
            String temp = msg.message.replace('/', ' ').trim();
            String[] arguments = temp.split(" ");
            if (msg.user.getUserGroup().isAvaliableChatCommand(arguments[0])) {
                return;
            }
            switch (arguments[0]) {
                case "help": {
                    msg.localLobby.send(Type.LOBBY_CHAT, "system", String.valueOf(msg.user.getUserGroup()));
                    break;
                }
                case "cb": {
                    if (arguments.length >= 8) {
                        BattleInfo battle = new BattleInfo();
                        battle.map = MapsLoader.maps.get(arguments[1]);
                        battle.maxPeople = Integer.parseInt(arguments[2]);
                        battle.minRank = Integer.parseInt(arguments[3]);
                        battle.maxRank = Integer.parseInt(arguments[4]);
                        battle.time = Integer.parseInt(arguments[5]);
                        int lastIndex = arguments.length - 1;
                        battle.numKills = Integer.parseInt(arguments[lastIndex]);
                        StringBuilder nameBuilder = new StringBuilder();
                        for (int i = 6; i < lastIndex; ++i) {
                            nameBuilder.append(arguments[i]);
                            nameBuilder.append(" ");
                        }
                        battle.name = nameBuilder.toString();
                        battle.battleType = "DM";
                        battle.countPeople = 0;
                        battle.team = false;
                        battle.isPrivate = false;
                        battle.isPaid = false;
                        battle.withoutBonuses = false;
                        BattlesList.tryCreateBatle(battle);
                        msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: SYSTEM BATTLE HAS BEEN CREATED!");
                        break;
                    }
                    msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: Invalid arguments for creating a battle. Usage: cb [map_id] [maxPeople] [minRank] [maxRank] [time] [Battle_name] [countKills]");
                    break;
                }
                case "cbtdm": {
                    if (arguments.length >= 7) {
                        BattleInfo battle = new BattleInfo();
                        battle.map = MapsLoader.maps.get(arguments[1]);
                        battle.maxPeople = Integer.parseInt(arguments[2]);
                        battle.minRank = Integer.parseInt(arguments[3]);
                        battle.maxRank = Integer.parseInt(arguments[4]);
                        battle.time = Integer.parseInt(arguments[5]);
                        int lastIndex = arguments.length - 1;
                        battle.numKills = Integer.parseInt(arguments[lastIndex]);
                        StringBuilder nameBuilder = new StringBuilder();
                        for (int i = 6; i < lastIndex; ++i) {
                            nameBuilder.append(arguments[i]);
                            nameBuilder.append(" ");
                        }
                        battle.name = nameBuilder.toString();
                        battle.battleType = "TDM";
                        battle.countPeople = 0;
                        battle.team = true;
                        battle.friendlyFire = false;
                        battle.autobalance = false;
                        battle.isPrivate = false;
                        battle.isPaid = false;
                        battle.withoutBonuses = false;
                        BattlesList.tryCreateBatle(battle);
                        msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: SYSTEM BATTLE HAS BEEN CREATED!");
                        break;
                    }
                    msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: Invalid arguments for creating a battle. Usage: cbtdm [map_id] [maxPeople] [minRank] [maxRank] [time] [Battle_name] [countKills]");
                    break;
                }
                case "cbctf": {
                    if (arguments.length >= 7) {
                        BattleInfo battle = new BattleInfo();
                        battle.map = MapsLoader.maps.get(arguments[1]);
                        battle.maxPeople = Integer.parseInt(arguments[2]);
                        battle.minRank = Integer.parseInt(arguments[3]);
                        battle.maxRank = Integer.parseInt(arguments[4]);
                        battle.time = Integer.parseInt(arguments[5]);
                        int lastIndex = arguments.length - 1;
                        battle.numFlags = Integer.parseInt(arguments[lastIndex]);
                        StringBuilder nameBuilder = new StringBuilder();
                        for (int i = 6; i < lastIndex; ++i) {
                            nameBuilder.append(arguments[i]);
                            nameBuilder.append(" ");
                        }
                        battle.name = nameBuilder.toString();
                        battle.battleType = "CTF";
                        battle.countPeople = 0;
                        battle.team = true;
                        battle.friendlyFire = false;
                        battle.autobalance = false;
                        battle.isPrivate = false;
                        battle.isPaid = false;
                        battle.withoutBonuses = false;
                        BattlesList.tryCreateBatle(battle);
                        msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: SYSTEM BATTLE HAS BEEN CREATED!");
                        break;
                    }
                    msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: Invalid arguments for creating a battle. Usage: cbctf [map_id] [maxPeople] [minRank] [maxRank] [time] [Battle_name] [countFlags]");
                    break;
                }
                case "cbdom": {
                    if (arguments.length >= 7) {
                        BattleInfo battle = new BattleInfo();
                        battle.map = MapsLoader.maps.get(arguments[1]);
                        battle.maxPeople = Integer.parseInt(arguments[2]);
                        battle.minRank = Integer.parseInt(arguments[3]);
                        battle.maxRank = Integer.parseInt(arguments[4]);
                        battle.time = Integer.parseInt(arguments[5]);
                        int lastIndex = arguments.length - 1;
                        battle.numFlags = Integer.parseInt(arguments[lastIndex]);
                        StringBuilder nameBuilder = new StringBuilder();
                        for (int i = 6; i < lastIndex; ++i) {
                            nameBuilder.append(arguments[i]);
                            nameBuilder.append(" ");
                        }
                        battle.name = nameBuilder.toString();
                        battle.battleType = "DOM";
                        battle.countPeople = 0;
                        battle.team = true;
                        battle.friendlyFire = false;
                        battle.autobalance = false;
                        battle.isPrivate = false;
                        battle.isPaid = false;
                        battle.withoutBonuses = false;
                        BattlesList.tryCreateBatle(battle);
                        msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: SYSTEM BATTLE HAS BEEN CREATED!");
                        break;
                    }
                    msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: Invalid arguments for creating a battle. Usage: cbdom [map_id] [maxPeople] [minRank] [maxRank] [time] [Battle_name] [countPoints]");
                    break;
                }
                case "system": {
                    if (arguments.length < 2) break;
                    this.sendSystemMessageToAll(arguments, false);
                    break;
                }
                case "warn": {
                    this.sendSystemMessageToAll(arguments, true);
                    break;
                }
                case "clear": {
                    this.clear();
                    break;
                }
                case "stop": {
                    this.stoped = true;
                    this.sendSystemMessageToAll("\u0427\u0430\u0442 \u043e\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d", false);
                    break;
                }
                case "start": {
                    this.stoped = false;
                    this.sendSystemMessageToAll("\u0427\u0430\u0442 \u0437\u0430\u043f\u0443\u0449\u0435\u043d", false);
                    break;
                }
                case "addcry": {
                    if (arguments.length < 2) break;
                    this.tanksServices.addCrystall(msg.localLobby, this.getInt(arguments[1]));
                    break;
                }
                case "addscore": {
                    if (arguments.length < 2) break;
                    int score = this.getInt(arguments[1]);
                    if (msg.localLobby.getLocalUser().getScore() + score < 0) {
                        msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: \u0412\u0430\u0448\u0435 \u043a\u043e\u043b\u0438\u0447\u0435\u0441\u0442\u0432\u043e \u043e\u0447\u043a\u043e\u0432 \u043e\u043f\u044b\u0442\u0430 \u043d\u0435 \u0434\u043e\u043b\u0436\u043d\u043e \u0431\u044b\u0442\u044c \u043e\u0442\u0440\u0438\u0446\u0430\u0442\u0435\u043b\u044c\u043d\u043e\u0435!");
                        break;
                    }
                    this.tanksServices.addScore(msg.localLobby, score);
                    break;
                }
                case "kick": {
                    if (arguments.length < 2) break;
                    User _userForKick = this.database.getUserById(arguments[1]);
                    if (_userForKick == null) {
                        msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: \u0418\u0433\u0440\u043e\u043a \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d");
                        break;
                    }
                    LobbyManager _lobby = this.lobbyServices.getLobbyByUser(_userForKick);
                    if (_lobby == null) break;
                    _lobby.kick();
                    this.sendSystemMessageToAll(_userForKick.getNickname() + " \u043a\u0438\u043a\u043d\u0443\u0442", false);
                    break;
                }
                case "online": {
                    msg.localLobby.send(Type.LOBBY_CHAT, "system", "Current online: " + OnlineStats.getOnline() + "\nMax online: " + OnlineStats.getMaxOnline() + "\nIn battles: " + OnlineStats.getInBattlesOnline());
                    break;
                }
                case "rbattle": {
                    if (arguments.length < 2) break;
                    StringBuilder id = new StringBuilder();
                    for (int i = 1; i < arguments.length; ++i) {
                        id.append(arguments[i]).append(" ");
                    }
                    BattleInfo battle = BattlesList.getBattleInfoById(id.toString().trim().replace("#battle", ""));
                    if (battle == null) {
                        msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: \u0411\u0438\u0442\u0432\u0430 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u0430");
                        break;
                    }
                    if (battle.model != null) {
                        battle.model.sendTableMessageToPlayers("\u0411\u0438\u0442\u0432\u0430 \u0431\u044b\u043b\u0430 \u0434\u043e\u0441\u0440\u043e\u0447\u043d\u0430 \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043d\u0430, \u0441\u043a\u043e\u0440\u043e \u0432\u044b \u0431\u0443\u0434\u0435\u0442\u0435 \u043a\u0438\u043a\u043d\u0443\u0442\u044b");
                    }
                    SystemTimerScheduler.scheduleTask(() -> {
                        this.sendSystemMessageToAll("\u0411\u0438\u0442\u0432\u0430 " + battle.name + " \u0431\u044b\u043b\u0430 \u043f\u0440\u0438\u043d\u0443\u0434\u0438\u0442\u0435\u043b\u044c\u043d\u043e \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043d\u0430", false);
                        BattlesList.removeBattle(battle);
                    }, 4000L);
                    msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: \u0411\u0438\u0442\u0432\u0430 \u0431\u0443\u0434\u0435\u0442 \u0443\u0434\u0430\u043b\u0435\u043d\u0430 \u0447\u0435\u0440\u0435\u0437 4 \u0441\u0435\u043a\u0443\u043d\u0434\u044b");
                    break;
                }
                case "muts": {
                    if (arguments.length < 2) break;
                    User shower = this.database.getUserById(arguments[1]);
                    if (shower == null) {
                        msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: \u0418\u0433\u0440\u043e\u043a \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d");
                        return;
                    }
                    String ipAddress = shower.getLastIP().split(":")[0];
                    List<String> usernames = this.database.getUsernamesWithSameIP(ipAddress);
                    msg.localLobby.send(Type.LOBBY_CHAT, "system", "Users with ip: " + ipAddress + ":");
                    for (String username : usernames) {
                        msg.localLobby.send(Type.LOBBY_CHAT, "system", username);
                    }
                    break;
                }
                case "getip": {
                    if (arguments.length < 2) break;
                    User shower = this.database.getUserById(arguments[1]);
                    if (shower == null) {
                        msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: \u0418\u0433\u0440\u043e\u043a \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d");
                        return;
                    }
                    String ip = shower.getAntiCheatData().ip;
                    if (ip == null) {
                        ip = shower.getLastIP();
                    }
                    msg.localLobby.send(Type.LOBBY_CHAT, "system", "IP user " + shower.getNickname() + " : " + ip);
                    break;
                }
                case "banip": {
                    if (arguments.length < 2) break;
                    User victim = this.database.getUserById(arguments[1]);
                    if (victim == null) {
                        msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: \u0418\u0433\u0440\u043e\u043a \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d");
                        return;
                    }
                    msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: \u0418\u0433\u0440\u043e\u043a " + victim.getNickname() + " \u0437\u0430\u0431\u0430\u043d\u0435\u043d \u043f\u043e IP: " + victim.getLastIP());
                    LobbyManager l = this.lobbyServices.getLobbyByUser(victim);
                    if (l == null) break;
                    NettyUsersHandler.block(l.getLocalUser().getLastIP());
                    l.kick();
                    break;
                }
                case "unbanip": {
                    if (arguments.length < 2) break;
                    User _victim = this.database.getUserById(arguments[1]);
                    if (_victim == null) {
                        msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: \u0418\u0433\u0440\u043e\u043a \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d");
                        return;
                    }
                    LobbyManager _l = this.lobbyServices.getLobbyByUser(_victim);
                    NettyUsersHandler.unblock(_l.getLocalUser().getLastIP());
                    break;
                }
                case "battleinfo": {
                    if (arguments.length < 2) break;
                    StringBuilder id = new StringBuilder();
                    for (int i = 1; i < arguments.length; ++i) {
                        id.append(arguments[i]).append(" ");
                    }
                    BattleInfo battle = BattlesList.getBattleInfoById(id.toString().trim().replace("#battle", ""));
                    if (battle == null) {
                        msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: \u0411\u0438\u0442\u0432\u0430 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u0430");
                        break;
                    }
                    if (battle.model == null) break;
                    msg.localLobby.send(Type.LOBBY_CHAT, "system", battle.dump());
                    break;
                }
                case "clean": {
                    if (arguments.length < 2) break;
                    this.cleanMessagesByUser(arguments[1]);
                    break;
                }
                case "cleant": {
                    if (arguments.length < 2) break;
                    this.cleanMessagesByText(StringUtils.concatMassive(arguments, 1));
                    break;
                }
                case "unban": {
                    if (arguments.length < 2) break;
                    User cu = this.database.getUserById(arguments[1]);
                    if (cu == null) {
                        msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: \u0418\u0433\u0440\u043e\u043a \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d!");
                        break;
                    }
                    Logger.debug(msg.user.getNickname() + " -> " + cu.getNickname() + " UNBAN chat.");
                    this.banServices.unbanChat(cu);
                    this.sendSystemMessageToAll(StringUtils.concatStrings("\u0422\u0430\u043d\u043a\u0438\u0441\u0442\u0443 ", cu.getNickname(), " \u0431\u044b\u043b \u0440\u0430\u0437\u0440\u0435\u0448\u0451\u043d \u0432\u044b\u0445\u043e\u0434 \u0432 \u044d\u0444\u0438\u0440"), false);
                    break;
                }
                case "w": {
                    if (arguments.length < 3) {
                        return;
                    }
                    User giver = this.database.getUserById(arguments[1]);
                    if (giver == null) {
                        msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: \u0418\u0433\u0440\u043e\u043a \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d!");
                        break;
                    }
                    String reason = StringUtils.concatMassive(arguments, 2);
                    this.sendSystemMessageToAll(StringUtils.concatStrings("\u0422\u0430\u043d\u043a\u0438\u0441\u0442 ", giver.getNickname(), " \u043f\u0440\u0435\u0434\u0443\u043f\u0440\u0435\u0436\u0434\u0435\u043d. \u041f\u0440\u0438\u0447\u0438\u043d\u0430: ", reason), false);
                    break;
                }
                case "blockgame": {
                    if (arguments.length < 3) {
                        return;
                    }
                    User victim_ = this.database.getUserById(arguments[1]);
                    int reasonId = 0;
                    try {
                        reasonId = Integer.parseInt(arguments[2]);
                    } catch (Exception ex) {
                        reasonId = 0;
                    }
                    if (victim_ == null) {
                        msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: \u0418\u0433\u0440\u043e\u043a \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d!");
                        break;
                    }
                    this.banServices.ban(BanType.GAME, BanTimeType.FOREVER, victim_, msg.user, BlockGameReason.getReasonById(reasonId).getReason());
                    LobbyManager lobby = this.lobbyServices.getLobbyByNick(victim_.getNickname());
                    if (lobby != null) {
                        lobby.kick();
                    }
                    Logger.debug(msg.user.getNickname() + " -> " + victim_.getNickname() + " BAN GAME Reason: " + BlockGameReason.getReasonById(reasonId).getReason());
                    this.sendSystemMessageToAll(StringUtils.concatStrings("\u0422\u0430\u043d\u043a\u0438\u0441\u0442 ", victim_.getNickname(), " \u0431\u044b\u043b \u0437\u0430\u0431\u043b\u043e\u043a\u0438\u0440\u043e\u0432\u0430\u043d \u0438 \u043a\u0438\u043a\u043d\u0443\u0442"), false);
                    break;
                }
                case "unblockgame": {
                    if (arguments.length < 2) {
                        return;
                    }
                    User av = this.database.getUserById(arguments[1]);
                    if (av == null) {
                        msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: \u0418\u0433\u0440\u043e\u043a \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d!");
                        break;
                    }
                    Logger.debug(msg.user.getNickname() + " -> " + av.getNickname() + " UNBAN game.");
                    this.banServices.unblock(av);
                    this.sendSystemMessageToAll(StringUtils.concatStrings("\u0422\u0430\u043d\u043a\u0438\u0441\u0442 ", av.getNickname(), " \u0431\u044b\u043b \u0440\u0430\u0437\u0431\u043b\u043e\u043a\u0438\u0440\u043e\u0432\u0430\u043d"), false);
                    break;
                }
                default: {
                    if (msg.message.startsWith("/ban")) break;
                    msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: \u041d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u0430\u044f \u043a\u043e\u043c\u0430\u043d\u0434\u0430!");
                }
            }
            if (msg.message.startsWith("/ban")) {
                BanTimeType time = BanChatCommads.getTimeType(arguments[0]);
                if (arguments.length < 3) {
                    return;
                }
                String reason = StringUtils.concatMassive(arguments, 2);
                if (time == null) {
                    msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: \u041a\u043e\u043c\u0430\u043d\u0434\u0430 \u0431\u0430\u043d\u0430 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u0430!");
                    return;
                }
                User _victim = this.database.getUserById(arguments[1]);
                if (_victim == null) {
                    msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: \u0418\u0433\u0440\u043e\u043a \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d!");
                    return;
                }
                Logger.debug(msg.user.getNickname() + " -> " + _victim.getNickname() + " BAN " + String.valueOf((Object)BanType.CHAT) + " for a " + BanTimeType.getConstantName(BanChatCommads.getTimeType(arguments[0])) + " Reason: " + reason);
                this.banServices.ban(BanType.CHAT, time, _victim, msg.user, reason);
                this.sendSystemMessageToAll(StringUtils.concatStrings("\u0422\u0430\u043d\u043a\u0438\u0441\u0442 ", _victim.getNickname(), " \u043b\u0438\u0448\u0435\u043d \u043f\u0440\u0430\u0432\u0430 \u0432\u044b\u0445\u043e\u0434\u0430 \u0432 \u044d\u0444\u0438\u0440 ", time.getNameType(), " \u041f\u0440\u0438\u0447\u0438\u043d\u0430: ", reason), false);
            }
        } else if (!msg.message.isEmpty()) {
            if (msg.message.length() >= 399) {
                return;
            }
            if (!this.stoped) {
                if (!msg.localLobby.getChatFloodController().detected(msg.message)) {
                    msg.message = this.getNormalMessage(msg.message.trim());
                    msg.localLobby.timer = System.currentTimeMillis();
                    if (this.chatMessages.size() >= 50) {
                        this.chatMessages.remove(0);
                    }
                    this.chatMessages.add(msg);
                    this.sendMessageToAll(msg);
                } else {
                    if (msg.user.getWarnings() >= 4) {
                        BanTimeType time = BanTimeType.FIVE_MINUTES;
                        String reason = "\u0424\u043b\u0443\u0434.";
                        this.banServices.ban(BanType.CHAT, time, msg.user, msg.user, reason);
                        this.sendSystemMessageToAll(StringUtils.concatStrings("\u0422\u0430\u043d\u043a\u0438\u0441\u0442 ", msg.user.getNickname(), " \u043b\u0438\u0448\u0435\u043d \u043f\u0440\u0430\u0432\u0430 \u0432\u044b\u0445\u043e\u0434\u0430 \u0432 \u044d\u0444\u0438\u0440 ", time.getNameType(), " \u041f\u0440\u0438\u0447\u0438\u043d\u0430: ", reason), false);
                        return;
                    }
                    this.sendSystemMessageToAll("\u0422\u0430\u043d\u043a\u0438\u0441\u0442 " + msg.user.getNickname() + "  \u043f\u0440\u0435\u0434\u0443\u043f\u0440\u0435\u0436\u0434\u0435\u043d. \u041f\u0440\u0438\u0447\u0438\u043d\u0430: \u0424\u043b\u0443\u0434.", false);
                    msg.user.addWarning();
                }
            }
        }
    }

    public static String filter(String text) {
        PreparedMessage msg = ChatModel.prepareMessage(text);
        Boolean[] mask = new Boolean[msg.text.length()];
        for (int i = 0; i < mask.length; ++i) {
            mask[i] = false;
        }
        String loweredText = msg.text.toLowerCase();
        for (String forbiddenWord : forbiddenWordParts) {
            int fwPosition = loweredText.indexOf(forbiddenWord);
            while (fwPosition != -1) {
                if (fwPosition != -1) {
                    for (int i = 0; i < forbiddenWord.length(); ++i) {
                        mask[i + fwPosition] = true;
                    }
                }
                fwPosition = loweredText.indexOf(forbiddenWord, fwPosition + forbiddenWord.length());
            }
        }
        StringBuilder result = new StringBuilder(text);
        for (int i = 0; i < mask.length; ++i) {
            if (!mask[i].booleanValue()) continue;
            result.setCharAt(msg.charsPositions[i], '*');
        }
        return result.toString();
    }

    private static PreparedMessage prepareMessage(String text) {
        return new PreparedMessage(text);
    }

    public void cleanMessagesByText(String text) {
        Predicate<ChatMessage> filter = p -> p.message.equals(text);
        this.chatMessages.removeIf(filter);
        this.lobbyServices.sendCommandToAllUsers(Type.LOBBY_CHAT, UserLocation.ALL, "clean_by_text", text);
    }

    public void cleanMessagesByUser(String nickname) {
        Predicate<ChatMessage> ifDelete = p -> !p.system && p.user != null && p.user.getNickname().equals(nickname);
        this.chatMessages.removeIf(ifDelete);
        this.lobbyServices.sendCommandToAllUsers(Type.LOBBY_CHAT, UserLocation.ALL, "clean_by", nickname);
    }

    public void clear() {
        this.lobbyServices.sendCommandToAllUsers(Type.LOBBY_CHAT, UserLocation.ALL, "clear_all");
        this.chatMessages.clear();
        this.sendSystemMessageToAll("\u0427\u0430\u0442 \u043e\u0447\u0438\u0449\u0435\u043d", false);
    }

    public void sendSystemMessageToAll(String[] ar, boolean yellow) {
        StringBuffer total = new StringBuffer();
        for (int i = 1; i < ar.length; ++i) {
            total.append(ar[i]).append(" ");
        }
        ChatMessage sys_msg = new ChatMessage(null, total.toString(), false, null, yellow, null);
        sys_msg.system = true;
        this.chatMessages.add(sys_msg);
        if (this.chatMessages.size() >= 50) {
            this.chatMessages.remove(0);
        }
        this.lobbyServices.sendCommandToAllUsers(Type.LOBBY_CHAT, UserLocation.ALL, "system", total.toString().trim(), yellow ? "yellow" : "green");
    }

    public void sendSystemMessageToAll(String msg, boolean yellow) {
        ChatMessage sys_msg = new ChatMessage(null, msg, false, null, yellow, null);
        sys_msg.system = true;
        this.chatMessages.add(sys_msg);
        if (this.chatMessages.size() >= 50) {
            this.chatMessages.remove(0);
        }
        this.lobbyServices.sendCommandToAllUsersBesides(Type.LOBBY_CHAT, UserLocation.BATTLE, "system", msg.trim(), yellow ? "yellow" : "green");
    }

    public void sendMessageToAll(ChatMessage msg) {
        this.lobbyServices.sendCommandToAllUsersBesides(Type.LOBBY_CHAT, UserLocation.BATTLE, JSONUtils.parseChatLobbyMessage(msg));
    }

    public String getNormalMessage(String src) {
        StringBuilder str = new StringBuilder();
        char[] mass = src.toCharArray();
        for (int i = 0; i < mass.length; ++i) {
            if (mass[i] == ' ') {
                if (mass[i] == mass[i + 1]) continue;
                str.append(" ");
                continue;
            }
            str.append(mass[i]);
        }
        return str.toString();
    }

    public int getInt(String src) {
        try {
            return Integer.parseInt(src);
        } catch (Exception ex) {
            return Integer.MAX_VALUE;
        }
    }

    public Collection<ChatMessage> getMessages() {
        return this.chatMessages;
    }

    public Date setBanTo(Date banTo) {
        return banTo;
    }

    public long setCurrDate(long currDate) {
        this.currDate = currDate;
        return currDate;
    }

    private static class PreparedMessage {
        String text;
        int[] charsPositions;

        public PreparedMessage(String text) {
            this.text = text;
            this.charsPositions = new int[text.length()];
            for (int i = 0; i < text.length(); ++i) {
                this.charsPositions[i] = i;
            }
        }
    }
}
