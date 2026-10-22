package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for Articles. */
@Tag(name = "Articles")
@RequestMapping("/api/Articles")
@RestController
@Slf4j
public class ArticlesController extends ApiController {

  @Autowired ArticlesRepository articlesRepository;

  /**
   * List all articles.
   *
   * @return an iterable of Articles
   */
  @Operation(summary = "List all articles")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<Articles> allArticles() {
    Iterable<Articles> articles = articlesRepository.findAll();
    return articles;
  }

  /**
   * Create a new article. Accessible only to administrators.
   *
   * @param title the article title
   * @param url the URL of the article
   * @param explanation a description of the article
   * @param email the email of the person who submitted the article
   * @param dateAdded the date and time the article was added
   * @return the saved article
   */
  @Operation(summary = "Create a new article")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public Articles postArticles(
      @Parameter(name = "title", description = "Article title") @RequestParam String title,
      @Parameter(name = "url", description = "URL of the article") @RequestParam String url,
      @Parameter(name = "explanation", description = "Description of the article") @RequestParam
          String explanation,
      @Parameter(name = "email", description = "Email of the person who submitted the article")
          @RequestParam
          String email,
      @Parameter(
              name = "dateAdded",
              description = "Date added in ISO format, e.g. 2026-10-21T12:00:00")
          @RequestParam("dateAdded")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateAdded) {

    Articles articles = new Articles();
    articles.setTitle(title);
    articles.setUrl(url);
    articles.setExplanation(explanation);
    articles.setEmail(email);
    articles.setDateAdded(dateAdded);

    Articles savedArticles = articlesRepository.save(articles);

    return savedArticles;
  }
}
