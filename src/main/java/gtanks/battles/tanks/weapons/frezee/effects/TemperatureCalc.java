/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.tanks.weapons.frezee.effects;

import gtanks.battles.tanks.Tank;

public class TemperatureCalc {
    private static final double MIN_TEMPERATURE = -2.0;

    public static double getTemperature(Tank currState, float speed, float turnSpeed, float turretRotationSpeed) {
        double temperature_speed = (speed - currState.speed) * (speed / 30.0f);
        double temperature_turn = (turnSpeed - currState.turnSpeed) * (turnSpeed / 10.0f);
        double temperature_turret = (turretRotationSpeed - currState.turretRotationSpeed) * (turretRotationSpeed / 10.0f);
        double temperature = -(temperature_speed + temperature_turn + temperature_turret);
        if (temperature < -2.0) {
            temperature = -2.0;
        }
        if (temperature > 0.0) {
            temperature = 0.0;
        }
        return temperature;
    }
}

