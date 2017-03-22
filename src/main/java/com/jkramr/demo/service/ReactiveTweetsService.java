package com.jkramr.demo.service;


import com.jkramr.demo.service.github.GithubRepo;
import com.jkramr.demo.service.github.GithubService;
import com.jkramr.demo.service.twitter.TwitterSearchResponse;
import com.jkramr.demo.service.twitter.TwitterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ReactiveTweetsService {

  private GithubService  githubService;
  private TwitterService twitterService;


  @Autowired
  public ReactiveTweetsService(
          GithubService githubService,
          TwitterService twitterService
  ) {
    this.githubService = githubService;
    this.twitterService = twitterService;
  }

  public void start() {
    githubService.getRepos()
                 .filter(Objects::nonNull)
                 .map(GithubRepo::getFullName)
                 .flatMap(twitterService::searchTweets)
                 .map(this::formatToJsonResponse)
                 .toStream()
                 .forEach(System.out::println);
  }

  private RepoInfo formatToJsonResponse(TwitterSearchResponse twitterSearchResponse) {
    return new RepoInfo(
            twitterSearchResponse.getSearchQuery(),
            twitterSearchResponse.getTweets()
    );
  }

}
