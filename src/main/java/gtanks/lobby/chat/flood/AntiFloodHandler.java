/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.lobby.chat.flood;

import gtanks.lobby.chat.flood.ConstraintHandler;

public class AntiFloodHandler
extends ConstraintHandler {
    private String lastMessage;
    private int messagesRepeated;
    private long lastMessageTime;

    @Override
    public boolean detected(String msg) {
        if (System.currentTimeMillis() - this.lastMessageTime < 500L) {
            return true;
        }
        if (msg.equals(this.lastMessage)) {
            ++this.messagesRepeated;
            if (this.messagesRepeated >= 5) {
                return true;
            }
        } else {
            this.messagesRepeated = 0;
        }
        this.lastMessageTime = System.currentTimeMillis();
        this.lastMessage = msg;
        return false;
    }
}

