/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.system.dailybonus.crystalls;

import gtanks.lobby.LobbyManager;
import gtanks.services.TanksServices;
import gtanks.services.annotations.ServicesInject;

public class CrystallsBonusModel {
    @ServicesInject(target=TanksServices.class)
    private static final TanksServices tanksServices = TanksServices.getInstance();
    private static final int[] CRYSTALLS;

    public void applyBonus(LobbyManager lobby) {
        int bonus = this.getBonus(lobby.getLocalUser().getRang());
        tanksServices.addCrystall(lobby, bonus);
    }

    public int getBonus(int rangIndex) {
        return CRYSTALLS[rangIndex];
    }

    static {
        int[] arrn = new int[27];
        arrn[2] = 15;
        arrn[3] = 25;
        arrn[4] = 35;
        arrn[5] = 50;
        arrn[6] = 60;
        arrn[7] = 75;
        arrn[8] = 85;
        arrn[9] = 95;
        arrn[10] = 110;
        arrn[11] = 120;
        arrn[12] = 135;
        arrn[13] = 145;
        arrn[14] = 155;
        arrn[15] = 170;
        arrn[16] = 180;
        arrn[17] = 195;
        arrn[18] = 205;
        arrn[19] = 215;
        arrn[20] = 230;
        arrn[21] = 240;
        arrn[22] = 255;
        arrn[23] = 265;
        arrn[24] = 275;
        arrn[25] = 290;
        arrn[26] = 300;
        CRYSTALLS = arrn;
    }
}

