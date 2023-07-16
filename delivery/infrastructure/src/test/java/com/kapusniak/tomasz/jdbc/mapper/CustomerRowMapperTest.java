package com.kapusniak.tomasz.jdbc.mapper;

import com.kapusniak.tomasz.entity.CustomerEntity;
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
class CustomerRowMapperTest {

    @Mock
    private ResultSet resultSet;

    @Autowired
    private CustomerRowMapper customerRowMapper;

    @BeforeEach
    void setUp() throws SQLException {
        when(resultSet.getLong("CUSTOMER_ID")).thenReturn(1L);
        when(resultSet.getString("FIRST_NAME")).thenReturn("John");
        when(resultSet.getString("LAST_NAME")).thenReturn("Doe");
        when(resultSet.getString("EMAIL")).thenReturn("john.doe@example.com");
        when(resultSet.getString("UUID")).thenReturn("28f60dc1-993a-4d08-ac54-850a1fefb6a3");
        when(resultSet.getLong("VERSION")).thenReturn(0L);
    }

    @Test
    @DisplayName("should correctly map resultSet for Customer object")
    void mapRow() throws SQLException {

        // when
        CustomerEntity customer = customerRowMapper.mapRow(resultSet, 1);

        // then
        assertNotNull(customer);
        assertEquals(1L, customer.getId());
        assertEquals("John", customer.getFirstName());
        assertEquals("Doe", customer.getLastName());
        assertEquals("john.doe@example.com", customer.getEmail());
        assertEquals("28f60dc1-993a-4d08-ac54-850a1fefb6a3", customer.getUuid().toString());
        assertEquals(0L, customer.getVersion());
    }
}