package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
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
