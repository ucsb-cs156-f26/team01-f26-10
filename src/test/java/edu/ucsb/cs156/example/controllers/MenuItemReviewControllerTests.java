package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = MenuItemReviewController.class)
@Import(TestConfig.class)
public class MenuItemReviewControllerTests extends ControllerTestCase {

  @MockitoBean MenuItemReviewRepository menuItemReviewRepository;

  @MockitoBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/MenuItemReview/all")).andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_reviews() throws Exception {
    LocalDateTime date = LocalDateTime.parse("2026-10-21T14:00:00");

    MenuItemReview review1 =
        MenuItemReview.builder()
            .id(1L)
            .itemId(10L)
            .reviewerEmail("a@ucsb.edu")
            .stars(5)
            .dateReviewed(date)
            .comments("Good food")
            .build();

    MenuItemReview review2 =
        MenuItemReview.builder()
            .id(2L)
            .itemId(11L)
            .reviewerEmail("b@ucsb.edu")
            .stars(4)
            .dateReviewed(date)
            .comments("Nice")
            .build();

    List<MenuItemReview> expectedReviews = Arrays.asList(review1, review2);

    when(menuItemReviewRepository.findAll()).thenReturn(expectedReviews);

    MvcResult response =
        mockMvc.perform(get("/api/MenuItemReview/all")).andExpect(status().isOk()).andReturn();

    verify(menuItemReviewRepository, times(1)).findAll();

    String expectedJson = mapper.writeValueAsString(expectedReviews);
    String responseString = response.getResponse().getContentAsString();

    assertEquals(expectedJson, responseString);
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/MenuItemReview/post")
                .param("itemId", "10")
                .param("reviewerEmail", "test@ucsb.edu")
                .param("stars", "5")
                .param("dateReviewed", "2026-10-21T14:00:00")
                .param("comments", "Good food")
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/MenuItemReview/post")
                .param("itemId", "10")
                .param("reviewerEmail", "test@ucsb.edu")
                .param("stars", "5")
                .param("dateReviewed", "2026-10-21T14:00:00")
                .param("comments", "Good food")
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_post_review() throws Exception {
    LocalDateTime date = LocalDateTime.parse("2026-10-21T14:00:00");

    MenuItemReview incomingReview =
        MenuItemReview.builder()
            .itemId(10L)
            .reviewerEmail("test@ucsb.edu")
            .stars(5)
            .dateReviewed(date)
            .comments("Good food")
            .build();

    MenuItemReview savedReview =
        MenuItemReview.builder()
            .id(42L)
            .itemId(10L)
            .reviewerEmail("test@ucsb.edu")
            .stars(5)
            .dateReviewed(date)
            .comments("Good food")
            .build();

    when(menuItemReviewRepository.save(eq(incomingReview))).thenReturn(savedReview);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/MenuItemReview/post")
                    .param("itemId", "10")
                    .param("reviewerEmail", "test@ucsb.edu")
                    .param("stars", "5")
                    .param("dateReviewed", "2026-10-21T14:00:00")
                    .param("comments", "Good food")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(menuItemReviewRepository, times(1)).save(incomingReview);

    String expectedJson = mapper.writeValueAsString(savedReview);
    String responseString = response.getResponse().getContentAsString();

    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_review_by_id() throws Exception {
    LocalDateTime date = LocalDateTime.parse("2026-10-21T14:00:00");

    MenuItemReview review =
        MenuItemReview.builder()
            .id(3L)
            .itemId(1L)
            .reviewerEmail("test@ucsb.edu")
            .stars(5)
            .dateReviewed(date)
            .comments("Good food")
            .build();

    when(menuItemReviewRepository.findById(3L)).thenReturn(Optional.of(review));

    MvcResult response =
        mockMvc
            .perform(get("/api/MenuItemReview").param("id", "3"))
            .andExpect(status().isOk())
            .andReturn();

    verify(menuItemReviewRepository, times(1)).findById(3L);

    String expectedJson = mapper.writeValueAsString(review);
    String responseString = response.getResponse().getContentAsString();

    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_cannot_get_nonexistent_review() throws Exception {

    when(menuItemReviewRepository.findById(123L)).thenReturn(Optional.empty());

    mockMvc
        .perform(get("/api/MenuItemReview").param("id", "123"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.type").value("EntityNotFoundException"))
        .andExpect(jsonPath("$.message").value("MenuItemReview with id 123 not found"));
    verify(menuItemReviewRepository, times(1)).findById(123L);
  }
}
