/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.rmi.payments;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIPaymentCallback
extends Remote {
    public boolean paymentAccepted(long var1, String var3, int var4) throws RemoteException;
}

