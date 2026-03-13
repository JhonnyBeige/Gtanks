/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.commands;

import gtanks.commands.Type;
import gtanks.utils.StringUtils;

public class Command {
    public Type type;
    public String[] args;

    public Command(Type type, String[] args) {
        this.type = type;
        this.args = args;
    }

    public String toString() {
        String argsString = StringUtils.concatStrings(this.args);
        return this.type.toString() + " " + argsString;
    }
}

