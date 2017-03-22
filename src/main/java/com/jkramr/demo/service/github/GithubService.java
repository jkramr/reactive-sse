package com.jkramr.demo.service.github;

import reactor.core.publisher.Flux;

public interface GithubService {
  Flux<GithubRepo> getRepos();
}
