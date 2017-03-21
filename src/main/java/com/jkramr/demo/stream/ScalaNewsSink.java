package com.jkramr.demo.stream;

import com.jkramr.demo.util.Formatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.social.twitter.api.Tweet;

@EnableBinding(Sink.class)
public class ScalaNewsSink {

  private final Formatter<Tweet> tweetFormatter;

  @Autowired
  public ScalaNewsSink(
          Formatter<Tweet> tweetFormatter
  ) {
    this.tweetFormatter = tweetFormatter;
  }

  @StreamListener(Sink.INPUT)
  public void receive(Tweet tweet) {
    System.out.println(tweetFormatter.format(tweet));
  }
}
