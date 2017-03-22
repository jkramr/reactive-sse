package com.jkramr.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.social.twitter.api.Tweet;

import java.util.List;

@Data
public class RepoInfo {
  private final String      repo;
  private final List<Tweet> tweets;

  @Override
  public String toString() {
    try {
      return new ObjectMapper().writeValueAsString(this);
    } catch (JsonProcessingException e) {
      return super.toString();
    }
  }
}
