/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.battles.bonuses;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.bonuses.Bonus;
import gtanks.battles.bonuses.BonusRegion;
import gtanks.battles.bonuses.BonusType;
import gtanks.battles.tanks.math.Vector3;
import gtanks.commands.Type;
import gtanks.system.timers.SystemTimerScheduler;
import gtanks.utils.RandomUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BonusesSpawnService
implements Runnable {
    private static final int DISAPPEARING_TIME_DROP = 30;
    public BattlefieldModel battlefieldModel;
    private final Random random = new Random();
    private int inc = 0;
    private int prevFund = 0;
    private int crystallFund;
    private int goldFund;
    private int giftFund;
    private int nextGoldFund;
    private int nextGiftFund;

    public BonusesSpawnService(BattlefieldModel model) {
        this.battlefieldModel = model;
        this.nextGoldFund = (int)RandomUtils.getRandom(1.0f, 700.0f);
        this.nextGiftFund = (int)RandomUtils.getRandom(1.0f, 700.0f);
    }

    public void spawnRandomBonus() {
        boolean wasSpawned = this.random.nextBoolean();
        if (this.battlefieldModel.battleInfo.withoutBonuses) {
            return;
        }
        if (wasSpawned && this.battlefieldModel.players.size() > 0) {
            int id = this.random.nextInt(5);
            BonusType bonusType = null;
            switch (id) {
                case 0: {
                    bonusType = BonusType.NITRO;
                    break;
                }
                case 1: {
                    bonusType = BonusType.ARMOR;
                    break;
                }
                case 2: {
                    bonusType = BonusType.HEALTH;
                    break;
                }
                case 3: {
                    bonusType = BonusType.DAMAGE;
                    break;
                }
                case 4: {
                    bonusType = BonusType.NITRO;
                }
            }
            int count = this.random.nextInt(4);
            for (int i = 0; i < count; ++i) {
                this.spawnBonus(bonusType);
            }
        }
    }

    public void spawnBonus(BonusType type) {
        List<List> allRegions = Arrays.asList(this.battlefieldModel.battleInfo.map.goldsRegions, this.battlefieldModel.battleInfo.map.crystallsRegions, this.battlefieldModel.battleInfo.map.armorsRegions, this.battlefieldModel.battleInfo.map.damagesRegions, this.battlefieldModel.battleInfo.map.healthsRegions, this.battlefieldModel.battleInfo.map.nitrosRegions);
        switch (type) {
            case GOLD: {
                if (this.battlefieldModel.battleInfo.map.goldsRegions.size() <= 0) break;
                int index = this.random.nextInt(this.battlefieldModel.battleInfo.map.goldsRegions.size());
                BonusRegion region = this.battlefieldModel.battleInfo.map.goldsRegions.get(index);
                Bonus bonus = new Bonus(this.getRandomSpawnPostiton(region), BonusType.GOLD);
                this.battlefieldModel.spawnBonus(bonus, this.inc, 300);
                break;
            }
            case GIFT: {
                ArrayList<List> availableRegions = new ArrayList<List>(allRegions);
                availableRegions.remove(this.battlefieldModel.battleInfo.map.goldsRegions);
                availableRegions.remove(this.battlefieldModel.battleInfo.map.crystallsRegions);
                ArrayList possibleRegions = new ArrayList();
                for (List regions : availableRegions) {
                    possibleRegions.addAll(regions);
                }
                if (this.battlefieldModel.battleInfo.map.goldsRegions.size() <= 0) break;
                int index = this.random.nextInt(possibleRegions.size());
                BonusRegion region = (BonusRegion)possibleRegions.get(index);
                Bonus bonus = new Bonus(this.getRandomSpawnPostiton(region), BonusType.GIFT);
                this.battlefieldModel.spawnBonus(bonus, this.inc, 300);
                break;
            }
            case CRYSTALL: {
                if (this.battlefieldModel.battleInfo.map.crystallsRegions.size() <= 0) break;
                int index = this.random.nextInt(this.battlefieldModel.battleInfo.map.crystallsRegions.size());
                BonusRegion region = this.battlefieldModel.battleInfo.map.crystallsRegions.get(index);
                Bonus bonus = new Bonus(this.getRandomSpawnPostiton(region), BonusType.CRYSTALL);
                this.battlefieldModel.spawnBonus(bonus, this.inc, 300);
                break;
            }
            case ARMOR: {
                if (this.battlefieldModel.battleInfo.map.armorsRegions.size() <= 0) break;
                int index = this.random.nextInt(this.battlefieldModel.battleInfo.map.armorsRegions.size());
                BonusRegion region = this.battlefieldModel.battleInfo.map.armorsRegions.get(index);
                Bonus bonus = new Bonus(this.getRandomSpawnPostiton(region), BonusType.ARMOR);
                this.battlefieldModel.spawnBonus(bonus, this.inc, 30);
                break;
            }
            case DAMAGE: {
                if (this.battlefieldModel.battleInfo.map.damagesRegions.size() <= 0) break;
                int index = this.random.nextInt(this.battlefieldModel.battleInfo.map.damagesRegions.size());
                BonusRegion region = this.battlefieldModel.battleInfo.map.damagesRegions.get(index);
                Bonus bonus = new Bonus(this.getRandomSpawnPostiton(region), BonusType.DAMAGE);
                this.battlefieldModel.spawnBonus(bonus, this.inc, 30);
                break;
            }
            case HEALTH: {
                if (this.battlefieldModel.battleInfo.map.healthsRegions.size() <= 0) break;
                int index = this.random.nextInt(this.battlefieldModel.battleInfo.map.healthsRegions.size());
                BonusRegion region = this.battlefieldModel.battleInfo.map.healthsRegions.get(index);
                Bonus bonus = new Bonus(this.getRandomSpawnPostiton(region), BonusType.HEALTH);
                this.battlefieldModel.spawnBonus(bonus, this.inc, 30);
                break;
            }
            case NITRO: {
                if (this.battlefieldModel.battleInfo.map.nitrosRegions.size() <= 0) break;
                int index = this.random.nextInt(this.battlefieldModel.battleInfo.map.nitrosRegions.size());
                BonusRegion region = this.battlefieldModel.battleInfo.map.nitrosRegions.get(index);
                Bonus bonus = new Bonus(this.getRandomSpawnPostiton(region), BonusType.NITRO);
                this.battlefieldModel.spawnBonus(bonus, this.inc, 30);
            }
        }
        ++this.inc;
    }

    public void battleFinished() {
        this.prevFund = 0;
        this.crystallFund = 0;
        this.goldFund = 0;
        this.giftFund = 0;
        this.nextGoldFund = (int)RandomUtils.getRandom(1.0f, 700.0f);
        this.nextGiftFund = (int)RandomUtils.getRandom(1.0f, 700.0f);
    }

    private Vector3 getRandomSpawnPostiton(BonusRegion region) {
        Vector3 f = new Vector3(0.0f, 0.0f, 0.0f);
        Random rand = new Random();
        f.x = region.min.x + (region.max.x - region.min.x) * rand.nextFloat();
        f.y = region.min.y + (region.max.y - region.min.y) * rand.nextFloat();
        f.z = region.max.z;
        return f;
    }

    public void updatedFund() {
        int deff = (int)this.battlefieldModel.tanksKillModel.getBattleFund() - this.prevFund;
        this.goldFund += deff;
        this.giftFund += deff;
        this.crystallFund += deff;
        if (this.goldFund >= this.nextGoldFund / 2) {
            this.battlefieldModel.sendToAllPlayers(Type.BATTLE, "gold_spawn");
            SystemTimerScheduler.scheduleTask(() -> this.spawnBonus(BonusType.GOLD), (int)RandomUtils.getRandom(10000.0f, 23000.0f));
            this.nextGoldFund = (int)RandomUtils.getRandom(1.0f, 700.0f);
            this.goldFund = 0;
        }
        if (this.giftFund >= this.nextGiftFund / 2) {
            this.spawnBonus(BonusType.GIFT);
            this.nextGiftFund = (int)RandomUtils.getRandom(1.0f, 700.0f);
            this.giftFund = 0;
        }
        if (this.crystallFund >= 6) {
            for (int i = 0; i < (int)RandomUtils.getRandom(1.0f, 6.0f); ++i) {
                this.spawnBonus(BonusType.CRYSTALL);
            }
            this.crystallFund = 0;
        }
        this.prevFund = (int)this.battlefieldModel.tanksKillModel.getBattleFund();
    }

    @Override
    public void run() {
        if (this.battlefieldModel.battleInfo.map.crystallsRegions.size() <= 0 && this.battlefieldModel.battleInfo.map.goldsRegions.size() <= 0) {
            this.battlefieldModel = null;
        }
        while (this.battlefieldModel != null) {
            try {
                Thread.sleep(5000L);
                if (this.battlefieldModel == null || this.battlefieldModel.players == null) break;
                this.spawnRandomBonus();
            } catch (InterruptedException e) {
                System.out.println("Bonus error = " + e.getMessage());
            }
        }
    }
}

