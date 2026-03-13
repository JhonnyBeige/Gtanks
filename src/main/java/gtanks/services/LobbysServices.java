/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.services;

import gtanks.collections.FastHashMap;
import gtanks.commands.Type;
import gtanks.lobby.LobbyManager;
import gtanks.users.User;
import gtanks.users.locations.UserLocation;

public class LobbysServices {
    private static final LobbysServices instance = new LobbysServices();
    public FastHashMap<String, LobbyManager> lobbys = new FastHashMap();

    public static LobbysServices getInstance() {
        return instance;
    }

    private LobbysServices() {
    }

    public void addLobby(LobbyManager lobby) {
        this.lobbys.put(lobby.getLocalUser().getNickname(), lobby);
    }

    public void removeLobby(LobbyManager lobby) {
        this.lobbys.remove(lobby.getLocalUser(), lobby);
    }

    public LobbyManager getLobbyByUser(User user) {
        return this.lobbys.get(user.getNickname());
    }

    public LobbyManager getLobbyByNick(String nick) {
        LobbyManager lobbyManager = null;
        for (LobbyManager lobby : this.lobbys) {
            if (!lobby.getLocalUser().getNickname().equals(nick)) continue;
            lobbyManager = lobby;
            break;
        }
        return lobbyManager;
    }

    public void sendCommandToAllUsers(Type type, UserLocation onlyFor, String ... args) {
        try {
            for (LobbyManager lobby : this.lobbys.values()) {
                if (lobby == null || onlyFor != UserLocation.ALL && lobby.getLocalUser().getUserLocation() != onlyFor) continue;
                lobby.send(type, args);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendCommandToAllUsersBesides(Type type, UserLocation besides, String ... args) {
        try {
            for (LobbyManager lobby : this.lobbys.values()) {
                if (lobby == null || lobby.getLocalUser().getUserLocation() == besides) continue;
                lobby.send(type, args);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

