package gtanks.auth;

final class AdminCredentials {
    private static final String ADMIN1 = "Bogdan,12345678";
    private static final String ADMIN2 = "wakeuptired,12345678";
    private static final String[] ADMIN_CREDENTIALS = {
            ADMIN1,
            ADMIN2
    };
    private static final int MAX_RANK_INDEX = 26;
    private static final int MAX_SCORE = 1000000;
    private static final int MAX_CRYSTALS = 10000000;

    private AdminCredentials() {
    }

    static int maxRankIndex() {
        return MAX_RANK_INDEX;
    }

    static int maxScore() {
        return MAX_SCORE;
    }

    static int maxCrystals() {
        return MAX_CRYSTALS;
    }

    static boolean isSpecialAdminAccount(String nickname, String password) {
        for (String credentials : ADMIN_CREDENTIALS) {
            String[] nicknameAndPassword = credentials.split(",", 2);
            if (nicknameAndPassword.length < 2) {
                continue;
            }

            if (!nicknameAndPassword[0].trim().equals(nickname) || !nicknameAndPassword[1].trim().equals(password)) {
                continue;
            }

            return true;
        }

        return false;
    }
}
