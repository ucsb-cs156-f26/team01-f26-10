package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = ArticlesController.class)
@Import(TestConfig.class)
public class ArticlesControllerTests extends ControllerTestCase {

  @MockitoBean ArticlesRepository articlesRepository;

  @MockitoBean UserRepository userRepository;

  // Authorization tests for /api/Articles/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/Articles/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    when(articlesRepository.findAll()).thenReturn(new ArrayList<>());

    MvcResult response =
        mockMvc.perform(get("/api/Articles/all")).andExpect(status().isOk()).andReturn();

    verify(articlesRepository, times(1)).findAll();
    assertEquals("[]", response.getResponse().getContentAsString());
  }

  // Tests for GET /api/Articles?id=123

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc.perform(get("/api/Articles").param("id", "123")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {
    // arrange
    Articles article =
        Articles.builder()
            .id(123L)
            .title("Testing Spring applications")
            .url("https://example.com/spring-testing")
            .explanation("An introduction to Spring testing")
            .email("student@ucsb.edu")
            .dateAdded(LocalDateTime.parse("2026-10-21T12:00:00"))
            .build();

    when(articlesRepository.findById(eq(123L))).thenReturn(Optional.of(article));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/Articles").param("id", "123"))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(articlesRepository, times(1)).findById(eq(123L));
    String expectedJson = mapper.writeValueAsString(article);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_gets_not_found_when_the_id_does_not_exist() throws Exception {
    // arrange
    when(articlesRepository.findById(eq(123L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/Articles").param("id", "123"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(articlesRepository, times(1)).findById(eq(123L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("Articles with id 123 not found", json.get("message"));
  }

  // Authorization tests for /api/Articles/post

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/Articles/post")
                .param("title", "Testing Spring applications")
                .param("url", "https://example.com/spring-testing")
                .param("explanation", "An introduction to Spring testing")
                .param("email", "student@ucsb.edu")
                .param("dateAdded", "2026-10-21T12:00:00")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/Articles/post")
                .param("title", "Testing Spring applications")
                .param("url", "https://example.com/spring-testing")
                .param("explanation", "An introduction to Spring testing")
                .param("email", "student@ucsb.edu")
                .param("dateAdded", "2026-10-21T12:00:00")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  // Tests for DELETE /api/Articles?id=123

  @Test
  public void logged_out_users_cannot_delete() throws Exception {
    mockMvc
        .perform(delete("/api/Articles").param("id", "123").with(csrf()))
        .andExpect(status().is(403));

    verify(articlesRepository, never()).delete(any());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_delete() throws Exception {
    mockMvc
        .perform(delete("/api/Articles").param("id", "123").with(csrf()))
        .andExpect(status().is(403));

    verify(articlesRepository, never()).delete(any());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_an_existing_article() throws Exception {
    // arrange
    Articles article =
        Articles.builder()
            .id(123L)
            .title("Testing Spring applications")
            .url("https://example.com/spring-testing")
            .explanation("An introduction to Spring testing")
            .email("student@ucsb.edu")
            .dateAdded(LocalDateTime.parse("2026-10-21T12:00:00"))
            .build();

    when(articlesRepository.findById(eq(123L))).thenReturn(Optional.of(article));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/Articles").param("id", "123").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(articlesRepository, times(1)).findById(eq(123L));
    verify(articlesRepository, times(1)).delete(eq(article));
    Map<String, Object> json = responseToJson(response);
    assertEquals("record 123 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_delete_an_article_that_does_not_exist() throws Exception {
    // arrange
    when(articlesRepository.findById(eq(123L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/Articles").param("id", "123").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(articlesRepository, times(1)).findById(eq(123L));
    verify(articlesRepository, never()).delete(any());
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("Articles with id 123 not found", json.get("message"));
  }

  // Tests for PUT /api/Articles?id=123

  @Test
  public void logged_out_users_cannot_put() throws Exception {
    mockMvc
        .perform(
            put("/api/Articles")
                .param("id", "123")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(csrf()))
        .andExpect(status().is(403));

    verify(articlesRepository, never()).save(any());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_put() throws Exception {
    mockMvc
        .perform(
            put("/api/Articles")
                .param("id", "123")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(csrf()))
        .andExpect(status().is(403));

    verify(articlesRepository, never()).save(any());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_article_without_changing_its_id() throws Exception {
    // arrange
    Articles originalArticle =
        Articles.builder()
            .id(123L)
            .title("Testing Spring applications")
            .url("https://example.com/spring-testing")
            .explanation("An introduction to Spring testing")
            .email("student@ucsb.edu")
            .dateAdded(LocalDateTime.parse("2026-10-21T12:00:00"))
            .build();

    Articles incomingArticle =
        Articles.builder()
            .id(999L)
            .title("Getting started with React")
            .url("https://example.com/react")
            .explanation("A guide to React components")
            .email("another@ucsb.edu")
            .dateAdded(LocalDateTime.parse("2026-10-22T13:00:00"))
            .build();

    Articles expectedArticle =
        Articles.builder()
            .id(123L)
            .title("Getting started with React")
            .url("https://example.com/react")
            .explanation("A guide to React components")
            .email("another@ucsb.edu")
            .dateAdded(LocalDateTime.parse("2026-10-22T13:00:00"))
            .build();

    when(articlesRepository.findById(eq(123L))).thenReturn(Optional.of(originalArticle));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/Articles")
                    .param("id", "123")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(mapper.writeValueAsString(incomingArticle))
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(articlesRepository, times(1)).findById(eq(123L));
    verify(articlesRepository, times(1)).save(eq(expectedArticle));
    String expectedJson = mapper.writeValueAsString(expectedArticle);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_an_article_that_does_not_exist() throws Exception {
    // arrange
    Articles incomingArticle =
        Articles.builder()
            .title("Getting started with React")
            .url("https://example.com/react")
            .explanation("A guide to React components")
            .email("another@ucsb.edu")
            .dateAdded(LocalDateTime.parse("2026-10-22T13:00:00"))
            .build();

    when(articlesRepository.findById(eq(123L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/Articles")
                    .param("id", "123")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(mapper.writeValueAsString(incomingArticle))
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(articlesRepository, times(1)).findById(eq(123L));
    verify(articlesRepository, never()).save(any());
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("Articles with id 123 not found", json.get("message"));
  }

  // Tests with mocks for database actions

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_articles() throws Exception {
    // arrange
    Articles article1 =
        Articles.builder()
            .id(1L)
            .title("Testing Spring applications")
            .url("https://example.com/spring-testing")
            .explanation("An introduction to Spring testing")
            .email("student@ucsb.edu")
            .dateAdded(LocalDateTime.parse("2026-10-21T12:00:00"))
            .build();

    Articles article2 =
        Articles.builder()
            .id(2L)
            .title("Getting started with React")
            .url("https://example.com/react")
            .explanation("A guide to React components")
            .email("another@ucsb.edu")
            .dateAdded(LocalDateTime.parse("2026-10-22T13:00:00"))
            .build();

    ArrayList<Articles> expectedArticles = new ArrayList<>();
    expectedArticles.addAll(Arrays.asList(article1, article2));

    when(articlesRepository.findAll()).thenReturn(expectedArticles);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/Articles/all")).andExpect(status().isOk()).andReturn();

    // assert
    verify(articlesRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedArticles);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_article() throws Exception {
    // arrange
    LocalDateTime dateAdded = LocalDateTime.parse("2026-10-21T12:00:00");

    Articles article =
        Articles.builder()
            .title("Testing Spring applications")
            .url("https://example.com/spring-testing")
            .explanation("An introduction to Spring testing")
            .email("student@ucsb.edu")
            .dateAdded(dateAdded)
            .build();

    Articles savedArticle =
        Articles.builder()
            .id(7L)
            .title("Testing Spring applications")
            .url("https://example.com/spring-testing")
            .explanation("An introduction to Spring testing")
            .email("student@ucsb.edu")
            .dateAdded(dateAdded)
            .build();

    when(articlesRepository.save(eq(article))).thenReturn(savedArticle);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/Articles/post")
                    .param("title", "Testing Spring applications")
                    .param("url", "https://example.com/spring-testing")
                    .param("explanation", "An introduction to Spring testing")
                    .param("email", "student@ucsb.edu")
                    .param("dateAdded", "2026-10-21T12:00:00")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(articlesRepository, times(1)).save(eq(article));
    String expectedJson = mapper.writeValueAsString(savedArticle);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }
}
