package com.jkramr.demo.config;

import com.jkramr.demo.service.github.GithubService;
import com.jkramr.demo.service.github.ReactiveWebClientGithubService;
import com.jkramr.demo.service.twitter.SpringSocialTwitterService;
import com.jkramr.demo.service.twitter.TwitterService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.function.Consumer;
import java.util.function.Function;

@SpringBootConfiguration
public class ApplicationConfig {

  @Bean
  GithubService githubService(
          Function<String, WebClient.HeaderSpec> gitHubClient,
          Logger logger
  ) {
    return new ReactiveWebClientGithubService(gitHubClient, logger);
  }

  @Bean
  TwitterService twitterService(
          Logger logger,
          Twitter twitter
  ) {
    return new SpringSocialTwitterService(logger, twitter);
  }

  @Bean
  @Scope("prototype")
  Logger logger(InjectionPoint ip) {
    return Logger.getLogger(ip.getDeclaredType().getName());
  }

  @Bean
  Consumer<String> outputConsumer() {
    return System.out::println;
  }
}
