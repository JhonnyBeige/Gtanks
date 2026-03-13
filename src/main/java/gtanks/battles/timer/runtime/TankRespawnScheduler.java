/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.timer.runtime;

import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.managers.SpawnManager;
import gtanks.battles.tanks.math.Vector3;
import gtanks.commands.Type;
import gtanks.json.JSONUtils;
import gtanks.logger.remote.RemoteDatabaseLogger;
import gtanks.utils.StringUtils;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class TankRespawnScheduler {
    private static final Timer TIMER = new Timer("TankRespawnScheduler timer");
    private static final HashMap<BattlefieldPlayerController, PrepareToSpawnTask> tasks = new HashMap();
    private static boolean disposed;

    public static void startRespawn(BattlefieldPlayerController player, boolean onlySpawn) {
        if (disposed) {
            return;
        }
        try {
            if (player == null) {
                return;
            }
            if (player.battle == null) {
                return;
            }
            PrepareToSpawnTask task = new PrepareToSpawnTask();
            task.player = player;
            task.onlySpawn = onlySpawn;
            tasks.put(player, task);
            TIMER.schedule((TimerTask)task, onlySpawn ? 1L : 3000L);
        } catch (Exception ex) {
            ex.printStackTrace();
            RemoteDatabaseLogger.error(ex);
        }
    }

    public static void dispose() {
        disposed = true;
    }

    public static void cancelRespawn(BattlefieldPlayerController player) {
        try {
            PrepareToSpawnTask task = tasks.get(player);
            if (task == null) {
                return;
            }
            if (task.spawnTask == null) {
                task.cancel();
            } else {
                task.spawnTask.cancel();
            }
            tasks.remove(player);
        } catch (Exception ex) {
            ex.printStackTrace();
            RemoteDatabaseLogger.error(ex);
        }
    }

    static class PrepareToSpawnTask
    extends TimerTask {
        public SpawnTask spawnTask;
        public BattlefieldPlayerController player;
        public Vector3 preparedPosition;
        public boolean onlySpawn;

        PrepareToSpawnTask() {
        }

        @Override
        public void run() {
            try {
                if (this.player == null) {
                    return;
                }
                if (this.player.tank == null) {
                    return;
                }
                if (this.player.battle == null) {
                    return;
                }
                this.preparedPosition = SpawnManager.getSpawnState(this.player.battle.battleInfo.map, this.player.playerTeamType);
                if (this.onlySpawn) {
                    this.player.send(Type.BATTLE, "prepare_to_spawn", StringUtils.concatStrings(this.player.tank.id, ";", String.valueOf(this.preparedPosition.x), "@", String.valueOf(this.preparedPosition.y), "@", String.valueOf(this.preparedPosition.z), "@", String.valueOf(this.preparedPosition.rot)));
                } else {
                    if (this.player.battle == null) {
                        return;
                    }
                    this.player.tank.position = this.preparedPosition;
                    this.player.send(Type.BATTLE, "prepare_to_spawn", StringUtils.concatStrings(this.player.tank.id, ";", String.valueOf(this.preparedPosition.x), "@", String.valueOf(this.preparedPosition.y), "@", String.valueOf(this.preparedPosition.z), "@", String.valueOf(this.preparedPosition.rot)));
                }
                this.spawnTask = new SpawnTask();
                this.spawnTask.preparedSpawnTask = this;
                TIMER.schedule((TimerTask)this.spawnTask, 5000L);
            } catch (Exception ex) {
                ex.printStackTrace();
                RemoteDatabaseLogger.error(ex);
            }
        }
    }

    static class SpawnTask
    extends TimerTask {
        PrepareToSpawnTask preparedSpawnTask;

        SpawnTask() {
        }

        @Override
        public void run() {
            try {
                BattlefieldPlayerController player = this.preparedSpawnTask.player;
                if (player == null) {
                    return;
                }
                if (player.tank == null) {
                    return;
                }
                if (player.battle == null) {
                    return;
                }
                player.battle.tanksKillModel.changeHealth(player.tank, 10000);
                player.battle.sendToAllPlayers(Type.BATTLE, "spawn", JSONUtils.parseSpawnCommand(player, this.preparedSpawnTask.preparedPosition));
                player.tank.state = "newcome";
                tasks.remove(player);
            } catch (Exception ex) {
                ex.printStackTrace();
                RemoteDatabaseLogger.error(ex);
            }
        }
    }
}

