package com.jkramr.demo.service.twitter;

import reactor.core.publisher.Mono;

public interface TwitterService {
  Mono<TwitterSearchResponse> searchTweets(String searchQuery);
}
