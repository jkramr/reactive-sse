package com.jkramr.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

@SpringBootApplication
@PropertySource("classpath:oauth.properties")
public class ReactiveSseApplication {

  public static void main(String[] args) {
    SpringApplication.run(ReactiveSseApplication.class, args);
  }

  @Bean
  Twitter twitter(
          @Value("${twitter.oauth.consumerKey}") String consumerKey,
          @Value("${twitter.oauth.consumerSecret}") String consumerSecret,
          @Value("${twitter.oauth.accessToken}") String accessToken,
          @Value("${twitter.oauth.accessTokenSecret}") String accessTokenSecret
  ) {
    return new TwitterTemplate(consumerKey, consumerSecret, accessToken, accessTokenSecret);
  }
}
