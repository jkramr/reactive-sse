# Reactive Tweets

Powered by Spring Boot, one-button bootstrap, Reactive WebFlux Java

## To run the program:

### 1) Clone this repository

### 2a) Go to /src/main/resources, fill `twitter_oauth.properties` then run:

- `./gradlew bootRun`

### 2b) Specify all the options using command line:

`./gradlew bootRun
-Dserver.port=8083
-Dtwitter.oauth.consumerKey=<key> 
-Dtwitter.oauth.consumerSecret=<key> 
-Dtwitter.oauth.accessToken=<token> 
-Dtwitter.oauth.accessTokenSecret=<token>
-Dgithub.api.query=<custom_query>`

## Required properties:

Twitter handles:

- twitter.oauth.consumerKey
 
- twitter.oauth.consumerSecret
 
- twitter.oauth.accessToken 

- twitter.oauth.accessTokenSecret