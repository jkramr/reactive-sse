package com.jkramr.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.twitter.inbound.SearchReceivingMessageSource;
import org.springframework.integration.twitter.inbound.TimelineReceivingMessageSource;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;


@SpringBootApplication
@PropertySource("classpath:oauth.properties")
public class SpringCloudStreamsApplication {

  @Value("${twitter.oauth.consumerKey}")       String consumerKey;
  @Value("${twitter.oauth.consumerSecret}")    String consumerSecret;
  @Value("${twitter.oauth.accessToken}")       String accessToken;
  @Value("${twitter.oauth.accessTokenSecret}") String accessTokenSecret;

  public static void main(String[] args) {
    SpringApplication.run(SpringCloudStreamsApplication.class, args);
  }

  @Bean
  SearchReceivingMessageSource searchTwitterMessageSource() {
    return new SearchReceivingMessageSource(twitter(), "foo");
  }

  @Bean
  TimelineReceivingMessageSource timelineTwitterMessageSource() {
    return new TimelineReceivingMessageSource(twitter(), "foo");
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

  @EnableBinding(Source.class)
  public class TweetSource {

    @Bean
    @InboundChannelAdapter(value = Source.OUTPUT,
                           poller = @Poller(fixedDelay = "10000",
                                            maxMessagesPerPoll = "1"))
    public MessageSource<Tweet> inboundTimelineMessageSource(
    ) {
      return timeLineMessageSource(20);
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

      System.out.println("----" +
                         source +
                         ": sent");

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

      return () -> getTweetMessage("search:" + query, searchTwitterMessageSource());
    }

    private MessageSource<Tweet> timeLineMessageSource(int pageSize) {
      return () -> {
        TimelineReceivingMessageSource messageSource =
                timelineTwitterMessageSource();

        messageSource.setPageSize(pageSize);

        return getTweetMessage("timeline", messageSource);
      };
    }

  }

  @EnableBinding(Sink.class)
  public static class TweetSink {

    @StreamListener(Sink.INPUT)
    public void receive(Tweet tweet) {
      System.out.println("---- @" + tweet.getFromUser() + ": " + tweet.getText());
    }
  }
}