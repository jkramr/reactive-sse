package com.jkramr.demo.config;

import lombok.Data;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@SpringBootConfiguration
@ConfigurationProperties("github.api")
public class GithubConfig {

  private String host;
  private String uri;
}