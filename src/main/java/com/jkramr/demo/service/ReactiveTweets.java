package com.jkramr.demo.service;


import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class ReactiveTweets {

  private static final String PATH = "https://api.github.com/search";
  private static final String URI  = "/repositories?q=reactive";

  public void go() {

    String block = WebClient.create(PATH)
                            .get()
                            .uri(URI)
                            .header(
                                    "Accept",
                                    "application/vnd.github.v3+json"
                            )
                            .header("User-Agent", "jkramr")
                            .accept(MediaType.APPLICATION_JSON)
                            .exchange()
                            .then(clientResponse -> clientResponse.bodyToMono(
                                    String.class))
                            .block();

    System.out.println(block);
  }

}
