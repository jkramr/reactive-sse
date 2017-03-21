package com.jkramr.demo;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.integration.twitter.inbound.SearchReceivingMessageSource;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

@SpringBootApplication
@PropertySource("classpath:oauth.properties")
public class SpringCloudStreamsApplication {

  @Value("${debug:false}") boolean debug;

  @Value("${twitter.oauth.consumerKey}")       String consumerKey;
  @Value("${twitter.oauth.consumerSecret}")    String consumerSecret;
  @Value("${twitter.oauth.accessToken}")       String accessToken;
  @Value("${twitter.oauth.accessTokenSecret}") String accessTokenSecret;

  public static void main(String[] args) {
    SpringApplication.run(SpringCloudStreamsApplication.class, args);
  }

  @Bean
  @Scope("prototype")
  Logger logger(InjectionPoint ip) {
    return Logger.getLogger(ip.getDeclaredType().getName());
  }

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
  Writer<Tweet> tweetWriter() {
    return tweet -> "---- @" +
                    tweet.getFromUser() +
                    ": " +
                    tweet.getText();
  }

  public interface Writer<T> {
    String write(T obj);
  }

  @EnableBinding(Sink.class)
  public class TweetSink {

    @StreamListener(Sink.INPUT)
    public void receive(Tweet tweet) {
      System.out.println(tweetWriter().write(tweet));
    }
  }
}