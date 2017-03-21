package com.jkramr.demo;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@SpringBootApplication
public class SpringCloudStreamsApplication {

  @Value("${debug:false}") boolean debug;

  public static void main(String[] args) {
    SpringApplication.run(SpringCloudStreamsApplication.class, args);
  }

  @Bean
  @Scope("prototype")
  Logger logger(InjectionPoint ip) {
    return Logger.getLogger(ip.getDeclaredType().getName());
  }

}