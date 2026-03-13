package gtanks.main;

import gtanks.Captcha;
import gtanks.battles.BotDamageHandler;
import gtanks.battles.maps.MapsLoader;
import gtanks.battles.tanks.loaders.HullsFactory;
import gtanks.battles.tanks.loaders.WeaponsFactory;
import gtanks.configurator.server.configuration.ConfigurationsLoader;
import gtanks.logger.Logger;
import gtanks.logger.remote.RemoteDatabaseLogger;
import gtanks.main.netty.NettyService;
import gtanks.rmi.server.RMIServer;
import gtanks.services.hibernate.HibernateService;
import gtanks.system.SystemBattlesHandler;
import gtanks.system.SystemConsoleHandler;
import gtanks.users.garage.GarageItemsLoader;
import gtanks.users.groups.UserGroupsLoader;
import gtanks.utils.ResourceUtils;
import gtanks.utils.RankUtils;
import gtanks.discord.JdaBot;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.Session;

import javax.security.auth.login.LoginException;

public class Main {

    public static void main(String[] args) {
        try {
            Logger.log("Game Server Starting");
            //String botURL = BotDamageHandler.URL;

            PropertyConfigurator.configure(ResourceUtils.config("log4j.properties"));
            ConfigurationsLoader.load("");
            initFactories();
            UserGroupsLoader.load(ResourceUtils.data("groups"));

            Logger.log("Connecting to DB...");
            try (Session session = HibernateService.getSessionFactory().getCurrentSession()) {
                session.beginTransaction();
                session.getTransaction().commit();
            }
            NettyService.inject().init();
            Captcha.loadCatches();
            SystemBattlesHandler.systemBattlesInit();
            RMIServer.run();
            //startDiscordBot("MTI0OTI4Mjc2ODc4NjAzMDY1Mw.GNhwjn.tGmHM8L0VZjvtZamOrSmWZ690hA0g2ZkTQqkaA");

        } catch (Exception e) {
            e.printStackTrace();
            RemoteDatabaseLogger.error(e);
        }
    }

    private static void initFactories() {
        try {
            GarageItemsLoader.loadFromConfig(
                    ResourceUtils.data("json/turrets.json"),
                    ResourceUtils.data("json/hulls.json"),
                    ResourceUtils.data("json/colormaps.json"),
                    ResourceUtils.data("json/specialTurrets.json"),
                    ResourceUtils.data("json/specialHulls.json"),
                    ResourceUtils.data("json/specialColormaps.json"),
                    ResourceUtils.data("json/inventory.json"),
                    ResourceUtils.data("json/effects.json")
            );
        } catch (Throwable t) {
            t.printStackTrace();
        }
        MapsLoader.initFactoryMaps();
        WeaponsFactory.init(ResourceUtils.data("config/equipment/weapons"));
        HullsFactory.init(ResourceUtils.data("config/equipment/hulls"));
        RankUtils.init();
        SystemConsoleHandler.getInstance().start();
    }

    private static void startDiscordBot(String token) {
        try {
            JdaBot.initialize(token);
        } catch (InterruptedException | LoginException e) {
            e.printStackTrace();
            RemoteDatabaseLogger.error(e);
        }
    }
}
