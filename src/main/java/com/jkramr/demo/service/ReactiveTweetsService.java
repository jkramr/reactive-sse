package com.jkramr.demo.service;


import com.jkramr.demo.integration.GithubService;
import com.jkramr.demo.integration.TwitterService;
import com.jkramr.demo.model.Repo;
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
                 .map(Repo::getFullName)
                 .flatMap(twitterService::getTweets)
                 .toStream()
                 .forEach(System.out::println);
  }
}
