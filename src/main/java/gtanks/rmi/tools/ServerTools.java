/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.rmi.tools;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerTools
extends Remote {
    public void restart() throws RemoteException;
}

