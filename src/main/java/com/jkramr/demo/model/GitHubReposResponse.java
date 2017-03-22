package com.jkramr.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jkramr.demo.model.Repo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubReposResponse {
  private List<Repo> items;
}
