/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.ctf.flags;

import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.ctf.FlagReturnTimer;
import gtanks.battles.ctf.flags.FlagState;
import gtanks.battles.tanks.math.Vector3;

public class FlagServer {
    public String flagTeamType;
    public BattlefieldPlayerController owner;
    public Vector3 position;
    public Vector3 basePosition;
    public FlagState state;
    public FlagReturnTimer returnTimer;
}

