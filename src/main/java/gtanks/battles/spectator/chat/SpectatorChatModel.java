/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.spectator.chat;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.bonuses.BonusType;
import gtanks.battles.chat.BattlefieldChatModel;
import gtanks.battles.spectator.SpectatorController;
import gtanks.battles.spectator.SpectatorModel;
import gtanks.commands.Type;
import gtanks.lobby.LobbyManager;
import gtanks.logger.Logger;
import gtanks.main.database.DatabaseManager;
import gtanks.main.database.impl.DatabaseManagerImpl;
import gtanks.main.params.OnlineStats;
import gtanks.services.BanServices;
import gtanks.services.LobbysServices;
import gtanks.services.annotations.ServicesInject;
import gtanks.services.ban.BanChatCommads;
import gtanks.services.ban.BanTimeType;
import gtanks.services.ban.BanType;
import gtanks.services.ban.block.BlockGameReason;
import gtanks.users.TypeUser;
import gtanks.users.User;
import gtanks.utils.StringUtils;
import java.util.List;

public class SpectatorChatModel {
    private static final String CHAT_SPECTATOR_COMAND = "spectator_message";
    private SpectatorModel spModel;
    private BattlefieldModel bfModel;
    private BattlefieldChatModel chatModel;
    @ServicesInject(target=DatabaseManagerImpl.class)
    private DatabaseManager database = DatabaseManagerImpl.instance();
    @ServicesInject(target=LobbysServices.class)
    private LobbysServices lobbyServices = LobbysServices.getInstance();
    @ServicesInject(target=BanServices.class)
    private BanServices banServices = BanServices.getInstance();

    public SpectatorChatModel(SpectatorModel spModel) {
        this.spModel = spModel;
        this.bfModel = spModel.getBattleModel();
        this.chatModel = this.bfModel.chatModel;
    }

    public void onMessage(String message, SpectatorController spectator) {
        if (spectator.getUser().getType() == TypeUser.SPECTATOR) {
            return;
        }
        if (message.startsWith("/")) {
            String[] arguments = message.replace('/', ' ').trim().split(" ");
            if (spectator.getUser().getUserGroup().isAvaliableChatCommand(arguments[0])) {
                return;
            }
            switch (arguments[0]) {
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
                    if (victim_ == null) break;
                    Logger.debug(spectator.getUser().getNickname() + " -> " + victim_.getNickname() + " BAN GAME Reason: " + BlockGameReason.getReasonById(reasonId).getReason());
                    this.banServices.ban(BanType.GAME, BanTimeType.FOREVER, victim_, spectator.getUser(), BlockGameReason.getReasonById(reasonId).getReason());
                    LobbyManager lobby = this.lobbyServices.getLobbyByNick(victim_.getNickname());
                    if (lobby != null) {
                        lobby.kick();
                    }
                    this.chatModel.sendSystemMessage(StringUtils.concatStrings("Танкист ", victim_.getNickname(), " был заблокирован и кикнут"));
                    break;
                }
                case "spects": {
                    this.chatModel.sendSystemMessage(this.bfModel.spectatorModel.getSpectators(), spectator);
                    break;
                }
                case "system": {
                    StringBuffer total = new StringBuffer();
                    for (int i = 1; i < arguments.length; ++i) {
                        total.append(arguments[i]).append(" ");
                    }
                    this.chatModel.sendSystemMessage(total.toString());
                    break;
                }
                case "muts": {
                    if (arguments.length < 2) break;
                    User shower = this.database.getUserById(arguments[1]);
                    if (shower == null) {
                        this.chatModel.sendSystemMessage("[SERVER]: Игрок не найден", spectator);
                        return;
                    }
                    String ipAddress = shower.getLastIP().split(":")[0];
                    List<String> usernames = this.database.getUsernamesWithSameIP(ipAddress);
                    this.chatModel.sendSystemMessage("Users with ip: " + ipAddress + ":", spectator);
                    for (String username : usernames) {
                        this.chatModel.sendSystemMessage(username, spectator);
                    }
                    break;
                }
                case "unban": {
                    User cu;
                    if (arguments.length < 2 || (cu = this.database.getUserById(arguments[1])) == null) break;
                    Logger.debug(spectator.getUser().getNickname() + " -> " + cu.getNickname() + " UNBAN chat.");
                    this.banServices.unbanChat(cu);
                    this.chatModel.sendSystemMessage("Танкисту " + cu.getNickname() + " был разрешён выход в эфир");
                    break;
                }
                case "unblockgame": {
                    if (arguments.length < 2) {
                        return;
                    }
                    User av = this.database.getUserById(arguments[1]);
                    if (av == null) break;
                    Logger.debug(spectator.getUser().getNickname() + " -> " + av.getNickname() + " UNBAN game.");
                    this.banServices.unblock(av);
                    this.chatModel.sendSystemMessage(av.getNickname() + " разблокирован");
                    break;
                }
                case "spawngold": {
                    for (int i = 0; i < Integer.parseInt(arguments[1]); ++i) {
                        this.spModel.getBattleModel().bonusesSpawnService.spawnBonus(BonusType.GOLD);
                    }
                    break;
                }
                case "spawncry": {
                    for (int i = 0; i < Integer.parseInt(arguments[1]); ++i) {
                        this.spModel.getBattleModel().bonusesSpawnService.spawnBonus(BonusType.CRYSTALL);
                    }
                    break;
                }
                case "addfund": {
                    int arg = Integer.parseInt(arguments[1]);
                    this.bfModel.tanksKillModel.addFund(arg);
                    break;
                }
                case "kick": {
                    LobbyManager _lobby;
                    User _userForKick = this.database.getUserById(arguments[1]);
                    if (_userForKick == null || (_lobby = this.lobbyServices.getLobbyByUser(_userForKick)) == null) break;
                    _lobby.kick();
                    this.chatModel.sendSystemMessage(_userForKick.getNickname() + " кикнут");
                    break;
                }
                case "online": {
                    this.chatModel.sendSystemMessage("Current online: " + OnlineStats.getOnline() + "\nMax online: " + OnlineStats.getMaxOnline() + "\nIn battles: " + OnlineStats.getInBattlesOnline(), spectator);
                    break;
                }
                case "finishbattle": {
                    this.bfModel.tanksKillModel.restartBattle(false);
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
                    this.chatModel.sendSystemMessage("IP user " + shower.getNickname() + " : " + ip, spectator);
                    break;
                }
                case "w": {
                    if (arguments.length < 3) {
                        return;
                    }
                    User giver = this.database.getUserById(arguments[1]);
                    if (giver == null) break;
                    String reason = StringUtils.concatMassive(arguments, 2);
                    this.chatModel.sendSystemMessage(StringUtils.concatStrings("Танкист ", giver.getNickname(), " предупрежден. Причина: ", reason));
                    break;
                }
            }
            if (message.startsWith("/ban")) {
                BanTimeType time = BanChatCommads.getTimeType(arguments[0]);
                if (arguments.length < 3) {
                    return;
                }
                String reason = StringUtils.concatMassive(arguments, 2);
                if (time == null) {
                    return;
                }
                User _victim = this.database.getUserById(arguments[1]);
                if (_victim == null) {
                    return;
                }
                Logger.debug(spectator.getUser().getNickname() + " -> " + _victim.getNickname() + " BAN " + String.valueOf((Object)BanType.CHAT) + " for a " + BanTimeType.getConstantName(BanChatCommads.getTimeType(arguments[0])) + " Reason: " + reason);
                this.banServices.ban(BanType.CHAT, time, _victim, spectator.getUser(), reason);
                this.chatModel.sendSystemMessage(StringUtils.concatStrings("Танкист ", _victim.getNickname(), " лишен права выхода в эфир ", time.getNameType(), " Причина: ", reason));
            }
        } else {
            Logger.debug("[SPECTATOR_CHAT_LOG]: User " + spectator.getUser().getNickname() + " said: \"" + message + "\" battle info: " + String.valueOf(this.bfModel.battleInfo));
            this.spModel.getBattleModel().sendToAllPlayers(Type.BATTLE, CHAT_SPECTATOR_COMAND, message);
        }
    }
}

