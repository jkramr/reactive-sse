package com.jkramr.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.file.dsl.Files;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.File;

@SpringBootApplication
@PropertySource("classpath:oauth.properties")
@RestController
public class ReactiveSseApplication {

  public static void main(String[] args) {
    SpringApplication.run(ReactiveSseApplication.class, args);
  }

  @Bean
  SubscribableChannel filesChannel() {
    return MessageChannels.publishSubscribe().get();
  }

  @GetMapping(value = "/files/{name}",
              produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  Flux<String> files(@PathVariable String name) {
    return Flux.create(this::serializeSink);
  }

  private void serializeSink(FluxSink<String> sink) {
    FluxSink<String> serialize = sink.serialize();

    MessageHandler handler = message -> serialize.next(formatMessage(message));

    serialize.setCancellation(() -> filesChannel().unsubscribe(handler));

    filesChannel().subscribe(handler);
  }

  private static String formatMessage(Message<?> msg) {
    return msg.getPayload().toString();
  }

  @Bean
  IntegrationFlow integrationFlow(
          @Value("${input-dir:file://${HOME}/Desktop/in}") File in
  ) {
    return IntegrationFlows.from(
            Files.inboundAdapter(in)
                 .autoCreateDirectory(true),
            poller -> poller.poller(spec -> spec.fixedRate(1000L))
    )
                           .transform(File.class, File::getAbsolutePath)
                           .channel(filesChannel())
                           .get();
  }

  @Bean
  Twitter twitter(
          @Value("${twitter.oauth.consumerKey}") String consumerKey,
          @Value("${twitter.oauth.consumerSecret}") String consumerSecret,
          @Value("${twitter.oauth.accessToken}") String accessToken,
          @Value("${twitter.oauth.accessTokenSecret}") String accessTokenSecret
  ) {
    return new TwitterTemplate(
            consumerKey,
            consumerSecret,
            accessToken,
            accessTokenSecret
    );
  }
}
