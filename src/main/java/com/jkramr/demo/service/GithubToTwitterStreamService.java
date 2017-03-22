package com.jkramr.demo.service;


import com.jkramr.demo.service.github.GithubRepo;
import com.jkramr.demo.service.github.GithubService;
import com.jkramr.demo.service.twitter.TwitterSearchResponse;
import com.jkramr.demo.service.twitter.TwitterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.function.Consumer;

@Component
public class GithubToTwitterStreamService {

  private GithubService      githubService;
  private TwitterService     twitterService;
  private Consumer<RepoInfo> outputConsumer;


  @Autowired
  public GithubToTwitterStreamService(
          GithubService githubService,
          TwitterService twitterService,
          Consumer<RepoInfo> outputConsumer
  ) {
    this.githubService = githubService;
    this.twitterService = twitterService;
    this.outputConsumer = outputConsumer;
  }

  public void start() {
    outputConsumer = System.out::println;
    githubService.getRepos()
                 .filter(Objects::nonNull)
                 .map(GithubRepo::getFullName)
                 .flatMap(twitterService::searchTweets)
                 .map(this::formatToJsonResponse)
                 .toStream()
                 .forEach(outputConsumer);
  }

  private RepoInfo formatToJsonResponse(TwitterSearchResponse twitterSearchResponse) {
    return new RepoInfo(
            twitterSearchResponse.getSearchQuery(),
            twitterSearchResponse.getTweets()
    );
  }

}
