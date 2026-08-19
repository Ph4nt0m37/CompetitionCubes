# Contributing Guidelines
Hello! First of all, I want to thank you for your interest in contributing to Competition Cubes. This project is a personal pride of mine, and I'm thrilled to have any help I can get!

## How to Contribute
If you want to contribute, first you need clone either the `main` (production) branch or the `test` (testing) branch. Then, you to set up the Development Environment (see [Setting Up Development Environment](#setting-up-development-environment)).

Once you have set up the environment and can successfully run Competition Cubes locally, create a new branch with a name that describes the issue you are trying to solve or the feature you are trying to add. Once you have implemented your fix/feature, create a pull request with a descriptive name and description that describes the fix/feature. 

From here, I review the pull request and approve/deny it. If approved, it will be merged into the test branch to undergo testing. Finally, if it passes testing, it will eventually be merged with main for production.

## Setting Up Development Environment
### Requirements
- Java 17
- PostgreSQL
- Maven

### Database Setup
The Competition Cubes program **will not run** unless you have the database setup. To setup the database, create a Postgres database then import the `comp_cube_test_db.sql` SQL dump. This dump has **no** user data so it will be empty.

### Program Execution
Once you have met all the requirements and have the database set up, you can run Competition Cubes with `mvn spring-boot:run`.

## Thank You
Thank you so much for your interest in contributing to Competition Cubes! With your help, Competition Cubes can continue to grow and improve for the sake of all of its users.
