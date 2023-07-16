package com.kapusniak.tomasz.jdbc.mapper;

import com.kapusniak.tomasz.entity.CustomerEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Component
public class CustomerRowMapper implements RowMapper<CustomerEntity> {

    @Override
    public CustomerEntity mapRow(ResultSet rs, int rowNum) throws SQLException {

        CustomerEntity customer = new CustomerEntity();
        customer.setId(rs.getLong("CUSTOMER_ID"));
        customer.setFirstName(rs.getString("FIRST_NAME"));
        customer.setLastName(rs.getString("LAST_NAME"));
        customer.setEmail(rs.getString("EMAIL"));
        customer.setUuid(UUID.fromString(rs.getString("UUID")));
        customer.setVersion(rs.getLong("VERSION"));


        return customer;
    }
}