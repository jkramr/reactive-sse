package com.jkramr.demo;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.social.twitter.api.Tweet;

@EnableBinding(TweetSink.Sink.class)
public class TweetSink {

  @StreamListener(Sink.SAMPLE)
  public void receive(Tweet tweet) {
    System.out.println("******************");
    System.out.println("At the Sink");
    System.out.println("******************");
    System.out.println("Received transformed message " +
                       tweet.getText() +
                       " of type " +
                       tweet.getClass());
  }

  public interface Sink {
    String SAMPLE = "sample-sink";

    @Input(SAMPLE)
    SubscribableChannel sampleSink();
  }
}
