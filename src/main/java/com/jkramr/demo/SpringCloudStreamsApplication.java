package com.jkramr.demo;

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

  @Value("${twitter.oauth.consumerKey}")       String consumerKey;
  @Value("${twitter.oauth.consumerSecret}")    String consumerSecret;
  @Value("${twitter.oauth.accessToken}")       String accessToken;
  @Value("${twitter.oauth.accessTokenSecret}") String accessTokenSecret;

  public static void main(String[] args) {
    SpringApplication.run(SpringCloudStreamsApplication.class, args);
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
  public static class TweetSource {

    private final Twitter twitter;

    @Autowired
    public TweetSource(Twitter twitter) {
      this.twitter = twitter;
    }

    @Bean
    SearchReceivingMessageSource searchTwitterMessageSource() {
      return new SearchReceivingMessageSource(twitter, "foo");
    }

    @Bean
    TimelineReceivingMessageSource timelineTwitterMessageSource() {
      return new TimelineReceivingMessageSource(twitter, "foo");
    }

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
      return searchMessageSource("trump_search", "trump", 20);
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

      System.out.println("----" +
                         source +
                         ": @" +
                         tweet.getFromUser() +
                         ": " +
                         tweet.getText());

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
            String source,
            String query,
            int pageSize
    ) {
      SearchReceivingMessageSource messageSource = searchTwitterMessageSource();

      messageSource.setQuery(query);
      messageSource.setPageSize(pageSize);

      return () -> getTweetMessage(source, searchTwitterMessageSource());
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
      System.out.println("******************");
      System.out.println("At the Sink");
      System.out.println("******************");
      System.out.println("Received message " +
                         tweet.getText() +
                         " of type " +
                         tweet.getClass());
    }

  }
}