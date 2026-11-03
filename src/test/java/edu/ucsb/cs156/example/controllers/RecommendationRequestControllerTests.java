package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
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

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {

  @MockitoBean RecommendationRequestRepository recommendationRequestRepository;

  @MockitoBean UserRepository userRepository;

  private RecommendationRequest sample1() {
    return RecommendationRequest.builder()
        .id(1L)
        .requesterEmail("cgaucho@ucsb.edu")
        .professorEmail("phtcon@ucsb.edu")
        .explanation("BS/MS program")
        .dateRequested(LocalDateTime.parse("2022-04-20T00:00:00"))
        .dateNeeded(LocalDateTime.parse("2022-05-01T00:00:00"))
        .done(false)
        .build();
  }

  private RecommendationRequest sample2() {
    return RecommendationRequest.builder()
        .id(2L)
        .requesterEmail("ldelplaya@ucsb.edu")
        .professorEmail("richert@ucsb.edu")
        .explanation("PhD CS Stanford")
        .dateRequested(LocalDateTime.parse("2022-05-20T00:00:00"))
        .dateNeeded(LocalDateTime.parse("2022-11-15T00:00:00"))
        .done(true)
        .build();
  }

  // ---------- GET /all ----------

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/recommendationrequests/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/recommendationrequests/all")).andExpect(status().is(200));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_recommendation_requests() throws Exception {
    ArrayList<RecommendationRequest> expected = new ArrayList<>();
    expected.addAll(Arrays.asList(sample1(), sample2()));

    when(recommendationRequestRepository.findAll()).thenReturn(expected);

    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequests/all"))
            .andExpect(status().isOk())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findAll();
    assertEquals(mapper.writeValueAsString(expected), response.getResponse().getContentAsString());
  }

  // ---------- POST ----------

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/recommendationrequests/post")
                .param("requesterEmail", "cgaucho@ucsb.edu")
                .param("professorEmail", "phtcon@ucsb.edu")
                .param("explanation", "BS/MS program")
                .param("dateRequested", "2022-04-20T00:00:00")
                .param("dateNeeded", "2022-05-01T00:00:00")
                .param("done", "false")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/recommendationrequests/post")
                .param("requesterEmail", "cgaucho@ucsb.edu")
                .param("professorEmail", "phtcon@ucsb.edu")
                .param("explanation", "BS/MS program")
                .param("dateRequested", "2022-04-20T00:00:00")
                .param("dateNeeded", "2022-05-01T00:00:00")
                .param("done", "false")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_recommendation_request() throws Exception {
    RecommendationRequest toSave =
        RecommendationRequest.builder()
            .requesterEmail("ldelplaya@ucsb.edu")
            .professorEmail("richert@ucsb.edu")
            .explanation("PhD CS Stanford")
            .dateRequested(LocalDateTime.parse("2022-05-20T00:00:00"))
            .dateNeeded(LocalDateTime.parse("2022-11-15T00:00:00"))
            .done(true)
            .build();

    RecommendationRequest saved = sample2();

    when(recommendationRequestRepository.save(eq(toSave))).thenReturn(saved);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/recommendationrequests/post")
                    .param("requesterEmail", "ldelplaya@ucsb.edu")
                    .param("professorEmail", "richert@ucsb.edu")
                    .param("explanation", "PhD CS Stanford")
                    .param("dateRequested", "2022-05-20T00:00:00")
                    .param("dateNeeded", "2022-11-15T00:00:00")
                    .param("done", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).save(eq(toSave));
    assertEquals(mapper.writeValueAsString(saved), response.getResponse().getContentAsString());
  }

  // ---------- GET by id ----------

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/recommendationrequests").param("id", "7"))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {
    RecommendationRequest rr = sample1();
    when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.of(rr));

    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequests").param("id", "7"))
            .andExpect(status().isOk())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findById(eq(7L));
    assertEquals(mapper.writeValueAsString(rr), response.getResponse().getContentAsString());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_gets_404_when_id_does_not_exist() throws Exception {
    when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequests").param("id", "7"))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findById(eq(7L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("RecommendationRequest with id 7 not found", json.get("message"));
  }

  // ---------- DELETE ----------

  @Test
  public void logged_out_users_cannot_delete() throws Exception {
    mockMvc
        .perform(delete("/api/recommendationrequests").param("id", "15").with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_users_cannot_delete() throws Exception {
    mockMvc
        .perform(delete("/api/recommendationrequests").param("id", "15").with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_a_recommendation_request() throws Exception {
    RecommendationRequest rr = sample1();
    when(recommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.of(rr));

    MvcResult response =
        mockMvc
            .perform(delete("/api/recommendationrequests").param("id", "15").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findById(15L);
    verify(recommendationRequestRepository, times(1)).delete(eq(rr));

    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 15 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_non_existant_recommendation_request_and_gets_404()
      throws Exception {
    when(recommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(delete("/api/recommendationrequests").param("id", "15").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findById(15L);
    verify(recommendationRequestRepository, never()).delete(any(RecommendationRequest.class));
    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 15 not found", json.get("message"));
  }

  // ---------- PUT ----------

  @Test
  public void logged_out_users_cannot_put() throws Exception {
    mockMvc
        .perform(
            put("/api/recommendationrequests")
                .param("id", "67")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(mapper.writeValueAsString(sample1()))
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_users_cannot_put() throws Exception {
    mockMvc
        .perform(
            put("/api/recommendationrequests")
                .param("id", "67")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(mapper.writeValueAsString(sample1()))
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_recommendation_request() throws Exception {
    RecommendationRequest orig = sample1();
    orig.setId(67L);

    // every field differs from orig, and done flips false -> true
    RecommendationRequest edited =
        RecommendationRequest.builder()
            .id(67L)
            .requesterEmail("alu@ucsb.edu")
            .professorEmail("richert@ucsb.edu")
            .explanation("PhD CE Cal Tech")
            .dateRequested(LocalDateTime.parse("2022-05-21T00:00:00"))
            .dateNeeded(LocalDateTime.parse("2022-11-16T00:00:00"))
            .done(true)
            .build();

    String requestBody = mapper.writeValueAsString(edited);
    when(recommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.of(orig));

    MvcResult response =
        mockMvc
            .perform(
                put("/api/recommendationrequests")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findById(67L);
    verify(recommendationRequestRepository, times(1)).save(eq(edited));
    assertEquals(requestBody, response.getResponse().getContentAsString());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_change_done_from_true_to_false() throws Exception {
    RecommendationRequest orig = sample2();
    RecommendationRequest edited = sample2();
    edited.setDone(false);

    when(recommendationRequestRepository.findById(eq(2L))).thenReturn(Optional.of(orig));

    mockMvc
        .perform(
            put("/api/recommendationrequests")
                .param("id", "2")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(mapper.writeValueAsString(edited))
                .with(csrf()))
        .andExpect(status().isOk());

    verify(recommendationRequestRepository, times(1)).save(eq(edited));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_recommendation_request_that_does_not_exist() throws Exception {
    RecommendationRequest edited = sample1();
    edited.setId(67L);

    when(recommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(
                put("/api/recommendationrequests")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(mapper.writeValueAsString(edited))
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findById(67L);
    verify(recommendationRequestRepository, never()).save(any(RecommendationRequest.class));
    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 67 not found", json.get("message"));
  }
}
