package dev.pakn.competitioncubes;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class WaitlistResult {
    private WaitlistCode waitlistCode;
    private int banExpirationYears;
    private int banExpirationMonths;
    private int banExpirationDays;
    private int banExpirationHours;
    private int banExpirationMins;
    private int banExpirationSecs;
    private String expirationDate;

    public WaitlistResult(WaitlistCode code) {
        this.waitlistCode = code;
    }

    public WaitlistResult(WaitlistCode code, long banExpirationEpoch) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyy HH:mm");
        LocalDateTime expirationDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(banExpirationEpoch), ZoneId.systemDefault());
        expirationDate = expirationDateTime.format(formatter);
        this.waitlistCode = code;

        /*For ban time
        LocalDateTime now = LocalDateTime.now();
        this.banExpirationYears = (int) ChronoUnit.YEARS.between(now, expirationDateTime);
        this.banExpirationMonths = (int) ChronoUnit.MONTHS.between(now, expirationDateTime)%12;
        this.banExpirationDays = (int) ChronoUnit.DAYS.between(now, expirationDateTime)%31;
        this.banExpirationHours = (int) ChronoUnit.HOURS.between(now, expirationDateTime)%24;
        this.banExpirationMins = (int) ChronoUnit.MINUTES.between(now, expirationDateTime)%60;
        this.banExpirationSecs = (int) ChronoUnit.SECONDS.between(now, expirationDateTime)%60;
        */
    }

    public WaitlistCode getWaitlistCode() {
        return waitlistCode;
    }

    public int getBanExpirationYears() {
        return banExpirationYears;
    }

    public int getBanExpirationMonths() {
        return banExpirationMonths;
    }

    public int getBanExpirationDays() {
        return banExpirationDays;
    }

    public int getBanExpirationHours() {
        return banExpirationHours;
    }

    public int getBanExpirationMins() {
        return banExpirationMins;
    }

    public int getBanExpirationSecs() {
        return banExpirationSecs;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    @Override
    public String toString() {
        return "WaitlistResult [waitlistCode=" + waitlistCode + ", banExpirationYears=" + banExpirationYears
                + ", banExpirationMonths=" + banExpirationMonths + ", banExpirationDays=" + banExpirationDays
                + ", banExpirationHours=" + banExpirationHours + ", banExpirationMins=" + banExpirationMins
                + ", banExpirationSecs=" + banExpirationSecs + "]";
    }
}
