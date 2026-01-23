package dev.pakn.competitioncubes;

public class BadgeController {
    public static final int OWNER_BADGE = 0;
    public static final int ADMIN_BADGE = 1;
    public static final int MODERATOR_BADGE = 2;
    public static final int JR_MODERATOR_BADGE = 3;
    public static final int EARLY_TESTER_BADGE = 4;
    public static final int TESTER_BADGE = 5;
    public static final int CREATOR_BADGE = 6;
    public static final int DIAMOND_BADGE = 7;
    public static final int GOLD_BADGE = 8;
    public static final int SILVER_BADGE = 9;
    public static final int BRONZE_BADGE = 10;
    public static final int SNS_FALL_25_BADGE = 11;

    public static void calculateAndGrantBadges(Match match) {
        for (int userId:match.getUsers()) {
            User user = DBController.getUserByIDList(userId);
            int rank = DBController.getUserEloRank(userId,match.getEvent());
            if (rank>0) {
                if (rank<=100 && !user.getBadges().contains(BRONZE_BADGE)) {
                    user.getBadges().add(BRONZE_BADGE);
                }
                if (rank<=50 && !user.getBadges().contains(SILVER_BADGE)) {
                    user.getBadges().add(SILVER_BADGE);
                }
                if (rank<=10 && !user.getBadges().contains(GOLD_BADGE)) {
                    user.getBadges().add(GOLD_BADGE);
                }
                if (rank==1 && !user.getBadges().contains(DIAMOND_BADGE)) {
                    user.getBadges().add(DIAMOND_BADGE);
                }
            }
            user.sortBadges();
        }
    }
}
