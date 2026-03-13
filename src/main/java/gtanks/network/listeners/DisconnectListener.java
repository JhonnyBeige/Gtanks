/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.network.listeners;

import gtanks.network.listeners.IDisconnectListener;
import java.util.ArrayList;

public class DisconnectListener {
    private final ArrayList<IDisconnectListener> listeners = new ArrayList();

    public void addListener(IDisconnectListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(IDisconnectListener listener) {
        this.listeners.remove(listener);
    }
}

