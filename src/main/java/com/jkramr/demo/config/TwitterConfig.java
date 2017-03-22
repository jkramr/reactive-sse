package com.jkramr.demo.config;

import com.jkramr.demo.util.Formatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.twitter.inbound.SearchReceivingMessageSource;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

@SpringBootConfiguration
@PropertySource("classpath:twitter_oauth.properties")
public class TwitterConfig {

  @Value("${twitter.oauth.consumerKey}")       String consumerKey;
  @Value("${twitter.oauth.consumerSecret}")    String consumerSecret;
  @Value("${twitter.oauth.accessToken}")       String accessToken;
  @Value("${twitter.oauth.accessTokenSecret}") String accessTokenSecret;

  @Bean
  SearchReceivingMessageSource searchTwitterMessageSource() {
    return new SearchReceivingMessageSource(twitter(), "twitter");
  }

  @Bean
  Twitter twitter() {
    return new TwitterTemplate(
            consumerKey,
            consumerSecret,
            accessToken,
            accessTokenSecret
    );
  }

  @Bean
  Formatter<Tweet> tweetWriter() {
    return tweet -> "---- @" +
                    tweet.getFromUser() +
                    ": " +
                    tweet.getText();
  }

}
