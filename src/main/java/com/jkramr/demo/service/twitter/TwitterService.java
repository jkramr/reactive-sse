package com.jkramr.demo.service.twitter;

import reactor.core.publisher.Flux;

public interface TwitterService {
  Flux<TwitterSearchResponse> searchTweets(String searchQuery);
}
