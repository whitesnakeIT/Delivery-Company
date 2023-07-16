package com.kapusniak.tomasz.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kapusniak.tomasz.openapi.model.Tracking;
import com.kapusniak.tomasz.service.TrackingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureDataJpa
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@TestPropertySource("classpath:application-integration-test.properties")
@SqlGroup(
        @Sql(
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
                scripts = {
                        "classpath:integration-test-scripts/cleanup.sql",
                        "classpath:integration-test-scripts/insert-data.sql"})
)
@WithMockUser(authorities = "ADMIN")
public class TrackingTest {

    private static final UUID TRACKING_UUID_1 = UUID.fromString("97e37668-b355-4ecd-83be-dbc9cf56d8c0");

    @Autowired
    private TrackingService trackingService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("should return http status 403 unauthorized when user is anonymous" +
            " (test shouldn't return 401 cause of RFC 7231)")
    @WithAnonymousUser
    public void getAllTrackingAnonymous() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/api/v1/tracking"));

        // then
        result.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should return http status 403 forbidden when user not have ADMIN authority")
    @WithMockUser(authorities = "USER")
    public void getAllTrackingForbidden() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/api/v1/tracking"));

        // then
        result.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should correctly get Tracking from database and verify" +
            " properties with Tracking from controller method")
    void getTrackingExisting() throws Exception {
        // given
        UUID trackingUuid = TRACKING_UUID_1;
        Tracking tracking = trackingService.findByUuid(trackingUuid);

        // when
        ResultActions result =
                mockMvc.perform(get(
                        "/api/v1/tracking/" + trackingUuid));


        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(tracking.getUuid().toString()));
    }

    @Test
    @DisplayName("should return ResponseEntity<ApiError> with correct json data" +
            " when provided tracking uuid is not existing in database for searching")
    void getTrackingNonExisting() throws Exception {
        // given
        UUID trackingUuid = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(get(
                "/api/v1/tracking/" + trackingUuid));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("httpStatus", equalTo(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("timestamp", notNullValue()))
                .andExpect(jsonPath("message", equalTo("Searching for tracking failed. Unrecognized uuid " + trackingUuid)));
    }

    @Test
    @DisplayName("should correctly return list Tracking from database after executing" +
            " method from controller")
    void getAllTracking() throws Exception {
        // given
        List<Tracking> trackingList = trackingService.findAll();

        // when
        ResultActions result =
                mockMvc.perform(get(
                        "/api/v1/tracking"));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].uuid")
                        .value(trackingList.get(0).getUuid().toString()))
                .andExpect(jsonPath("$[1].uuid")
                        .value(trackingList.get(1).getUuid().toString()));


    }

    @Sql("classpath:integration-test-scripts/cleanup.sql")
    @Test
    @DisplayName("should correctly return empty list of Tracking when table is empty" +
            "after executing method from controller")
    void getAllTrackingEmpty() throws Exception {
        // when
        ResultActions result =
                mockMvc.perform(get(
                        "/api/v1/tracking"));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().json("[]"));

    }

    @Test
    @DisplayName("should save Tracking to database after executing method" +
            " from controller")
    void createTracking() throws Exception {
        // given
        Tracking tracking = new Tracking();
        tracking.setLocalization("localization");

        // when
        ResultActions result = mockMvc
                .perform(post("/api/v1/tracking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tracking)));

        // then
        result.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.localization").value(tracking.getLocalization()));
    }

    @Test
    @DisplayName("should delete Tracking from database after executing" +
            " method from controller")
    void deleteTrackingExisting() throws Exception {
        // given
        UUID trackingUuid = TRACKING_UUID_1;
        int sizeBeforeDeleting = trackingService.findAll().size();

        // when
        ResultActions result =
                mockMvc.perform(delete(
                        "/api/v1/tracking/" + trackingUuid));

        // then
        result.andExpect(status().isNoContent());

        // and
        int sizeAfterDeleting = trackingService.findAll().size();
        assertThat(sizeAfterDeleting).isEqualTo(sizeBeforeDeleting - 1);

    }

    @Test
    @DisplayName("should return ResponseEntity<ApiError> with correct json data" +
            " when provided tracking uuid is not existing in database for deleting")
    void deleteTrackingNonExisting() throws Exception {
        // given
        UUID trackingUuid = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(delete(
                "/api/v1/tracking/" + trackingUuid));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("httpStatus", equalTo(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("timestamp", notNullValue()))
                .andExpect(jsonPath("message", equalTo("Searching for tracking failed. Unrecognized uuid " + trackingUuid)));
    }

    @Test
    @DisplayName("should save edited Tracking to database after executing method" +
            " from controller")
    void updateTracking() throws Exception {
        // given
        UUID trackingUuid = TRACKING_UUID_1;
        Tracking trackingBeforeEdit = trackingService.findByUuid(trackingUuid);
        String newLocalization = "newLocalization";

        // and
        trackingBeforeEdit.setLocalization(newLocalization);

        // when
        ResultActions result = mockMvc
                .perform(put("/api/v1/tracking/" + trackingUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trackingBeforeEdit)));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(trackingBeforeEdit.getUuid().toString()))
                .andExpect(jsonPath("$.localization").value(newLocalization));

        // and
        Tracking trackingAfterEdit = trackingService.findByUuid(trackingUuid);
        assertThat(trackingAfterEdit.getLocalization()).isEqualTo(newLocalization);
    }
}