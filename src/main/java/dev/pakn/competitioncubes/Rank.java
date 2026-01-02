package dev.pakn.competitioncubes;

public enum Rank {
    BRONZE1(0), BRONZE2(100), BRONZE3(200), SILVER1(300), SILVER2(400), SILVER3(500), GOLD1(600), GOLD2(700), GOLD3(800), DIAMOND1(900), DIAMOND2(1000), DIAMOND3(1100), EMERALD1(1200), EMERALD2(1300), EMERALD3(1400), CHAMPION(1500);

    private int minElo = 0;

    Rank(int minElo) {
        this.minElo = minElo;
    }

    public int getMinElo() {
        return minElo;
    }

    public static Rank getRankByElo(int elo) {
        if (elo<BRONZE2.getMinElo()) {
            return BRONZE1;
        }else if (elo<BRONZE3.getMinElo()) {
            return BRONZE2;
        }else if (elo<SILVER1.getMinElo()) {
            return BRONZE3;
        }else if (elo<SILVER2.getMinElo()) {
            return SILVER1;
        }else if (elo<SILVER3.getMinElo()) {
            return SILVER2;
        }else if (elo<GOLD1.getMinElo()) {
            return SILVER3;
        }else if (elo<GOLD2.getMinElo()) {
            return GOLD1;
        }else if (elo<GOLD3.getMinElo()) {
            return GOLD2;
        }else if (elo<DIAMOND1.getMinElo()) {
            return GOLD3;
        }else if (elo<DIAMOND2.getMinElo()) {
            return DIAMOND1;
        }else if (elo<DIAMOND3.getMinElo()) {
            return DIAMOND2;
        }else if (elo<EMERALD1.getMinElo()) {
            return DIAMOND3;
        }else if (elo<EMERALD2.getMinElo()) {
            return EMERALD1;
        }else if (elo<EMERALD3.getMinElo()) {
            return EMERALD2;
        }else if (elo<CHAMPION.getMinElo()) {
            return EMERALD3;
        }else {
            return CHAMPION;
        }
    }
}
