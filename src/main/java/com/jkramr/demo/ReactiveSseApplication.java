package com.jkramr.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.twitter.inbound.SearchReceivingMessageSource;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.concurrent.atomic.AtomicInteger;


@SpringBootApplication
@PropertySource("classpath:oauth.properties")
@RestController
@IntegrationComponentScan
public class ReactiveSseApplication {

  public static final long   TWEET_POLL_PERIOD  = 1000L;
  public static final String TWEET_SEARCH_QUERY = "#trump";

  public static void main(String[] args) {
    SpringApplication.run(ReactiveSseApplication.class, args);
  }

  @Bean
  CommandLineRunner commandLineRunner() {
    return args -> {
      Flux<String> flux = Flux.create(fluxSink -> {
        FluxSink<String> serialize = fluxSink.serialize();

        MessageHandler messageHandler =
                message -> {
                  String messageString = message.getPayload().toString();

                  System.out.println(messageString);

                  serialize.next(messageString);
                };

        inputChannel().subscribe(messageHandler);
      });
    };
  }

  private MessageSource<?> tweetMessageSource(
          Twitter twitter,
          String query
  ) {
    SearchReceivingMessageSource source = new SearchReceivingMessageSource(
            twitter, "foo");

    source.setQuery(query);

    return source;
  }

  @Bean
  public SubscribableChannel inputChannel() {
    return MessageChannels.publishSubscribe().get();
  }

  @Bean
  public IntegrationFlow myFlow(
          Twitter twitter

  ) {
    AtomicInteger counter = new AtomicInteger();
    return IntegrationFlows.from(
            tweetMessageSource(twitter, TWEET_SEARCH_QUERY),
            poller -> poller.poller(Pollers.fixedRate(TWEET_POLL_PERIOD))
    )
                           .transform((Tweet o) -> {
                             System.out.println(counter.incrementAndGet() + ": " + o.getText());
                             return o.toString();
                           })
                           .channel(inputChannel())
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
