package com.jkramr.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.twitter.inbound.SearchReceivingMessageSource;
import org.springframework.messaging.MessageHandler;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


@SpringBootApplication
@PropertySource("classpath:oauth.properties")
@RestController
@IntegrationComponentScan
public class ReactiveSseApplication {

  public static final long TWEET_POLL_PERIOD = 10 * 1000L;
  public static final int  TWEET_PAGE_SIZE   = 30;

  public static final String TWEET_SEARCH_QUERY = "#scala";

  @Value("${twitter.oauth.consumerKey}")       String consumerKey;
  @Value("${twitter.oauth.consumerSecret}")    String consumerSecret;
  @Value("${twitter.oauth.accessToken}")       String accessToken;
  @Value("${twitter.oauth.accessTokenSecret}") String accessTokenSecret;

  private Map<String, SseEmitter> sses = new ConcurrentHashMap<>();


  public static void main(String[] args) {
    SpringApplication.run(ReactiveSseApplication.class, args);
  }

  @Bean
  PublishSubscribeChannel messageChannel() {
    return MessageChannels.publishSubscribe().get();
  }

  @GetMapping("/twitter/regular/{query}")
  SseEmitter regular(@PathVariable String query) {
    SseEmitter sseEmitter = new SseEmitter(60 * 1000L);

    sses.put(query, sseEmitter);

    return sseEmitter;
  }

  @GetMapping(value = "/twitter/reactive/{query}",
              produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  Flux<String> reactive(@PathVariable String query) {
    return Flux.create(emitter -> {
      FluxSink<String> serialize = emitter.serialize();

      MessageHandler handler =
              message -> serialize.next(message.getPayload().toString());

      messageChannel().subscribe(handler);
    });
  }

  private MessageSource<?> twitterMessageSource(Twitter twitter) {
    SearchReceivingMessageSource
            tweetMessageSource
            = new SearchReceivingMessageSource(
            twitter, "foo");

    tweetMessageSource.setQuery(TWEET_SEARCH_QUERY);
    tweetMessageSource.setPageSize(TWEET_PAGE_SIZE);

    return tweetMessageSource;
  }

  @Bean
  public IntegrationFlow myFlow(
          Twitter twitter

  ) {
    AtomicInteger counter = new AtomicInteger();

    return IntegrationFlows.from(
            twitterMessageSource(twitter),
            poller -> poller.poller(Pollers.fixedRate(TWEET_POLL_PERIOD))
    )
                           .transform((Tweet tweet) -> {
                             System.out.println("transform: " +
                                                counter.incrementAndGet() +
                                                ": " +
                                                tweet.getText());

                             return tweet.getText();
                           })
//                           .channel(messageChannel())
                           .handle(handleEmmitter())
                           .get();
  }

  private MessageHandler handleEmmitter() {
    return message -> sses.forEach((k, sse) -> {
      try {
        sse.send(message);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
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
