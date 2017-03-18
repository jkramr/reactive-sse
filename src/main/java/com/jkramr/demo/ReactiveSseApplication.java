package com.jkramr.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.twitter.inbound.TimelineReceivingMessageSource;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@PropertySource("classpath:oauth.properties")
@RestController
@IntegrationComponentScan
public class ReactiveSseApplication {

  public static void main(String[] args) {
    SpringApplication.run(ReactiveSseApplication.class, args);
  }

  private TimelineReceivingMessageSource tweetMessageSource(
          Twitter twitter
  ) {
    TimelineReceivingMessageSource source = new TimelineReceivingMessageSource(
            twitter, "foo");


    return source;
  }

  @Bean
  public DirectChannel inputChannel() {
    return new DirectChannel();
  }

  @Bean
  public IntegrationFlow myFlow(
          Twitter twitter

  ) {
    return IntegrationFlows.from(
            tweetMessageSource(twitter),
            poller -> poller.poller(Pollers.fixedRate(1000L))
    )
                           .channel(this.inputChannel())
                           .transform(Object::toString)
                           .channel(MessageChannels.queue())
                           .get();
  }

  @Bean
  Twitter twitter(
          @Value("${twitter.oauth.consumerKey}") String consumerKey,
          @Value("${twitter.oauth.consumerSecret}") String consumerSecret,
          @Value("${twitter.oauth.accessToken}") String accessToken,
          @Value("${twitter.oauth.accessTokenSecret}") String accessTokenSecret
  ) {
    return new TwitterTemplate(
            consumerKey,
            consumerSecret,
            accessToken,
            accessTokenSecret
    );
  }
}
