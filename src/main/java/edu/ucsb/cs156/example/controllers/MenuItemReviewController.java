package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Optional;
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

@Tag(name = "MenuItemReview")
@RequestMapping("/api/MenuItemReview")
@RestController
@Slf4j
public class MenuItemReviewController extends ApiController {

  @Autowired MenuItemReviewRepository menuItemReviewRepository;

  @Operation(summary = "List all menu item reviews")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<MenuItemReview> allMenuItemReviews() {
    Iterable<MenuItemReview> reviews = menuItemReviewRepository.findAll();
    return reviews;
  }

  @Operation(summary = "Create a new menu item review")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public MenuItemReview postMenuItemReview(
      @Parameter(name = "itemId") @RequestParam Long itemId,
      @Parameter(name = "reviewerEmail") @RequestParam String reviewerEmail,
      @Parameter(name = "stars") @RequestParam int stars,
      @Parameter(
              name = "dateReviewed",
              description = "Review date in ISO format, e.g. 2026-10-21T14:00:00")
          @RequestParam
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateReviewed,
      @Parameter(name = "comments") @RequestParam String comments) {
    MenuItemReview review = new MenuItemReview();

    review.setItemId(itemId);
    review.setReviewerEmail(reviewerEmail);
    review.setStars(stars);
    review.setDateReviewed(dateReviewed);
    review.setComments(comments);

    MenuItemReview savedReview = menuItemReviewRepository.save(review);

    return savedReview;
  }

  @Operation(summary = "Get a menu item review by id")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public MenuItemReview getMenuItemReviewById(@RequestParam Long id) {

    Optional<MenuItemReview> review = menuItemReviewRepository.findById(id);

    if (review.isEmpty()) {
      throw new EntityNotFoundException(MenuItemReview.class, id);
    }

    return review.get();
  }

  @Operation(
      summary = "Update a menu item review by id",
      description =
          "Find a review by id and update its fields using JSON. Returns 404 if the review does not exist.")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public MenuItemReview updateMenuItemReview(
      @Parameter(name = "id", description = "ID of the review to update") @RequestParam Long id,
      @RequestBody @Valid MenuItemReview incoming) {

    MenuItemReview review =
        menuItemReviewRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

    review.setItemId(incoming.getItemId());
    review.setReviewerEmail(incoming.getReviewerEmail());
    review.setStars(incoming.getStars());
    review.setDateReviewed(incoming.getDateReviewed());
    review.setComments(incoming.getComments());

    menuItemReviewRepository.save(review);

    return review;
  }

  @Operation(
      summary = "Delete a menu item review by id",
      description =
          "Delete an existing review by its id. Returns 404 if the review does not exist.")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteMenuItemReview(
      @Parameter(name = "id", description = "ID of the review to delete") @RequestParam Long id) {

    MenuItemReview review =
        menuItemReviewRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

    menuItemReviewRepository.delete(review);

    return genericMessage("record " + id + " deleted");
  }
}
