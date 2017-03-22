package com.jkramr.demo.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkramr.demo.service.github.GitHubRepoSearchResponse;
import com.jkramr.demo.service.github.GithubRepo;
import com.jkramr.demo.service.github.GithubService;
import com.jkramr.demo.service.twitter.TwitterSearchResponse;
import com.jkramr.demo.service.twitter.TwitterService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

@Component
public class GithubToTwitterStreamService {

  private Logger logger;

  private Consumer<String> outputConsumer;

  private GithubService  githubService;
  private TwitterService twitterService;


  @Autowired
  public GithubToTwitterStreamService(
          Logger logger,
          GithubService githubService,
          TwitterService twitterService,
          Consumer<String> outputConsumer
  ) {
    this.logger = logger;
    this.githubService = githubService;
    this.twitterService = twitterService;
    this.outputConsumer = outputConsumer;
  }

  public void start() {
    outputConsumer = System.out::println;
    githubService.getRepos()
                 .map(GitHubRepoSearchResponse::getItems)
                 .flatMap(Flux::fromIterable)
                 .map(GithubRepo::getFullName)
                 .flatMap(twitterService::searchTweets)
                 .flatMap(this::formatToJsonResponse)
                 .doOnError(logger::error)
                 .toStream()
                 .forEach(outputConsumer);
  }

  private Flux<String> formatToJsonResponse(TwitterSearchResponse twitterSearchResponse) {
    RepoInfo repoInfo = new RepoInfo(
            twitterSearchResponse.getSearchQuery(),
            twitterSearchResponse.getTweets()
    );

    try {
      return Flux.just(new ObjectMapper().writeValueAsString(repoInfo));
    } catch (JsonProcessingException e) {
      return Flux.error(e);
    }
  }

}
