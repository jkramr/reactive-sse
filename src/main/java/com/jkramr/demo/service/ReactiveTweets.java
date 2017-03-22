package com.jkramr.demo.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkramr.demo.config.GithubConfig;
import com.jkramr.demo.model.GitHubReposResponse;
import com.jkramr.demo.model.Repo;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
public class ReactiveTweets {

  private final Twitter      twitter;
  private       GithubConfig githubConfig;

  @Autowired
  public ReactiveTweets(
          Twitter twitter,
          GithubConfig githubConfig
  ) {
    this.twitter = twitter;
    this.githubConfig = githubConfig;
  }

  public void go() {
    WebClient.HeaderSpec request = WebClient.create(githubConfig.getHost())
                                            .get()
                                            .uri(githubConfig.getUri())
                                            .header(
                                                    "Accept",
                                                    "application/vnd.github.v3+json"
                                            )
                                            .header("User-Agent", "jkramr")
                                            .accept(MediaType.APPLICATION_JSON);

    request.exchange()
           .then(clientResponse -> clientResponse.bodyToMono(
                   String.class))

           .flatMap(json -> Flux.fromIterable(getRepos(json)))
           .filter(Objects::nonNull)
           .flatMap(this::getTweets)
           .toStream()
           .forEach(System.out::println);
  }

  private Flux<RepoInfo> getTweets(Repo repo) {
    List<Tweet> tweets = twitter.searchOperations()
                                .search(repo.getFullName())
                                .getTweets();

    return Flux.create(tweetFluxSink -> tweetFluxSink.next(new RepoInfo(
            repo.getFullName(),
            tweets
    )));
  }

  private List<Repo> getRepos(String json) {
    ObjectMapper objectMapper = new ObjectMapper();

    GitHubReposResponse response;
    try {
      response = objectMapper.readValue(
              json,
              GitHubReposResponse.class
      );
    } catch (IOException e) {
      return Collections.emptyList();
    }

    return response.getItems();
  }

  @Data
  private class RepoInfo {
    private final String      repo;
    private final List<Tweet> tweets;

    @Override
    public String toString() {
      try {
        return new ObjectMapper().writeValueAsString(this);
      } catch (JsonProcessingException e) {
        return super.toString();
      }
    }
  }
}
