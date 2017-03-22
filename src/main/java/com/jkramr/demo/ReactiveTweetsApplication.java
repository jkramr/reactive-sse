package com.jkramr.demo;

import com.jkramr.demo.service.GithubToTwitterStreamService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
@EnableConfigurationProperties
public class ReactiveTweetsApplication {

  @Value("${debug:false}") boolean debug;

  public static void main(String[] args) {
    SpringApplication.run(ReactiveTweetsApplication.class, args);
  }

  @Bean
  CommandLineRunner commandLineRunner(
          GithubToTwitterStreamService reactiveTweetsService
  ) {
    return args -> reactiveTweetsService.start();
  }
}