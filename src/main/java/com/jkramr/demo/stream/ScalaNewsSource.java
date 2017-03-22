package com.jkramr.demo.stream;

import com.jkramr.demo.util.Formatter;
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

@EnableBinding(Source.class)
public class ScalaNewsSource {

  private static final String HASH_TAG        = "#scala";
  private static final int    TWEET_PAGE_SIZE = 20;

  private final Logger                       logger;
  private final SearchReceivingMessageSource searchTwitterMessageSource;
  private final Formatter<Tweet>             tweetFormatter;


  @Autowired
  public ScalaNewsSource(
          Logger logger,
          Formatter<Tweet> tweetFormatter,
          SearchReceivingMessageSource searchTwitterMessageSource
  ) {
    this.logger = logger;
    this.tweetFormatter = tweetFormatter;
    this.searchTwitterMessageSource = searchTwitterMessageSource;
  }

  @Bean
  @InboundChannelAdapter(value = Source.OUTPUT,
                         poller = @Poller(fixedDelay = "10000",
                                          maxMessagesPerPoll = "1"))
  public MessageSource<Tweet> inboundSearchMessageSource() {
    SearchReceivingMessageSource messageSource = searchTwitterMessageSource;

    messageSource.setQuery(HASH_TAG);
    messageSource.setPageSize(TWEET_PAGE_SIZE);

    return () -> getTweetMessage(
            "scala news: ",
            searchTwitterMessageSource
    );
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
                    "application/x-java-object;type=" +
                    Tweet.class.getTypeName()
            )
            .build();

    Tweet tweet = tweetMessage.getPayload();

    logger.debug("----" +
                 source +
                 ": sent message: " +
                 tweetFormatter.format(tweet));

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
}
