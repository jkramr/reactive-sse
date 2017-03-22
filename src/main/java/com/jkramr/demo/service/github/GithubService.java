package com.jkramr.demo.service.github;

import reactor.core.publisher.Mono;

public interface GithubService {
  Mono<GitHubRepoSearchResponse> getRepos();
}
