package com.jkramr.demo;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.twitter.inbound.SearchReceivingMessageSource;
import org.springframework.integration.twitter.inbound.TimelineReceivingMessageSource;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
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

  private static String tweetToString(Tweet tweet) {
    return "---- @" +
           tweet.getFromUser() +
           ": " +
           tweet.getText();
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

  @EnableBinding(Sink.class)
  public static class TweetSink {

    @StreamListener(Sink.INPUT)
    public void receive(Tweet tweet) {
      System.out.println(tweetToString(tweet));
    }
  }

  @EnableBinding(Source.class)
  public class TweetSource {

    final Logger logger;

    @Autowired
    public TweetSource(Logger logger) {
      this.logger = logger;
    }

    @Bean
    @InboundChannelAdapter(value = Source.OUTPUT,
                           poller = @Poller(fixedDelay = "10000",
                                            maxMessagesPerPoll = "1"))
    public MessageSource<Tweet> inboundSearchMessageSource(
    ) {
      return searchMessageSource("trump", 20);
    }

    private Message<Tweet> getTweetMessage(
            String source,
            MessageSource messageSource
    ) {
      Message receive = receiveQuietly(messageSource);

      if (receive == null) {
        return null;
      }

      Message<Tweet> tweetMessage = MessageBuilder
              .withPayload((Tweet) receive.getPayload())
              .setHeader(
                      MessageHeaders.CONTENT_TYPE,
                      "application/x-java-object;type=org.springframework.social.twitter.api.Tweet"
              )
              .build();

      Tweet tweet = tweetMessage.getPayload();

      logger.debug("----" + source + ": sent message: " + tweetToString(tweet));

      return tweetMessage;
    }

    private Message receiveQuietly(MessageSource messageSource) {
      Message receive;

      try {
        receive = messageSource.receive();
      } catch (Exception e) {
        return null;
      }

      return receive;
    }

    private MessageSource<Tweet> searchMessageSource(
            String query,
            int pageSize
    ) {
      SearchReceivingMessageSource messageSource = searchTwitterMessageSource();

      messageSource.setQuery(query);
      messageSource.setPageSize(pageSize);

      return () -> getTweetMessage(
              "search:" + query,
              searchTwitterMessageSource()
      );
    }

  }
}