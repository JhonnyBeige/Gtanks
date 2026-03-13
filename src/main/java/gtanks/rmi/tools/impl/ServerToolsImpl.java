/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.rmi.tools.impl;

import gtanks.logger.Logger;
import gtanks.rmi.tools.ServerTools;
import gtanks.services.annotations.ServicesInject;
import gtanks.system.restart.ServerRestartService;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ServerToolsImpl
extends UnicastRemoteObject
implements ServerTools {
    private static final long serialVersionUID = 1034275549315539686L;
    @ServicesInject(target=ServerRestartService.class)
    private ServerRestartService serverRestartService = ServerRestartService.inject();

    public ServerToolsImpl() throws RemoteException {
        Logger.log("RMI ServerTools service is runned!");
    }

    @Override
    public void restart() throws RemoteException {
        this.serverRestartService.restart();
    }
}

