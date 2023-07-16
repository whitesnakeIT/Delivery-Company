package com.kapusniak.tomasz.jdbc.mapper;

import com.kapusniak.tomasz.entity.OrderEntity;
import com.kapusniak.tomasz.openapi.model.PackageSize;
import com.kapusniak.tomasz.openapi.model.PackageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("jdbc")
@TestPropertySource("classpath:application-jdbc-test.properties")
class OrderRowMapperTest {

    @Mock
    private ResultSet resultSet;

    @Autowired
    private OrderRowMapper orderRowMapper;

    @BeforeEach
    void setUp() throws SQLException {
        when(resultSet.getLong("ORDER_ID")).thenReturn(1L);
        when(resultSet.getString("SENDER_ADDRESS")).thenReturn("testSenderAddress");
        when(resultSet.getString("RECEIVER_ADDRESS")).thenReturn("testReceiverAddress");
        when(resultSet.getString("PACKAGE_TYPE")).thenReturn("DOCUMENT");
        when(resultSet.getString("PACKAGE_SIZE")).thenReturn("SMALL");
        when(resultSet.getDate("PREFERRED_DELIVERY_DATE")).thenReturn(Date.valueOf(LocalDate.of(2023, 5, 4)));
        when(resultSet.getString("UUID")).thenReturn("29755321-c483-4a12-9f64-30a132038b70");
        when(resultSet.getLong("VERSION")).thenReturn(0L);
    }

    @Test
    @DisplayName("should correctly map resultSet for Customer object")
    void mapRow() throws SQLException {

        // when
        OrderEntity order = orderRowMapper.mapRow(resultSet, 1);

        // then
        assertNotNull(order);
        assertEquals(1L, order.getId());
        assertEquals("testSenderAddress", order.getSenderAddress());
        assertEquals("testReceiverAddress", order.getReceiverAddress());
        assertEquals(PackageType.DOCUMENT, order.getPackageType());
        assertEquals(PackageSize.SMALL, order.getPackageSize());
        assertEquals(LocalDate.of(2023, 5, 4), order.getPreferredDeliveryDate());
        assertEquals("29755321-c483-4a12-9f64-30a132038b70", order.getUuid().toString());
        assertEquals(0L, order.getVersion());
    }
}