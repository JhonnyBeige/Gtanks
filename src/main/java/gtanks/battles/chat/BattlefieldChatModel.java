/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.chat;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.bonuses.BonusType;
import gtanks.battles.spectator.SpectatorController;
import gtanks.commands.Type;
import gtanks.json.JSONUtils;
import gtanks.lobby.LobbyManager;
import gtanks.lobby.chat.ChatModel;
import gtanks.logger.Logger;
import gtanks.main.database.DatabaseManager;
import gtanks.main.database.impl.DatabaseManagerImpl;
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
import gtanks.users.TypeUser;
import gtanks.users.User;
import gtanks.users.karma.Karma;
import gtanks.utils.StringUtils;

import java.util.Date;
import java.util.List;

public class BattlefieldChatModel {
    private BattlefieldModel bfModel;
    @ServicesInject(target=TanksServices.class)
    private TanksServices tanksServices = TanksServices.getInstance();
    @ServicesInject(target=DatabaseManager.class)
    private DatabaseManager database = DatabaseManagerImpl.instance();
    @ServicesInject(target=LobbysServices.class)
    private LobbysServices lobbyServices = LobbysServices.getInstance();
    @ServicesInject(target=BanServices.class)
    private BanServices banServices = BanServices.getInstance();
    private Date banTo;

    public BattlefieldChatModel(BattlefieldModel bfModel) {
        this.bfModel = bfModel;
    }

    public void onMessage(BattlefieldPlayerController player, String message, boolean team) {
        if ((message = message.trim()).isEmpty()) {
            return;
        }
        if (message.contains("end~")) {
            return;
        }
        Logger.log("[BATTLE_CHAT_LOG_OUT]: Nickname " + player.getUser().getNickname() + " said: \"" + message + "\" (rank: " + player.getUser().getRang() + " team type: " + player.playerTeamType + ")");
        message = ChatModel.filter(message);
        Karma karma = this.database.getKarmaByUser(player.getUser());
        if (karma.isChatBanned()) {
            long currDate = System.currentTimeMillis();
            long delta = currDate - this.setBanTo(karma.getChatBannedBefore()).getTime();
            if (delta <= 0L) {
                player.parentLobby.send(Type.LOBBY_CHAT, "system", StringUtils.concatStrings("Вы отключены от чата. Вы вернётесь в ЭФИР через ", DateFormater.formatTimeToUnban(delta), ". Причина: " + karma.getReasonChatBan()));
                return;
            }
            this.banServices.unbanChat(player.getUser());
        }
        if (!this.bfModel.battleInfo.team) {
            team = false;
        }
        if (message.startsWith("/")) {
            if (player.getUser().getType() == TypeUser.DEFAULT) {
                return;
            }
            String[] arguments = message.replace('/', ' ').trim().split(" ");
            if (player.getUser().getUserGroup().isAvaliableChatCommand(arguments[0])) {
                return;
            }
            switch (arguments[0]) {
                case "system": {
                    StringBuffer total = new StringBuffer();
                    for (int i = 1; i < arguments.length; ++i) {
                        total.append(arguments[i]).append(" ");
                    }
                    this.sendSystemMessage(total.toString());
                    break;
                }
                case "addcry": {
                    this.tanksServices.addCrystall(player.parentLobby, this.getInt(arguments[1]));
                    break;
                }
                case "addscore": {
                    int score = this.getInt(arguments[1]);
                    if (player.parentLobby.getLocalUser().getScore() + score < 0) {
                        this.sendSystemMessage("[SERVER]: Ваше количество очков опыта не должно быть отрицательное!", player);
                        break;
                    }
                    this.tanksServices.addScore(player.parentLobby, score);
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
                        this.sendSystemMessage("[SERVER]: Игрок не найден!", player);
                        break;
                    }
                    Logger.debug(player.getUser().getNickname() + " -> " + victim_.getNickname() + " BAN GAME Reason: " + BlockGameReason.getReasonById(reasonId).getReason());
                    this.banServices.ban(BanType.GAME, BanTimeType.FOREVER, victim_, player.getUser(), BlockGameReason.getReasonById(reasonId).getReason());
                    LobbyManager lobby = this.lobbyServices.getLobbyByNick(victim_.getNickname());
                    if (lobby != null) {
                        lobby.kick();
                    }
                    this.sendSystemMessage(StringUtils.concatStrings("Танкист ", victim_.getNickname(), " был заблокирован и кикнут"));
                    break;
                }
                case "unban": {
                    if (arguments.length < 2) break;
                    User cu = this.database.getUserById(arguments[1]);
                    if (cu == null) {
                        this.sendSystemMessage("[SERVER]: Игрок не найден!", player);
                        break;
                    }
                    Logger.debug(player.getUser().getNickname() + " -> " + cu.getNickname() + " UNBAN chat.");
                    this.banServices.unbanChat(cu);
                    this.sendSystemMessage("Танкисту " + cu.getNickname() + " был разрешён выход в эфир");
                    break;
                }
                case "unblockgame": {
                    if (arguments.length < 2) {
                        return;
                    }
                    User av = this.database.getUserById(arguments[1]);
                    if (av == null) {
                        this.sendSystemMessage("[SERVER]: Игрок не найден!", player);
                        break;
                    }
                    Logger.debug(player.getUser().getNickname() + " -> " + av.getNickname() + " UNBAN game.");
                    this.banServices.unblock(av);
                    this.sendSystemMessage(av.getNickname() + " разблокирован");
                    break;
                }
                case "spawngold": {
                    Logger.debug("User " + player.getUser().getNickname() + " dropped gold box in count " + arguments[1]);
                    for (int i = 0; i < Integer.parseInt(arguments[1]); ++i) {
                        this.bfModel.bonusesSpawnService.spawnBonus(BonusType.GOLD);
                    }
                    break;
                }
                case "finishbattle": {
                    this.bfModel.tanksKillModel.restartBattle(false);
                    break;
                }
                case "addfund": {
                    int arg = this.getInt(arguments[1]);
                    Logger.debug("User " + player.getUser().getNickname() + " added fund in count " + arguments[1]);
                    this.bfModel.tanksKillModel.addFund(arg);
                    break;
                }
                case "spawncry": {
                    Logger.debug("User " + player.getUser().getNickname() + " dropped crystal box in count " + arguments[1]);
                    for (int i = 0; i < Integer.parseInt(arguments[1]); ++i) {
                        this.bfModel.bonusesSpawnService.spawnBonus(BonusType.CRYSTALL);
                    }
                    break;
                }
                case "spects": {
                    this.sendSystemMessage(this.bfModel.spectatorModel.getSpectators(), player);
                    break;
                }
                case "kick": {
                    User _userForKick = this.database.getUserById(arguments[1]);
                    if (_userForKick == null) {
                        this.sendSystemMessage("[SERVER]: Игрок не найден", player);
                        break;
                    }
                    LobbyManager _lobby = this.lobbyServices.getLobbyByUser(_userForKick);
                    if (_lobby == null) break;
                    _lobby.kick();
                    this.sendSystemMessage(_userForKick.getNickname() + " кикнут");
                    break;
                }
                case "muts": {
                    if (arguments.length < 2) break;
                    User shower = this.database.getUserById(arguments[1]);
                    if (shower == null) {
                        this.sendSystemMessage("[SERVER]: Игрок не найден", player);
                        return;
                    }
                    String ipAddress = shower.getLastIP().split(":")[0];
                    List<String> usernames = this.database.getUsernamesWithSameIP(ipAddress);
                    this.sendSystemMessage("Users with ip: " + ipAddress + ":", player);
                    for (String username : usernames) {
                        this.sendSystemMessage(username, player);
                    }
                    break;
                }
                case "online": {
                    this.sendSystemMessage("Current online: " + OnlineStats.getOnline() + "\nMax online: " + OnlineStats.getMaxOnline() + "\nIn battles: " + OnlineStats.getInBattlesOnline(), player);
                    break;
                }
                case "w": {
                    if (arguments.length < 3) {
                        return;
                    }
                    User giver = this.database.getUserById(arguments[1]);
                    if (giver == null) {
                        this.sendSystemMessage("[SERVER]: Игрок не найден!", player);
                        break;
                    }
                    String reason = StringUtils.concatMassive(arguments, 2);
                    this.sendSystemMessage(StringUtils.concatStrings("Танкист ", giver.getNickname(), " предупрежден. Причина: ", reason));
                    break;
                }
                case "getip": {
                    if (arguments.length < 2) break;
                    User shower = this.database.getUserById(arguments[1]);
                    if (shower == null) {
                        return;
                    }
                    String ip = shower.getAntiCheatData().ip;
                    if (ip == null) {
                        ip = shower.getLastIP();
                    }
                    this.sendSystemMessage("IP user " + shower.getNickname() + " : " + ip, player);
                    break;
                }
                default: {
                    if (message.startsWith("/ban")) break;
                    this.sendSystemMessage("[SERVER]: Неизвестная команда!", player);
                }
            }
            if (message.startsWith("/ban")) {
                BanTimeType time = BanChatCommads.getTimeType(arguments[0]);
                if (arguments.length < 3) {
                    return;
                }
                String reason = StringUtils.concatMassive(arguments, 2);
                if (time == null) {
                    this.sendSystemMessage("[SERVER]: Команда бана не найдена!", player);
                    return;
                }
                User _victim = this.database.getUserById(arguments[1]);
                if (_victim == null) {
                    this.sendSystemMessage("[SERVER]: Игрок не найден!", player);
                    return;
                }
                Logger.debug(player.getUser().getNickname() + " -> " + _victim.getNickname() + " BAN " + String.valueOf((Object)BanType.CHAT) + " for a " + BanTimeType.getConstantName(BanChatCommads.getTimeType(arguments[0])) + " Reason: " + reason);
                this.banServices.ban(BanType.CHAT, time, _victim, player.getUser(), reason);
                this.sendSystemMessage(StringUtils.concatStrings("Танкист ", _victim.getNickname(), " лишен права выхода в эфир ", time.getNameType(), " Причина: ", reason));
            }
        } else {
            if (message.length() >= 399) {
                return;
            }
            if (!player.parentLobby.getChatFloodController().detected(message)) {
                player.parentLobby.timer = System.currentTimeMillis();
                this.sendMessage(new BattleChatMessage(player.getUser().getNickname(), player.getUser().getRang(), message, player.playerTeamType, team, false));
            } else {
                if (player.getUser().getWarnings() >= 5) {
                    BanTimeType time = BanTimeType.FIVE_MINUTES;
                    String reason = "Флуд.";
                    this.banServices.ban(BanType.CHAT, time, player.getUser(), player.getUser(), reason);
                    this.sendSystemMessage(StringUtils.concatStrings("Танкист ", player.getUser().getNickname(), " лишен права выхода в эфир ", time.getNameType(), " Причина: ", reason));
                    return;
                }
                this.sendSystemMessage("Танкист " + player.getUser().getNickname() + "  предупрежден. Причина: Флуд.");
                player.getUser().addWarning();
            }
        }
    }

    public void sendSystemMessage(String message) {
        if (message == null) {
            message = " ";
        }
        this.sendMessage(new BattleChatMessage(null, 0, message, "NONE", false, true));
    }

    public void sendSystemMessage(String message, BattlefieldPlayerController player) {
        if (message == null) {
            message = " ";
        }
        this.sendMessage(new BattleChatMessage(null, 0, message, "NONE", false, true), player);
    }

    public void sendSystemMessage(String message, SpectatorController player) {
        if (message == null) {
            message = " ";
        }
        this.sendMessage(new BattleChatMessage(null, 0, message, "NONE", false, true), player);
    }

    private void sendMessage(BattleChatMessage msg) {
        if (msg.team && msg.teamType.equals("RED")) {
            this.bfModel.sendToRedTeamPlayers(Type.BATTLE, "chat", JSONUtils.parseBattleChatMessage(msg));
        }
        if (msg.team) {
            if (msg.teamType.equals("BLUE")) {
                this.bfModel.sendToBlueTeamPlayers(Type.BATTLE, "chat", JSONUtils.parseBattleChatMessage(msg));
            }
        } else {
            this.bfModel.sendToAllPlayers(Type.BATTLE, "chat", JSONUtils.parseBattleChatMessage(msg));
        }
    }

    private void sendMessage(BattleChatMessage msg, BattlefieldPlayerController controller) {
        controller.send(Type.BATTLE, "chat", JSONUtils.parseBattleChatMessage(msg));
    }

    private void sendMessage(BattleChatMessage msg, SpectatorController controller) {
        controller.sendCommand(Type.BATTLE, "chat", JSONUtils.parseBattleChatMessage(msg));
    }

    public int getInt(String src) {
        try {
            return Integer.parseInt(src);
        } catch (Exception ex) {
            return Integer.MAX_VALUE;
        }
    }

    public Date setBanTo(Date banTo) {
        this.banTo = banTo;
        return banTo;
    }
}

