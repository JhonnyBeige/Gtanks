/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.chat;

public class BattleChatMessage {
    public String nickname;
    public int rank;
    public String message;
    public String teamType;
    public boolean team;
    public boolean system;

    public BattleChatMessage(String nickname, int rank, String message, String teamType, boolean team, boolean system) {
        this.nickname = nickname;
        this.rank = rank;
        this.message = message;
        this.teamType = teamType;
        this.team = team;
        this.system = system;
    }
}

