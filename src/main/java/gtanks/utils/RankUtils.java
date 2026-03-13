/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.utils;

import gtanks.utils.RankEntity;

public class RankUtils {
    private static RankEntity[] ranks;
    private static boolean var4;

    public static void init() {
        ranks = new RankEntity[27];
        RankUtils.ranks[0] = new RankEntity(0, 99, "\u041d\u043e\u0432\u043e\u0431\u0440\u0430\u043d\u0435\u0446");
        RankUtils.ranks[1] = new RankEntity(100, 499, "\u0420\u044f\u0434\u043e\u0432\u043e\u0439");
        RankUtils.ranks[2] = new RankEntity(500, 1499, "\u0415\u0444\u0440\u0435\u0439\u0442\u043e\u0440");
        RankUtils.ranks[3] = new RankEntity(1500, 3699, "\u041a\u0430\u043f\u0440\u0430\u043b");
        RankUtils.ranks[4] = new RankEntity(3700, 7099, "\u041c\u0430\u0441\u0442\u0435\u0440-\u043a\u0430\u043f\u0440\u0430\u043b");
        RankUtils.ranks[5] = new RankEntity(7100, 12299, "\u0421\u0435\u0440\u0436\u0430\u043d\u0442");
        RankUtils.ranks[6] = new RankEntity(12300, 19999, "\u0428\u0442\u0430\u0431-\u0441\u0435\u0440\u0436\u0430\u043d\u0442");
        RankUtils.ranks[7] = new RankEntity(20000, 28999, "\u041c\u0430\u0441\u0442\u0435\u0440-\u0441\u0435\u0440\u0436\u0430\u043d\u0442");
        RankUtils.ranks[8] = new RankEntity(29000, 40999, "\u041f\u0435\u0440\u0432\u044b\u0439 \u0441\u0435\u0440\u0436\u0430\u043d\u0442");
        RankUtils.ranks[9] = new RankEntity(41000, 56999, "\u0421\u0435\u0440\u0436\u0430\u043d\u0442-\u043c\u0430\u0439\u043e\u0440");
        RankUtils.ranks[10] = new RankEntity(57000, 75999, "\u0423\u043e\u0440\u044d\u0435\u043d\u0442-\u043e\u0444\u0438\u0446\u0435\u0440 1");
        RankUtils.ranks[11] = new RankEntity(76000, 97999, "\u0423\u043e\u0440\u044d\u0435\u043d\u0442-\u043e\u0444\u0438\u0446\u0435\u0440 2");
        RankUtils.ranks[12] = new RankEntity(98000, 124999, "\u0423\u043e\u0440\u044d\u0435\u043d\u0442-\u043e\u0444\u0438\u0446\u0435\u0440 3");
        RankUtils.ranks[13] = new RankEntity(125000, 155999, "\u0423\u043e\u0440\u044d\u0435\u043d\u0442-\u043e\u0444\u0438\u0446\u0435\u0440 4");
        RankUtils.ranks[14] = new RankEntity(156000, 191999, "\u0423\u043e\u0440\u044d\u0435\u043d\u0442-\u043e\u0444\u0438\u0446\u0435\u0440 5");
        RankUtils.ranks[15] = new RankEntity(192000, 232999, "\u041c\u043b\u0430\u0434\u0448\u044b\u0439 \u043b\u0435\u0439\u0442\u0435\u043d\u0430\u043d\u0442");
        RankUtils.ranks[16] = new RankEntity(233000, 279999, "\u041b\u0435\u0439\u0442\u0435\u043d\u0430\u043d\u0442");
        RankUtils.ranks[17] = new RankEntity(280000, 331999, "\u0421\u0442\u0430\u0440\u0448\u0438\u0439 \u043b\u0435\u0439\u0442\u0435\u043d\u0430\u043d\u0442");
        RankUtils.ranks[18] = new RankEntity(332000, 389999, "\u041a\u0430\u043f\u0438\u0442\u0430\u043d");
        RankUtils.ranks[19] = new RankEntity(390000, 454999, "\u041c\u0430\u0439\u043e\u0440");
        RankUtils.ranks[20] = new RankEntity(455000, 526999, "\u041f\u043e\u0434\u043f\u043e\u043b\u043a\u043e\u0432\u043d\u0438\u043a");
        RankUtils.ranks[21] = new RankEntity(527000, 605999, "\u041f\u043e\u043b\u043a\u043e\u0432\u043d\u0438\u043a");
        RankUtils.ranks[22] = new RankEntity(606000, 691999, "\u0411\u0440\u0438\u0433\u0430\u0434\u0438\u0440");
        RankUtils.ranks[23] = new RankEntity(692000, 786999, "\u0413\u0435\u043d\u0435\u0440\u0430\u043b-\u043c\u0430\u0439\u043e\u0440");
        RankUtils.ranks[24] = new RankEntity(787000, 888999, "\u0413\u0435\u043d\u0435\u0440\u0430\u043b-\u043b\u0435\u0439\u043d\u0435\u0442\u0430\u043d\u0442");
        RankUtils.ranks[25] = new RankEntity(889000, 999999, "\u0413\u0435\u043d\u0435\u0440\u0430\u043b");
        RankUtils.ranks[26] = new RankEntity(1000000, 0, "\u041c\u0430\u0440\u0448\u0430\u043b");
    }

    public static int getUpdateNumber(int score) {
        int result;
        RankEntity temp = RankUtils.getRankByScore(score);
        int rang = RankUtils.setRangId(RankUtils.getNumberRank(temp));
        RankUtils.setVar4(false);
        try {
            result = (int)((double)(score - RankUtils.ranks[rang - 1].max) * 1.0 / (double)(temp.max - RankUtils.ranks[rang - 1].max) * 10000.0);
        } catch (Exception var6) {
            result = (int)((double)(score - 0) * 1.0 / (double)(temp.max - 0) * 10000.0);
        }
        if (score > RankUtils.ranks[RankUtils.ranks.length - 1].min - 1) {
            result = 10000;
        } else if (score < 0) {
            result = 0;
        }
        return result;
    }

    public static int getNumberRank(RankEntity rank) {
        for (int i = 0; i < ranks.length; ++i) {
            if (ranks[i] != rank) continue;
            return i;
        }
        return -1;
    }

    public static RankEntity getRankByScore(int score) {
        RankEntity temp = ranks[0];
        if (score >= RankUtils.ranks[26].max) {
            temp = ranks[26];
        }
        RankEntity[] var5 = ranks;
        int var4 = ranks.length;
        for (int var3 = 0; var3 < var4; ++var3) {
            RankEntity rank = var5[var3];
            if (score < rank.min || score > rank.max) continue;
            temp = rank;
        }
        return temp;
    }

    public static RankEntity getRankByIndex(int index) {
        return ranks[index];
    }

    public static int setRangId(int rangId) {
        return rangId;
    }

    public static boolean isVar4() {
        return var4;
    }

    public static void setVar4(boolean var4) {
        RankUtils.var4 = var4;
    }
}

