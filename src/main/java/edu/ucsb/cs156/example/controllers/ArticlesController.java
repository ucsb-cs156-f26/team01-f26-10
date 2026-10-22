package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
   * Get a single article by id.
   *
   * @param id the id of the article
   * @return the article with the given id
   * @throws EntityNotFoundException if no article has the given id
   */
  @Operation(
      summary = "Get a single article by id",
      description = "Returns the article with the given id, or HTTP 404 if it does not exist.")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public Articles getById(
      @Parameter(name = "id", description = "ID of the article to retrieve", example = "123")
          @RequestParam
          Long id) {
    Articles articles =
        articlesRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));

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

  /**
   * Delete a single article. Accessible only to administrators.
   *
   * @param id the id of the article to delete
   * @return a message indicating the record was deleted
   * @throws EntityNotFoundException if no article has the given id
   */
  @Operation(
      summary = "Delete a single article",
      description =
          "Deletes the article with the given id and returns a message such as record 123 deleted. "
              + "Returns HTTP 404 with Articles with id 123 not found if it does not exist. "
              + "Requires an administrator.")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteArticles(
      @Parameter(name = "id", description = "ID of the article to delete", example = "123")
          @RequestParam
          Long id) {
    Articles articles =
        articlesRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));

    articlesRepository.delete(articles);
    return genericMessage("record %s deleted".formatted(id));
  }

  /**
   * Update a single article. Accessible only to administrators.
   *
   * @param id the id of the article to update
   * @param incoming the new values for the article fields
   * @return the updated article
   * @throws EntityNotFoundException if no article has the given id
   */
  @Operation(
      summary = "Update a single article",
      description =
          "Updates title, url, explanation, email, and dateAdded from the JSON body. "
              + "The id is preserved; any id in the body is ignored. "
              + "Use ISO format for dateAdded, e.g. 2026-10-21T12:00:00. "
              + "Returns HTTP 404 if the article does not exist. Requires an administrator.")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public Articles updateArticles(
      @Parameter(name = "id", description = "ID of the article to update", example = "123")
          @RequestParam
          Long id,
      @RequestBody @Valid Articles incoming) {

    Articles articles =
        articlesRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));

    articles.setTitle(incoming.getTitle());
    articles.setUrl(incoming.getUrl());
    articles.setExplanation(incoming.getExplanation());
    articles.setEmail(incoming.getEmail());
    articles.setDateAdded(incoming.getDateAdded());

    articlesRepository.save(articles);

    return articles;
  }
}
