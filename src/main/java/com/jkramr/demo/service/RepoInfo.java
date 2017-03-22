package com.jkramr.demo.service;

import lombok.Data;
import org.springframework.social.twitter.api.Tweet;

import java.util.List;

@Data
public class RepoInfo {
  private final String      repo;
  private final List<Tweet> tweets;
}
