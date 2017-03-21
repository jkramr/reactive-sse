package com.jkramr.demo;

import com.jkramr.demo.SpringCloudStreamsApplication.Writer;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.twitter.inbound.SearchReceivingMessageSource;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;

@EnableBinding(Source.class)
public class TweetSource {

  private final Logger                       logger;
  private final Twitter                      twitter;
  private final SearchReceivingMessageSource searchTwitterMessageSource;
  private       Writer<Tweet>                tweetWriter;

  @Autowired
  public TweetSource(
          Logger logger,
          Twitter twitter,
          Writer<Tweet> tweetWriter,
          SearchReceivingMessageSource searchTwitterMessageSource
  ) {
    this.logger = logger;
    this.twitter = twitter;
    this.tweetWriter = tweetWriter;
    this.searchTwitterMessageSource = searchTwitterMessageSource;
  }

  @Bean
  @InboundChannelAdapter(value = Source.OUTPUT,
                         poller = @Poller(fixedDelay = "10000",
                                          maxMessagesPerPoll = "1"))
  public MessageSource<Tweet> inboundSearchMessageSource(
  ) {
    return twitterSearchMessageSource("trump", 20);
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

    logger.debug("----" +
                 source +
                 ": sent message: " +
                 tweetWriter.write(tweet));

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

  private MessageSource<Tweet> twitterSearchMessageSource(
          String query,
          int pageSize
  ) {
    SearchReceivingMessageSource messageSource = searchTwitterMessageSource;

    messageSource.setQuery(query);
    messageSource.setPageSize(pageSize);

    return () -> getTweetMessage(
            "search:" + query,
            searchTwitterMessageSource
    );
  }

}
