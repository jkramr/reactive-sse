package com.jkramr.demo.service.twitter;

import lombok.Data;
import org.springframework.social.twitter.api.Tweet;

import java.util.List;

@Data
public class TwitterSearchResponse {
  private final String      searchQuery;
  private final List<Tweet> tweets;
}
