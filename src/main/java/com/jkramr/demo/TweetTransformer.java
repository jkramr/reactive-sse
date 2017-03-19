package com.jkramr.demo;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.social.twitter.api.Tweet;

@EnableBinding(Processor.class)
public class TweetTransformer {

  private static final String TRANSFORMATION_VALUE = "@jkramr";

  @StreamListener(Processor.INPUT)
  @SendTo(Processor.OUTPUT)
  public Tweet receive(Tweet tweet) {
    System.out.println("******************");
    System.out.println("At the transformer");
    System.out.println("******************");
    System.out.println("Received value "+ tweet.getText() + " of type " + tweet.getClass());
    System.out.println("Transforming the sender to " +
                       TRANSFORMATION_VALUE + " and with the type " + tweet.getClass());
    tweet.setFromUser(TRANSFORMATION_VALUE);
    return tweet;
  }
}
