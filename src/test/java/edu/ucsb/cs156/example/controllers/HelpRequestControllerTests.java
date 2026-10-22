package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = HelpRequestController.class)
@Import(TestConfig.class)
public class HelpRequestControllerTests extends ControllerTestCase {

  @MockitoBean HelpRequestRepository helpRequestRepository;

  @MockitoBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/helprequests/all")).andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/helprequests/all")).andExpect(status().isOk());
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc.perform(get("/api/helprequests").param("id", "7")).andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_by_id_when_id_exists() throws Exception {
    HelpRequest helpRequest =
        HelpRequest.builder()
            .id(7L)
            .requesterEmail("student@ucsb.edu")
            .teamId("f26-10")
            .tableOrBreakoutRoom("7")
            .requestTime(LocalDateTime.parse("2026-10-21T15:30:00"))
            .explanation("Need help with Dokku")
            .solved(false)
            .build();

    when(helpRequestRepository.findById(eq(7L))).thenReturn(Optional.of(helpRequest));

    MvcResult response =
        mockMvc
            .perform(get("/api/helprequests").param("id", "7"))
            .andExpect(status().isOk())
            .andReturn();

    verify(helpRequestRepository, times(1)).findById(eq(7L));

    assertEquals(
        mapper.writeValueAsString(helpRequest), response.getResponse().getContentAsString());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_cannot_get_by_id_when_id_does_not_exist() throws Exception {
    when(helpRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(get("/api/helprequests").param("id", "7"))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(helpRequestRepository, times(1)).findById(eq(7L));

    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("HelpRequest with id 7 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN"})
  @Test
  public void admin_can_edit_existing_help_request() throws Exception {

    HelpRequest original =
        HelpRequest.builder()
            .id(1L)
            .requesterEmail("student@ucsb.edu")
            .teamId("f26-10")
            .tableOrBreakoutRoom("7")
            .requestTime(LocalDateTime.parse("2026-10-21T15:30:00"))
            .explanation("Need help with Java")
            .solved(false)
            .build();

    HelpRequest edited =
        HelpRequest.builder()
            .id(1L)
            .requesterEmail("updated@ucsb.edu")
            .teamId("f26-11")
            .tableOrBreakoutRoom("12")
            .requestTime(LocalDateTime.parse("2026-10-22T16:00:00"))
            .explanation("TA helped resolve the Java issue")
            .solved(true)
            .build();

    when(helpRequestRepository.findById(eq(1L))).thenReturn(Optional.of(original));

    MvcResult response =
        mockMvc
            .perform(
                put("/api/helprequests")
                    .param("id", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(edited))
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(helpRequestRepository, times(1)).findById(eq(1L));
    verify(helpRequestRepository, times(1)).save(eq(edited));

    assertEquals(mapper.writeValueAsString(edited), response.getResponse().getContentAsString());
  }

  @WithMockUser(roles = {"ADMIN"})
  @Test
  public void admin_cannot_edit_nonexistent_help_request() throws Exception {

    HelpRequest incoming =
        HelpRequest.builder()
            .id(999999L)
            .requesterEmail("student@ucsb.edu")
            .teamId("f26-10")
            .tableOrBreakoutRoom("7")
            .requestTime(LocalDateTime.parse("2026-10-21T15:30:00"))
            .explanation("Need help with Java")
            .solved(false)
            .build();

    when(helpRequestRepository.findById(eq(999999L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(
                put("/api/helprequests")
                    .param("id", "999999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(incoming))
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(helpRequestRepository, times(1)).findById(eq(999999L));
    verify(helpRequestRepository, never()).save(any(HelpRequest.class));

    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("HelpRequest with id 999999 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN"})
  @Test
  public void admin_can_change_solved_from_true_to_false() throws Exception {
    HelpRequest original =
        HelpRequest.builder()
            .id(1L)
            .requesterEmail("student@ucsb.edu")
            .teamId("f26-10")
            .tableOrBreakoutRoom("7")
            .requestTime(LocalDateTime.parse("2026-10-21T15:30:00"))
            .explanation("Need more help")
            .solved(true)
            .build();

    HelpRequest incoming =
        HelpRequest.builder()
            .id(1L)
            .requesterEmail("student@ucsb.edu")
            .teamId("f26-10")
            .tableOrBreakoutRoom("7")
            .requestTime(LocalDateTime.parse("2026-10-21T15:30:00"))
            .explanation("Need more help")
            .solved(false)
            .build();

    when(helpRequestRepository.findById(eq(1L))).thenReturn(Optional.of(original));

    mockMvc
        .perform(
            put("/api/helprequests")
                .param("id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(incoming))
                .with(csrf()))
        .andExpect(status().isOk());

    verify(helpRequestRepository).save(eq(incoming));
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/helprequests/post")
                .param("requesterEmail", "student@ucsb.edu")
                .param("teamId", "f26-10")
                .param("tableOrBreakoutRoom", "7")
                .param("requestTime", "2026-10-21T15:30:00")
                .param("explanation", "Need help with Dokku")
                .param("solved", "false")
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/helprequests/post")
                .param("requesterEmail", "student@ucsb.edu")
                .param("teamId", "f26-10")
                .param("tableOrBreakoutRoom", "7")
                .param("requestTime", "2026-10-21T15:30:00")
                .param("explanation", "Need help with Dokku")
                .param("solved", "false")
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_help_requests() throws Exception {
    HelpRequest first =
        HelpRequest.builder()
            .id(1L)
            .requesterEmail("student@ucsb.edu")
            .teamId("f26-10")
            .tableOrBreakoutRoom("7")
            .requestTime(LocalDateTime.parse("2026-10-21T15:30:00"))
            .explanation("Need help with Dokku")
            .solved(false)
            .build();

    HelpRequest second =
        HelpRequest.builder()
            .id(2L)
            .requesterEmail("another@ucsb.edu")
            .teamId("f26-10")
            .tableOrBreakoutRoom("8")
            .requestTime(LocalDateTime.parse("2026-10-21T16:00:00"))
            .explanation("Need help with Git")
            .solved(true)
            .build();

    List<HelpRequest> expectedRequests = Arrays.asList(first, second);

    when(helpRequestRepository.findAll()).thenReturn(expectedRequests);

    MvcResult response =
        mockMvc.perform(get("/api/helprequests/all")).andExpect(status().isOk()).andReturn();

    verify(helpRequestRepository, times(1)).findAll();
    assertEquals(
        mapper.writeValueAsString(expectedRequests), response.getResponse().getContentAsString());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_post_a_new_help_request() throws Exception {
    HelpRequest request =
        HelpRequest.builder()
            .requesterEmail("student@ucsb.edu")
            .teamId("f26-10")
            .tableOrBreakoutRoom("7")
            .requestTime(LocalDateTime.parse("2026-10-21T15:30:00"))
            .explanation("Need help with Dokku")
            .solved(true)
            .build();

    HelpRequest savedRequest =
        HelpRequest.builder()
            .id(1L)
            .requesterEmail("student@ucsb.edu")
            .teamId("f26-10")
            .tableOrBreakoutRoom("7")
            .requestTime(LocalDateTime.parse("2026-10-21T15:30:00"))
            .explanation("Need help with Dokku")
            .solved(true)
            .build();

    when(helpRequestRepository.save(eq(request))).thenReturn(savedRequest);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/helprequests/post")
                    .param("requesterEmail", "student@ucsb.edu")
                    .param("teamId", "f26-10")
                    .param("tableOrBreakoutRoom", "7")
                    .param("requestTime", "2026-10-21T15:30:00")
                    .param("explanation", "Need help with Dokku")
                    .param("solved", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(helpRequestRepository, times(1)).save(request);
    assertEquals(
        mapper.writeValueAsString(savedRequest), response.getResponse().getContentAsString());
  }
}
