package gtanks.battles.tanks.weapons.flamethrower.effects;

public class TemperatureCalc {
    public static double getTemperature(double currState) {
        double temperature = currState;
        if (temperature < 0.6) {
            temperature = 0.6;
        }
        return temperature;
    }
}


