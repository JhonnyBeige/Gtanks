/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.tanks.statistic.prizes;

import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.tanks.statistic.PlayerStatistic;
import gtanks.logger.Logger;
import gtanks.services.TanksServices;
import gtanks.services.annotations.ServicesInject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BattlePrizeCalculate {
    @ServicesInject(target=TanksServices.class)
    private static TanksServices tankServices = TanksServices.getInstance();

    public static void calc(List<BattlefieldPlayerController> users, int fund) {
        if (users == null || users.size() == 0) {
            return;
        }
        BattlefieldPlayerController _first = Collections.max(users, new Comparator<BattlefieldPlayerController>(){

            @Override
            public int compare(BattlefieldPlayerController o1, BattlefieldPlayerController o2) {
                return (int)(o1.statistic.getScore() - o2.statistic.getScore());
            }
        });
        PlayerStatistic first = _first.statistic;
        double sumSquare = 0.0;
        int countFirstUsers = 0;
        for (BattlefieldPlayerController battlefieldPlayerController : users) {
            long value = battlefieldPlayerController.statistic.getScore();
            if (value != first.getScore()) {
                sumSquare += (double)(value * value);
                continue;
            }
            ++countFirstUsers;
        }
        sumSquare += (double)(first.getScore() * first.getScore() * (long)countFirstUsers * (long)countFirstUsers);
        int allSum = 0;
        for (BattlefieldPlayerController user : users) {
            if (user.statistic.getScore() == first.getScore()) continue;
            int prize = (int)((double)((long)fund * user.statistic.getScore() * user.statistic.getScore()) / sumSquare);
            if (prize < 0) {
                prize = Math.abs(prize);
            }
            allSum += prize;
            user.statistic.setPrize(prize);
            tankServices.addCrystall(user.parentLobby, prize);
            user.getUser().setWealth(user.getUser().getWealth() + prize);
            tankServices.updateRatingData(user.parentLobby);
        }
        int n = (fund - allSum) / countFirstUsers;
        for (BattlefieldPlayerController user : users) {
            PlayerStatistic _user = user.statistic;
            if (_user.getScore() != first.getScore() || user == _first) continue;
            _user.setPrize(n);
            tankServices.addCrystall(user.parentLobby, n);
            allSum += n;
        }
        first.setPrize(first.getPrize() + (fund - allSum));
        tankServices.addCrystall(_first.parentLobby, first.getPrize());
        _first.getUser().setWealth(_first.getUser().getWealth() + first.getPrize());
        tankServices.updateRatingData(_first.parentLobby);
        StringBuilder logMessage = new StringBuilder("[BATTLE_RESULT_LOG]: Battle has been finished. Details:\n");
        logMessage.append("[BATTLE_RESULT_LOG]: Battle name: ").append(_first.battle.battleInfo.name).append("\n");
        logMessage.append("[BATTLE_RESULT_LOG]: Total fund: ").append(fund).append("\n");
        logMessage.append("[BATTLE_RESULT_LOG]: Team type: ").append(_first.playerTeamType).append("\n");
        for (BattlefieldPlayerController user : users) {
            PlayerStatistic userStat = user.statistic;
            int userPrize = userStat.getPrize();
            logMessage.append("===================================\n");
            logMessage.append("[BATTLE_RESULT_LOG]: Nickname: ").append(user.getUser().getNickname()).append("\n");
            logMessage.append("[BATTLE_RESULT_LOG]: score: ").append(userStat.getScore()).append("\n");
            logMessage.append("[BATTLE_RESULT_LOG]: prize: ").append(userPrize).append("\n");
        }
        Logger.debug(logMessage.toString());
    }

    public static void calculateForTeam(ArrayList<BattlefieldPlayerController> redUsers, ArrayList<BattlefieldPlayerController> blueUsers, int scoreRed, int scoreBlue, double looseKoeff, int fund) {
        ArrayList<BattlefieldPlayerController> usersLoose;
        ArrayList<BattlefieldPlayerController> usersWin;
        int prizeLoose = 0;
        int prizeWin = 0;
        if (redUsers.isEmpty()) {
            Logger.log("calculateForTeam redUsers Empty fund = " + fund);
            usersWin = blueUsers;
            usersLoose = redUsers;
            prizeWin = fund;
        } else if (blueUsers.isEmpty()) {
            Logger.log("calculateForTeam  lueUsers Empty fund = " + fund);
            usersWin = redUsers;
            usersLoose = blueUsers;
            prizeWin = fund;
        } else if (scoreRed != scoreBlue) {
            int scoreWin = Math.max(scoreRed, scoreBlue);
            int scoreLoose = Math.min(scoreRed, scoreBlue);
            prizeLoose = (int)((double)fund * looseKoeff * (double)scoreLoose / (double)scoreWin);
            prizeWin = fund - prizeLoose;
            usersWin = scoreRed > scoreBlue ? redUsers : blueUsers;
            usersLoose = scoreRed > scoreBlue ? blueUsers : redUsers;
        } else {
            prizeLoose = (int)Math.ceil((float)fund / 2.0f);
            prizeWin = (int)Math.ceil((float)fund / 2.0f);
            usersWin = redUsers;
            usersLoose = blueUsers;
        }
        BattlePrizeCalculate.calc(usersWin, prizeWin);
        BattlePrizeCalculate.calc(usersLoose, prizeLoose);
    }
}

