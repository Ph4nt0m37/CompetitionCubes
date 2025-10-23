package dev.pakn.competitioncubes;

public enum Rank {
    BRONZE(0), SILVER(200), GOLD(400), DIAMOND(600), EMERALD(800), CHAMPION(1000);

    private int minElo = 0;

    Rank(int minElo) {
        this.minElo = minElo;
    }

    public int getMinElo() {
        return minElo;
    }

    public static Rank getRankByElo(int elo) {
        if (elo<200) {
            return BRONZE;
        }else if (elo<400) {
            return SILVER;
        }else if (elo<600) {
            return GOLD;
        }else if (elo<800) {
            return DIAMOND;
        }else if (elo<1000) {
            return EMERALD;
        }else {
            return CHAMPION;
        }
    }
}
