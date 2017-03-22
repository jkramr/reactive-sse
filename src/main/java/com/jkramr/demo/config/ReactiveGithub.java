package com.jkramr.demo.config;

import lombok.Data;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.function.Function;

@Data
@PropertySource("classpath:github.properties")
@ConfigurationProperties("github.api")
@SpringBootConfiguration
public class ReactiveGithub {

  private String host;
  private String headerAccept;
  private String headerUserAgent;

  @Bean
  Function<String, WebClient.HeaderSpec> githubWebClient(

  ) {
    return uri -> WebClient.create(host)
                           .get()
                           .uri(uri)
                           .header("Accept", headerAccept)
                           .header("User-Agent", headerUserAgent)
                           .accept(MediaType.APPLICATION_JSON);
  }

}
