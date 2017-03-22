package com.jkramr.demo.config;

import lombok.Data;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.twitter.inbound.SearchReceivingMessageSource;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

@Data
@SpringBootConfiguration
@PropertySource("classpath:twitter_oauth.properties")
@ConfigurationProperties("twitter.oauth")
public class TwitterConfig {

  private String consumerKey;
  private String consumerSecret;
  private String accessToken;
  private String accessTokenSecret;

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
}
