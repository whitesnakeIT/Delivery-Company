package com.kapusniak.tomasz.jdbc.mapper;

import com.kapusniak.tomasz.entity.CourierEntity;
import com.kapusniak.tomasz.openapi.model.CourierCompany;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Component
public class CourierRowMapper implements RowMapper<CourierEntity> {

    @Override
    public CourierEntity mapRow(ResultSet rs, int rowNum) throws SQLException {

        CourierEntity courier = new CourierEntity();
        courier.setId(rs.getLong("COURIER_ID"));
        courier.setFirstName(rs.getString("FIRST_NAME"));
        courier.setLastName(rs.getString("LAST_NAME"));
        courier.setCourierCompany(CourierCompany.valueOf(rs.getString("COURIER_COMPANY")));
        courier.setUuid(UUID.fromString(rs.getString("UUID")));
        courier.setVersion(rs.getLong("VERSION"));


        return courier;
    }
}
