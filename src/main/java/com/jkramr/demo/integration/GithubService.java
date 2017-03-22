package com.jkramr.demo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkramr.demo.model.GitHubRepoSearchResponse;
import com.jkramr.demo.model.Repo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Data
@Component
@PropertySource("classpath:github.properties")
@ConfigurationProperties("github.api")
public class GithubService {

  private String   host;
  private String   uri;
  private String   action;
  private String   userAgent;

  public Flux<Repo> getRepos() {

    return createRequest().exchange()
                          .then(clientResponse -> clientResponse.bodyToMono(
                                  String.class))

                          .flatMap(json -> Flux.fromIterable(getRepos(
                                  json)));
  }

  private WebClient.HeaderSpec createRequest() {
    return WebClient.create(host)
                    .get()
                    .uri(uri)
                    .header("Action", action)
                    .header("User-Agent", userAgent)
                    .accept(MediaType.APPLICATION_JSON);
  }

  private List<Repo> getRepos(String json) {
    ObjectMapper objectMapper = new ObjectMapper();

    GitHubRepoSearchResponse response;
    try {
      response = objectMapper.readValue(
              json,
              GitHubRepoSearchResponse.class
      );
    } catch (IOException e) {
      return Collections.emptyList();
    }

    return response.getItems();
  }
}