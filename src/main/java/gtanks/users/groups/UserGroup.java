/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.users.groups;

import java.util.Collections;
import java.util.List;

public class UserGroup {
    private String groupName;
    private final List<String> avaliableChatCommands;

    public UserGroup(List<String> avaliableChatCommands) {
        this.avaliableChatCommands = Collections.unmodifiableList(avaliableChatCommands);
    }

    public boolean isAvaliableChatCommand(String command) {
        return !this.avaliableChatCommands.contains(command);
    }

    public String getGroupName() {
        return this.groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("User group: ").append(this.getGroupName()).append(". ").append("Avaliable chat commands:\n");
        for (String command : this.avaliableChatCommands) {
            sb.append("        ---- /").append(command).append('\n');
        }
        return sb.toString();
    }
}

