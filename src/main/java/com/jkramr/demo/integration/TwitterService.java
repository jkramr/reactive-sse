package com.jkramr.demo.integration;

import com.jkramr.demo.model.RepoInfo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Data
@Component
@PropertySource("classpath:twitter_oauth.properties")
@ConfigurationProperties("twitter.oauth")
public class TwitterService {

  private String consumerKey;
  private String consumerSecret;
  private String accessToken;
  private String accessTokenSecret;

  @Bean
  Twitter twitter() {
    return new TwitterTemplate(
            consumerKey,
            consumerSecret,
            accessToken,
            accessTokenSecret
    );
  }

  public Flux<RepoInfo> getTweets(String searchQuery) {
    List<Tweet> tweets = twitter().searchOperations()
                                  .search(searchQuery)
                                  .getTweets();

    return Flux.create(tweetFluxSink -> tweetFluxSink.next(new RepoInfo(
            searchQuery,
            tweets
    )));
  }
}
