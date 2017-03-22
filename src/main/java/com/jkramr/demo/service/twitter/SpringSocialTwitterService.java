package com.jkramr.demo.service.twitter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;
import reactor.core.publisher.Flux;

import java.util.List;

public class SpringSocialTwitterService
        implements TwitterService {

  private final Twitter twitter;

  public SpringSocialTwitterService(Twitter twitter) {
    this.twitter = twitter;
  }

  @Override
  public Flux<TwitterSearchResponse> searchTweets(String searchQuery) {
    List<Tweet> tweets = twitter.searchOperations()
                                .search(searchQuery)
                                .getTweets();

    return Flux.create(tweetFluxSink -> tweetFluxSink.next(
            new TwitterSearchResponse(
                    searchQuery,
                    tweets
            )));
  }
}
