package com.jkramr.demo.service.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@PropertySource("classpath:github.properties")
@ConfigurationProperties("github.api.search")
public class ReactiveWebClientGithubService
        implements GithubService {

  private final Function<String, WebClient.HeaderSpec> gitHubClient;

  private String uri;
  private String query;
  private int    limit;

  public ReactiveWebClientGithubService(
          Function<String, WebClient.HeaderSpec> gitHubClient
  ) {
    this.gitHubClient = gitHubClient;
  }


  @Override
  public Flux<GithubRepo> getRepos() {

    return gitHubClient
            .apply(buildSearchQueryUri())
            .exchange()
            .then(clientResponse -> clientResponse.bodyToMono(String.class))

            .flatMap(json -> Flux.fromIterable(getRepos(json)));
  }

  private String buildSearchQueryUri() {
    return uri + query;
  }


  private List<GithubRepo> getRepos(String json) {
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

    return response.getItems()
                   .stream()
                   .limit(limit)
                   .collect(Collectors.toList());
  }
}