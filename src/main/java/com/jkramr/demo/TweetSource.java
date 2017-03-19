package com.jkramr.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.twitter.inbound.SearchReceivingMessageSource;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;

@EnableBinding(TweetSource.Source.class)
public class TweetSource {

  private final Twitter twitter;

  @Autowired
  public TweetSource(Twitter twitter) {
    this.twitter = twitter;
  }

  @Bean
  MessageSource<?> tweetMessageSource() {
    SearchReceivingMessageSource messageSource =
            new SearchReceivingMessageSource(twitter, "foo");

    messageSource.setQuery("trump");
    messageSource.setPageSize(20);

    return messageSource;
  }

  @Bean
  @InboundChannelAdapter(value = Source.SAMPLE,
                         poller = @Poller(fixedRate = "60000",
                                          maxMessagesPerPoll = "20"))
  public MessageSource<Tweet> timerMessageSource(
  ) {
    return () -> {

      Message receive = receiveQuietly();

      if (receive == null) {
        return null;
      }

      Message<Tweet> tweet = MessageBuilder
              .withPayload((Tweet) receive.getPayload())
              .setHeader(
                      MessageHeaders.CONTENT_TYPE,
                      "application/json"
              )
              .build();

      System.out.println("Sending tweet: " + tweet.getPayload()
                                                  .getText());

      return tweet;
    };
  }

  private Message receiveQuietly() {
    Message receive;

    try {
      receive = tweetMessageSource().receive();
    } catch (Exception e) {
      return null;
    }

    return receive;
  }

  public interface Source {

    String SAMPLE = "sample-source";

    @Output(SAMPLE)
    MessageChannel sampleSource();

  }
}
