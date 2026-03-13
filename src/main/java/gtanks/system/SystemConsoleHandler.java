/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.system;

import gtanks.battles.tanks.loaders.HullsFactory;
import gtanks.battles.tanks.loaders.WeaponsFactory;
import gtanks.services.PromocodeService;
import gtanks.utils.ResourceUtils;
import gtanks.logger.Logger;
import gtanks.logger.Type;
import gtanks.main.params.OnlineStats;
import gtanks.services.annotations.ServicesInject;
import gtanks.system.restart.ServerRestartService;
import java.util.Scanner;

public class SystemConsoleHandler
extends Thread {
    private static final SystemConsoleHandler instance = new SystemConsoleHandler();
    @ServicesInject(target=ServerRestartService.class)
    private static ServerRestartService serverRestartService = ServerRestartService.inject();
    @ServicesInject(target= PromocodeService.class)
    private final PromocodeService promocodeService = PromocodeService.instance();

    public static SystemConsoleHandler getInstance() {
        return instance;
    }

    private SystemConsoleHandler() {
        this.setName("SystemConsoleHandler thread");
    }

    private void onCommand(String input) {
        String[] spaceSplit = input.replace("/", "").split(" ");
        switch (spaceSplit[0]) {
            case "rf": {
                Logger.log(Type.WARNING, "Attention! The factories of weapons and hulls is reloaded!");
                WeaponsFactory.init(ResourceUtils.data("config/equipment/weapons"));
                HullsFactory.init(ResourceUtils.data("config/equipment/hulls"));
                this.promocodeService.loadPromoCodes();
                break;
            }
            case "help": {
                System.out.println(this.getHelpString());
                break;
            }
            case "online": {
                System.out.println(this.getOnlineInfoString());
                break;
            }
            case "restart": {
                serverRestartService.restart();
            }
        }
    }

    private String getOnlineInfoString() {
        return "\n Total online: " + OnlineStats.getOnline() + "\n Max online: " + OnlineStats.getMaxOnline() + "\n";
    }

    private String getHelpString() {
        return "rf - reload item's factories.\nonline - print current online.";
    }

    @Override
    public void run() {
        Throwable t = null;
        try {
            Scanner scn = new Scanner(System.in);
            try {
                String input = "";
                while (true) {
                    input = scn.nextLine();
                    this.onCommand(input);
                }
            } catch (Throwable throwable) {
                if (scn != null) {
                    scn.close();
                }
                throw throwable;
            }
        } catch (Throwable throwable) {
            if (t == null) {
                Throwable t2;
                t = t2 = null;
            } else {
                Throwable t2 = null;
                if (t != t2) {
                    t.addSuppressed(t2);
                }
            }
            throw throwable;
        }
    }
}

