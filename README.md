# Reactive Tweets

Powered by Spring Boot, one-button bootstrap, Reactive WebFlux Java

## To run the program:

- Clone this repository

Then

### a) Go to /src/main/resources and fill Twitter and Github properties:

- `./gradlew bootRun`

### b) Specify all the options using command line:

`./gradlew bootRun -Dserver.port -Dtwitter.oauth.consumerKey=<key> 
-Dtwitter.oauth.consumerSecret=<key> 
-Dtwitter.oauth.accessToken=<token> 
-Dtwitter.oauth.accessTokenSecret=<token>
-Dgithub.api.user_agent=<username>`
