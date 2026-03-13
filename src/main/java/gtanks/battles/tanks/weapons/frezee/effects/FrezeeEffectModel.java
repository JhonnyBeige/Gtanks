/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.tanks.weapons.frezee.effects;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.tanks.Tank;
import gtanks.battles.tanks.weapons.frezee.effects.TemperatureCalc;
import gtanks.commands.Type;
import gtanks.json.JSONUtils;

public class FrezeeEffectModel {
    public float speed;
    public float turnSpeed;
    public float turretRotationSpeed;
    private float power;
    private Tank tank;
    private BattlefieldModel bfModel;
    private FrezeeTimer currFrezeeTimer;

    public FrezeeEffectModel(float power, Tank tank, BattlefieldModel bfModel) {
        this.power = power;
        this.tank = tank;
        this.bfModel = bfModel;
    }

    public void setStartSpecFromTank() {
        this.speed = this.tank.speed;
        this.turnSpeed = this.tank.turnSpeed;
        this.turretRotationSpeed = this.tank.turretRotationSpeed;
    }

    public void update() {
        this.tank.speed -= this.power * this.speed / 100.0f * this.power;
        this.tank.turnSpeed -= this.power * this.turnSpeed / 100.0f * this.power;
        this.tank.turretRotationSpeed -= this.power * this.turretRotationSpeed / 100.0f * this.power;
        if (this.tank.speed < 0.4f) {
            this.tank.speed = 0.4f;
        }
        if (this.tank.turnSpeed < 0.4f) {
            this.tank.turnSpeed = 0.4f;
        }
        if (this.tank.turretRotationSpeed < 0.4f) {
            this.tank.turretRotationSpeed = 0.4f;
        }
        if (this.currFrezeeTimer != null) {
            this.currFrezeeTimer.stoped = true;
        }
        this.currFrezeeTimer = new FrezeeTimer();
        this.currFrezeeTimer.start();
        this.sendSpecData();
        this.sendChangeTemperature(TemperatureCalc.getTemperature(this.tank, this.speed, this.turnSpeed, this.turretRotationSpeed));
    }

    private void sendSpecData() {
        this.bfModel.sendToAllPlayers(Type.BATTLE, "change_spec_tank", this.tank.id, JSONUtils.parseTankSpec(this.tank, false));
    }

    private void sendChangeTemperature(double value) {
        this.bfModel.sendToAllPlayers(Type.BATTLE, "change_temperature_tank", this.tank.id, String.valueOf(value));
    }

    public void removeFrezeeEffect() {
        this.tank.speed = this.speed;
        this.tank.turnSpeed = this.turnSpeed;
        this.tank.turretRotationSpeed = this.turretRotationSpeed;
        this.sendSpecData();
        this.sendChangeTemperature(0.0);
    }

    class FrezeeTimer
    extends Thread {
        public boolean stoped = false;

        FrezeeTimer() {
        }

        @Override
        public void run() {
            this.setName("FREZEE TIMER THREAD " + String.valueOf(FrezeeEffectModel.this.tank));
            try {
                FrezeeTimer.sleep(3000L);
            } catch (InterruptedException var2) {
                var2.printStackTrace();
            }
            if (!this.stoped) {
                FrezeeEffectModel.this.tank.speed = FrezeeEffectModel.this.speed;
                FrezeeEffectModel.this.tank.turnSpeed = FrezeeEffectModel.this.turnSpeed;
                FrezeeEffectModel.this.tank.turretRotationSpeed = FrezeeEffectModel.this.turretRotationSpeed;
                FrezeeEffectModel.this.sendSpecData();
                FrezeeEffectModel.this.sendChangeTemperature(0.0);
            }
        }
    }
}

