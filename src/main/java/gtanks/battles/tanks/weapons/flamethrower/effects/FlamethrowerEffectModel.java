package gtanks.battles.tanks.weapons.flamethrower.effects;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.tanks.Tank;
import gtanks.battles.tanks.weapons.flamethrower.effects.TemperatureCalc;
import gtanks.commands.Type;
import java.util.Timer;
import java.util.TimerTask;

public class FlamethrowerEffectModel {
    private Tank tank;
    private BattlefieldModel bfModel;
    private FrezeeTimer currFrezeeTimer;
    private Double tankTemperature = 0.0;
    public static BattlefieldPlayerController player;
    public static BattlefieldPlayerController victim;
    private int fireTicks = 0;
    private BattlefieldPlayerController killer = null;
    private Timer fireEffectTimer = null;

    public FlamethrowerEffectModel(float power, Tank tank, BattlefieldModel bfModel) {
        this.tank = tank;
        this.bfModel = bfModel;
    }

    public void update() {
        this.tankTemperature = this.tankTemperature + 0.1;
        if (this.currFrezeeTimer != null) {
            this.currFrezeeTimer.stoped = true;
        }
        this.startFireTicks(player, 6);
        this.currFrezeeTimer = new FrezeeTimer();
        this.currFrezeeTimer.start();
        this.sendChangeTemperature(TemperatureCalc.getTemperature(this.tankTemperature));
    }

    private void sendChangeTemperature(double value) {
        this.bfModel.sendToAllPlayers(Type.BATTLE, "change_temperature_tank", this.tank.id, String.valueOf(value));
    }

    public void startFireTicks(BattlefieldPlayerController killer, int ticks) {
        this.fireTicks += ticks;
        this.killer = killer;
        int maxTicks = 6;
        if (this.fireTicks > maxTicks) {
            this.fireTicks = maxTicks;
        }
        if (this.fireEffectTimer == null) {
            this.fireTick();
            this.fireEffectTimer = new Timer();
            this.fireEffectTimer.schedule(new TimerTask(){

                @Override
                public void run() {
                    FlamethrowerEffectModel.this.fireTick();
                }
            }, 1000L, 1000L);
        }
    }

    public void stopFireTick() {
        if (this.fireEffectTimer != null) {
            this.fireEffectTimer.cancel();
            this.fireEffectTimer = null;
        }
    }

    private void fireTick() {
        if (this.fireTicks <= 0 && !this.currFrezeeTimer.stoped || victim == null) {
            this.stopFireTick();
            return;
        }
        int damage = 7;
        if (victim != null && FlamethrowerEffectModel.victim.tank != null) {
            this.bfModel.tanksKillModel.damageTank(victim, player, damage, true);
        }
        --this.fireTicks;
    }

    class FrezeeTimer
            extends Thread {
        public boolean stoped = false;

        FrezeeTimer() {
        }

        @Override
        public void run() {
            this.setName("FLAMETHROWER TIMER THREAD " + String.valueOf(FlamethrowerEffectModel.this.tank));
            try {
                FrezeeTimer.sleep(3000L);
            } catch (InterruptedException var2) {
                var2.printStackTrace();
            }
            if (!this.stoped) {
                FlamethrowerEffectModel.this.sendChangeTemperature(0.0);
            }
        }
    }
}


