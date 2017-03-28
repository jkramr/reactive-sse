package com.jkramr.demo.service.twitter;

import org.apache.log4j.Logger;
import org.springframework.social.ApiException;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;
import reactor.core.publisher.Mono;

import java.util.List;

public class SpringSocialTwitterService
        implements TwitterService {

  private final Twitter twitter;
  private       Logger  logger;

  public SpringSocialTwitterService(
          Logger logger,
          Twitter twitter
  ) {
    this.logger = logger;
    this.twitter = twitter;
  }

  @Override
  public Mono<TwitterSearchResponse> searchTweets(String searchQuery) {
    return getTweets(searchQuery)
            .map(tweets -> new TwitterSearchResponse(searchQuery, tweets))
            .doOnError(logger::error);
  }

  private Mono<List<Tweet>> getTweets(String searchQuery) {
    List<Tweet> tweets;

    try {
      tweets = twitter.searchOperations()
                      .search(searchQuery)
                      .getTweets();

    } catch (ApiException e) {
      return Mono.error(e);
    }

    return Mono.just(tweets);
  }

}