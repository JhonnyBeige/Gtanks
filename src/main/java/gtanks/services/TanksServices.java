/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.services;

import gtanks.commands.Type;
import gtanks.lobby.LobbyManager;
import gtanks.logger.remote.RemoteDatabaseLogger;
import gtanks.main.database.DatabaseManager;
import gtanks.main.database.impl.DatabaseManagerImpl;
import gtanks.services.annotations.ServicesInject;
import gtanks.users.User;
import gtanks.utils.RankUtils;

public class TanksServices {
    private static final TanksServices instance = new TanksServices();
    @ServicesInject(target=DatabaseManagerImpl.class)
    private final DatabaseManager database = DatabaseManagerImpl.instance();

    public static TanksServices getInstance() {
        return instance;
    }

    public void addScore(LobbyManager lobby, int score) {
        if (lobby == null) {
            RemoteDatabaseLogger.error("TanksServices::addScore: lobby null!");
            return;
        }
        User user = lobby.getLocalUser();
        if (user == null) {
            RemoteDatabaseLogger.error("TanksServices::addScore: user null!");
            return;
        }
        user.addScore(score);
        boolean increase = user.getScore() >= user.getNextScore() && user.getRang() != 29;
        boolean fall = user.getScore() < RankUtils.getRankByIndex((int)user.getRang()).min;
        this.setBl(fall);
        if (increase || fall) {
            user.setRang(RankUtils.getNumberRank(RankUtils.getRankByScore(user.getScore())));
            user.setNextScore(user.getRang() == 26 ? RankUtils.getRankByIndex((int)user.getRang()).max : RankUtils.getRankByIndex((int)user.getRang()).max + 1);
            lobby.send(Type.LOBBY, "update_rang_progress", String.valueOf(10000));
            if (user.getRang() == 1) {
                lobby.send(Type.BATTLE, "show_nube_new_rank");
                lobby.addCrystall(5);
            }
            lobby.send(Type.LOBBY, "update_rang", String.valueOf(user.getRang() + 1), String.valueOf(user.getNextScore()));
        }
        int update = RankUtils.getUpdateNumber(user.getScore());
        lobby.send(Type.LOBBY, "update_rang_progress", String.valueOf(update));
        lobby.send(Type.LOBBY, "add_score", String.valueOf(user.getScore()));
        this.database.update(user);
    }

    public void addCrystall(LobbyManager lobby, int crystall) {
        if (lobby == null) {
            RemoteDatabaseLogger.error("TanksServices::addCrystall: lobby null!");
            return;
        }
        User user = lobby.getLocalUser();
        if (user == null) {
            RemoteDatabaseLogger.error("TanksServices::addCrystall: user null!");
            return;
        }
        user.addCrystall(crystall);
        lobby.send(Type.LOBBY, "add_crystall", String.valueOf(user.getCrystall()));
        this.database.update(user);
    }

    public void dummyAddCrystall(LobbyManager lobby, int crystall) {
        if (lobby == null) {
            RemoteDatabaseLogger.error("TanksServices::addCrystall: lobby null!");
            return;
        }
        User user = lobby.getLocalUser();
        if (user == null) {
            RemoteDatabaseLogger.error("TanksServices::addCrystall: user null!");
            return;
        }
        lobby.send(Type.LOBBY, "add_crystall", String.valueOf(user.getCrystall()));
    }

    public void updateRatingData(LobbyManager controller) {
        int kills = controller.getLocalUser().getKills();
        int deaths = controller.getLocalUser().getDeaths();
        double kd = deaths == 0 ? (double)kills : (double)kills / (double)deaths;
        controller.getLocalUser().setKd(kd);
        this.database.setPlace(controller.getLocalUser());
        int userPlace = controller.getLocalUser().getPlace();
        controller.send(Type.LOBBY, "update_rating_data", String.valueOf(controller.getLocalUser().getRating()), String.valueOf(userPlace));
        this.database.update(controller.getLocalUser());
    }

    public void setBl(boolean bl) {
    }
}

