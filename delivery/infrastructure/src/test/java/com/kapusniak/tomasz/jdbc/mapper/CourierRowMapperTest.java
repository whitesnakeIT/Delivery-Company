package com.kapusniak.tomasz.jdbc.mapper;

import com.kapusniak.tomasz.entity.CourierEntity;
import com.kapusniak.tomasz.openapi.model.CourierCompany;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("jdbc")
@TestPropertySource("classpath:application-jdbc-test.properties")
class CourierRowMapperTest {

    @Mock
    private ResultSet resultSet;

    @Autowired
    private CourierRowMapper courierRowMapper;

    @BeforeEach
    void setUp() throws SQLException {
        when(resultSet.getLong("COURIER_ID")).thenReturn(1L);
        when(resultSet.getString("FIRST_NAME")).thenReturn("testFirstName");
        when(resultSet.getString("LAST_NAME")).thenReturn("testLastName");
        when(resultSet.getString("COURIER_COMPANY")).thenReturn("DPD");
        when(resultSet.getString("UUID")).thenReturn("fe362772-17c3-4547-b559-ceb13e164e6f");
        when(resultSet.getLong("VERSION")).thenReturn(0L);
    }

    @Test
    @DisplayName("should correctly map resultSet for Courier object")
    void mapRow() throws SQLException {

        // when
        CourierEntity courier = courierRowMapper.mapRow(resultSet, 1);

        // then
        assertNotNull(courier);
        assertEquals(1L, courier.getId());
        assertEquals("testFirstName", courier.getFirstName());
        assertEquals("testLastName", courier.getLastName());
        assertEquals(CourierCompany.DPD, courier.getCourierCompany());
        assertEquals("fe362772-17c3-4547-b559-ceb13e164e6f", courier.getUuid().toString());
        assertEquals(0L, courier.getVersion());
    }
}