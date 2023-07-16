package com.kapusniak.tomasz.jdbc.mapper;

import com.kapusniak.tomasz.entity.OrderEntity;
import com.kapusniak.tomasz.openapi.model.PackageSize;
import com.kapusniak.tomasz.openapi.model.PackageType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Component
public class OrderRowMapper implements RowMapper<OrderEntity> {

    @Override
    public OrderEntity mapRow(ResultSet rs, int rowNum) throws SQLException {

        OrderEntity order = new OrderEntity();
        order.setId(rs.getLong("ORDER_ID"));
        order.setPackageType(PackageType.valueOf(rs.getString("PACKAGE_TYPE")));
        order.setPackageSize(PackageSize.valueOf(rs.getString("PACKAGE_SIZE")));
        order.setSenderAddress(rs.getString("SENDER_ADDRESS"));
        order.setReceiverAddress(rs.getString("RECEIVER_ADDRESS"));
        order.setPreferredDeliveryDate(rs.getDate("PREFERRED_DELIVERY_DATE").toLocalDate());
        order.setUuid(UUID.fromString(rs.getString("UUID")));
        order.setVersion(rs.getLong("VERSION"));

        return order;
    }
}
