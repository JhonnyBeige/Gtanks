/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.commands;

import gtanks.commands.Command;
import gtanks.commands.Type;
import org.apache.commons.lang3.ArrayUtils;

public class Commands {
    public static final String SPLITTER_ARGS = ";";

    public static Command decrypt(String crypt) {
        String[] temp = crypt.split(SPLITTER_ARGS);
        Type type = switch (temp[0]) {
            case "auth" -> Type.AUTH;
            case "registration" -> Type.REGISTRATON;
            case "chat" -> Type.CHAT;
            case "lobby" -> Type.LOBBY;
            case "garage" -> Type.GARAGE;
            case "battle" -> Type.BATTLE;
            case "ping" -> Type.PING;
            case "lobby_chat" -> Type.LOBBY_CHAT;
            case "system" -> Type.SYSTEM;
            default -> Type.UNKNOWN;
        };
        String[] args = ArrayUtils.removeElement(temp, temp[0]);
        return new Command(type, args);
    }
}

