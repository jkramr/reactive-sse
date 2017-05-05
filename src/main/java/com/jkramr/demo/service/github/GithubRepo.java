package com.jkramr.demo.service.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Stack;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubRepo {
  @JsonProperty("full_name")
  String fullName;

  public static void main(String[] args) {
    Stack stack = new Stack();
    stack.peek();
  }
}
