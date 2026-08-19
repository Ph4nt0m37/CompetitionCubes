# Competition Cubes
### What is Competition Cubes?
**[Competition Cubes](https://compcube.pakn.dev)** is a real-time 1v1 speedcubing website where users compete against each other for ELO and to increase their global ranks.

**Competition Cubes** features:
- 1v1 User Competitions
- User Profiles with ranks and badges
- WCA ranking anticheat
- Global Leaderboard

### How does Competition Cubes prevent cheating?
**Competition Cubes** has a robust anticheat system that uses official WCA results as comparison with in-match times. If a user gets a time that is deemed to fast by the anticheat, the solve will either be DNF'd or sent to a moderator for approval depending on how fast the solve is.

This system is obviously not foolproof. If it has been a few years since a person's last WCA competition, their anticheat times may be slower than their actual times. Furthermore, people who have not competed in WCA competitions will also not be able to compete. Both of these issues are being looked into as we try to find solutions that balance inclusivity and solve integrity.

# Setting Up Development Environment
### Requirements
- Java 17
- PostgreSQL
- Maven

### Database Setup
The Competition Cubes program **will not run** unless you have the database setup. To setup the database, create a Postgres database then import the `comp_cube_test_db.sql` SQL dump. This dump has **no** user data so it will be empty.

### Program Execution
Once you have met all the requirements and have the database set up, you can run Competition Cubes with `mvn spring-boot:run`.

# Contributing
Thank you for your interest in contributing to **Competition Cubes**! Please read through the [Contribution Guidelines](https://github.com/Ph4nt0m37/CompetitionCubes/blob/main/CONTRIBUTING.md#contributing-guidelines) to learn how to contribute.
