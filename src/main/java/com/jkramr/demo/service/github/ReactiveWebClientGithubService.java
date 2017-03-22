package com.jkramr.demo.service.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.apache.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@PropertySource("classpath:github.properties")
@ConfigurationProperties("github.api.search")
public class ReactiveWebClientGithubService
        implements GithubService {

  private final Function<String, WebClient.HeaderSpec> gitHubClient;
  private       Logger                                 logger;

  private String uri;
  private String query;
  private int    limit;

  public ReactiveWebClientGithubService(
          Function<String, WebClient.HeaderSpec> gitHubClient,
          Logger logger
  ) {
    this.gitHubClient = gitHubClient;
    this.logger = logger;
  }

  @Override
  public Mono<GitHubRepoSearchResponse> getRepos() {

    return gitHubClient
            .apply(buildSearchQueryUri())
            .exchange()
            .then(clientResponse -> clientResponse.bodyToMono(String.class))
            .then(this::trySearchForRepos)
            .then(this::limitItems)
            .doOnError(logger::error);
  }

  private Mono<GitHubRepoSearchResponse> trySearchForRepos(String json) {
    ObjectMapper objectMapper = new ObjectMapper();

    GitHubRepoSearchResponse response;
    try {
      response = objectMapper.readValue(
              json,
              GitHubRepoSearchResponse.class
      );
    } catch (IOException e) {
      return Mono.error(e);
    }

    return Mono.just(response);
  }

  private Mono<GitHubRepoSearchResponse> limitItems(GitHubRepoSearchResponse response) {
    List<GithubRepo> limitItems = response.getItems()
                                          .stream()
                                          .limit(limit)
                                          .collect(Collectors.toList());

    response.setItems(limitItems);

    return Mono.just(response);
  }

  private String buildSearchQueryUri() {
    return uri + query;
  }
}