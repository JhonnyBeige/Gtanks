/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.mines.activator;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.mines.ServerMine;
import gtanks.battles.mines.activator.MineActivator;
import gtanks.configurator.osgi.OSGi;
import gtanks.configurator.server.configuration.entitys.MineConfiguratorEntity;
import java.util.Timer;
import java.util.TimerTask;

public class MinesActivatorService {
    private static final int ACTIVATION_TIME = ((MineConfiguratorEntity)OSGi.getModelByInterface(MineConfiguratorEntity.class)).getActivationTimeMsec();
    private static final MinesActivatorService instance = new MinesActivatorService();
    private static final Timer TIMER = new Timer("MinesActivatorService Timer");

    public static MinesActivatorService getInstance() {
        return instance;
    }

    public void activate(BattlefieldModel model, ServerMine mine) {
        MineActivator activator = new MineActivator(model, mine);
        TIMER.schedule((TimerTask)activator, ACTIVATION_TIME);
        activator.putMine();
    }
}

