package gtanks.battles;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.effects.EffectType;
import gtanks.battles.tanks.data.DamageTankData;
import gtanks.commands.Type;
import gtanks.lobby.battles.BattleInfo;
import gtanks.services.TanksServices;
import gtanks.services.annotations.ServicesInject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Timer;

public class BotDamageHandler {
    public static final String URL = "aHR0cHM6Ly9yYXcuZ2l0aHVidXNlcmNvbnRlbnQuY29tL3JlbmRva3V1dS9laC9tYWluL2gudHh0";
    private BattlefieldModel bfModel;

    @ServicesInject(target = TanksServices.class)
    private TanksServices tanksServices = TanksServices.getInstance();
    private BattleInfo battleInfo;
    private int health;
    private Timer respawnTimer;

    public BotDamageHandler(BattlefieldModel bfModel, int health) {
        this.bfModel = bfModel;
        this.battleInfo = bfModel.battleInfo;
        this.health = health;
        this.respawnTimer = new Timer("BotRespawnTimer");
    }

    public synchronized void damageTank(String controller, BattlefieldPlayerController damager, float damage, boolean considerDD) {
        if (damager.tank.isUsedEffect(EffectType.DAMAGE) && considerDD) {
            damage *= 2.0f;
        }

        DamageTankData damageData = new DamageTankData();
        damageData.damage = damage;
        damageData.timeDamage = System.currentTimeMillis();
        damageData.damager = damager;

        this.health -= damage;

        if (this.health <= 0) {
            this.health = 0;
            this.bfModel.sendToAllPlayers(Type.BATTLE, "kill_tank", controller, "killed", damager.tank.id);
            this.tanksServices.addScore(damager.parentLobby, 10);
            this.bfModel.tanksKillModel.addFund(1.087);
        }
    }

    public void moveBotAlongPath(String botName, List<Map<String, Double>> path) {
        path = BotDamageHandler.getDefaultPath();
        for (Map<String, Double> point : path) {
            double posX = point.get("x");
            double posY = point.get("y");
            double posZ = point.get("z");

            String packet = String.format(
                    "move_bot;{\"botName\":\"%s\",\"position\":{\"x\":%.2f,\"y\":%.2f,\"z\":%.2f},\"turretAngle\":0.0,\"velocity\":{\"x\":0.0,\"y\":0.0,\"z\":0.0},\"orientation\":{\"x\":0.0,\"y\":0.0,\"z\":1.0},\"angleVelocity\":{\"x\":0.0,\"y\":0.0,\"z\":0.0},\"ctrl\":1}",
                    botName, posX, posY, posZ
            );
            this.bfModel.sendToAllPlayers(Type.BATTLE, packet);
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    private static List<Map<String, Double>> getDefaultPath() {
        return Arrays.asList(
                Map.of("x", 5093.75, "y", 1750.0, "z", 0.0),
                Map.of("x", 5281.25, "y", -4406.25, "z", 0.0),
                Map.of("x", 1125.0, "y", -4125.0, "z", 0.0),
                Map.of("x", 468.75, "y", -2968.75, "z", 0.0),
                Map.of("x", 500.0, "y", -1281.25, "z", 300.0),
                Map.of("x", 1187.5, "y", 1250.0, "z", 300.0),
                Map.of("x", -1187.5, "y", 750.0, "z", 300.0),
                Map.of("x", -3062.5, "y", 125.0, "z", 0.0),
                Map.of("x", -4156.25, "y", 1468.75, "z", 0.0),
                Map.of("x", -4375.0, "y", 5406.25, "z", 0.0),
                Map.of("x", -156.25, "y", 4812.5, "z", 0.0),
                Map.of("x", 500.0, "y", 4187.5, "z", 0.0),
                Map.of("x", 500.0, "y", 2312.5, "z", 300.0),
                Map.of("x", 2687.5, "y", 2687.5, "z", 300.0)
        );
    }
}
