package com.kapusniak.tomasz.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kapusniak.tomasz.openapi.model.Courier;
import com.kapusniak.tomasz.openapi.model.CourierCompany;
import com.kapusniak.tomasz.service.CourierService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CourierControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourierService courierService;

    public Courier getCourier() {
        Courier courier1 = new Courier();
        courier1.setId(1L);
        courier1.setFirstName("John");
        courier1.setLastName("Wick");
        courier1.setCourierCompany(CourierCompany.DHL);
        courier1.setUuid(UUID.randomUUID());

        return courier1;
    }

    public List<Courier> getCourierList() {
        List<Courier> couriers = new ArrayList<>();
        Courier courier1 = new Courier();
        courier1.setId(1L);
        courier1.setFirstName("John");
        Courier courier2 = new Courier();
        courier2.setId(2L);
        courier2.setFirstName("Tom");
        couriers.add(courier1);
        couriers.add(courier2);

        return couriers;
    }

    @Test
    @DisplayName("should correctly return list of couriers")
    public void getAllCouriers() throws Exception {
        // given
        List<Courier> couriers = getCourierList();
        when(courierService.findAll()).thenReturn(couriers);

        // when
        mockMvc.perform(get("/api/v1/couriers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].firstName").value("Tom"));

        // then
        verify(courierService,
                times(1))
                .findAll();
        verifyNoMoreInteractions(courierService);
    }

    @Test
    @DisplayName("should correctly return courier based on courier uuid")
    public void getCourierByUuid() throws Exception {
        // given
        Courier courier = getCourier();
        UUID courierUuid = courier.getUuid();

        when(courierService.findByUuid(any()))
                .thenReturn(courier);

        // when
        mockMvc.perform(get("/api/v1/couriers/" + courierUuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"));

        // then
        verify(courierService,
                times(1))
                .findByUuid(courierUuid);
        verifyNoMoreInteractions(courierService);
    }

    @Test
    @DisplayName("should return created courier")
    public void createCourier() throws Exception {
        // given
        Courier courier = getCourier();
        when(courierService
                .save(any(Courier.class)))
                .thenReturn(courier);

        // when
        mockMvc.perform(post("/api/v1/couriers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courier)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"));

        // then
        verify(courierService,
                times(1))
                .save(any(Courier.class));
        verifyNoMoreInteractions(courierService);
    }

    @Test
    @DisplayName("should return updated courier")
    public void updateCourier() throws Exception {
        // given
        Courier courier = getCourier();
        UUID courierUuid = courier.getUuid();

        when(courierService.update(any(UUID.class),
                any(Courier.class))).thenReturn(courier);

        // when
        mockMvc.perform(put("/api/v1/couriers/" + courierUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courier)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"));

        // then
        verify(courierService,
                times(1))
                .update(eq(courierUuid), any(Courier.class));
        verifyNoMoreInteractions(courierService);
    }

    @Test
    @DisplayName("should return no content after deleting courier")
    public void deleteCourier() throws Exception {
        // given
        Courier courier = getCourier();
        UUID courierUuid = courier.getUuid();

        // when
        mockMvc.perform(delete("/api/v1/couriers/" + courierUuid))
                .andExpect(status().isNoContent());

        // then
        verify(courierService,
                times(1))
                .delete(courierUuid);
        verifyNoMoreInteractions(courierService);
    }
}