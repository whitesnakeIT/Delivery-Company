package com.kapusniak.tomasz.service;

import com.kapusniak.tomasz.entity.BaseEntity;
import com.kapusniak.tomasz.openapi.model.Order;
import com.kapusniak.tomasz.service.model.OrderService;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVPrinter;
import org.hibernate.metamodel.MappingMetamodel;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final EntityManagerFactory entityManagerFactory;

    private final OrderService orderService;


    public <T extends BaseEntity> String[] getTableHeaders(Class<T> clazz) {
        MappingMetamodel metamodel =
                (MappingMetamodel) entityManagerFactory.getMetamodel();
        AbstractEntityPersister persister =
                (AbstractEntityPersister) metamodel.getEntityDescriptor(clazz.getName());

        List<String> columnNamesList = extractColumnNamesFromProperties(persister);
        changeIdFieldInForeignKeysToUuid(columnNamesList);

        columnNamesList.addAll(Arrays.asList(persister.getKeyColumnNames()));

        columnNamesList.sort(String::compareTo);

        return columnNamesList.toArray(new String[0]);
    }

    private List<String> extractColumnNamesFromProperties(AbstractEntityPersister persister) {
        List<String> columnNamesList = new ArrayList<>();
        String[] propertyNames = persister.getPropertyNames();

        for (String propertyName : propertyNames) {
            String[] propertyColumnNames = persister.getPropertyColumnNames(propertyName);
            columnNamesList.addAll(Arrays.asList(propertyColumnNames));
        }

        return columnNamesList;
    }

    private void changeIdFieldInForeignKeysToUuid(List<String> columnNamesList) {
        ListIterator<String> iterator = columnNamesList.listIterator();
        while (iterator.hasNext()) {
            String propertyColumnName = iterator.next();
            if (propertyColumnName.contains("_id")) {
                iterator.set(propertyColumnName.replace("_id", "_uuid")); // replace the original element
            }
        }
    }

    public void writeAllRecordsToFile(String fileName, StringWriter sw) {
        try (OutputStreamWriter fileWriter =
                     new OutputStreamWriter(
                             new FileOutputStream(fileName),
                             StandardCharsets.UTF_8)) {
            fileWriter.write(sw.toString());
        } catch (IOException e) {
            log.error("Error occurred while writing to file: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred while writing to file: ", e);
        }
    }

    public void printAllRecords(CSVPrinter printer) {
        int pageNumber = 0;
        Page<Order> page;
        do {
            page = orderService.findAll(pageNumber);

            page.forEach((order) -> {
                printOrder(printer, order);
            });
            pageNumber++;

        } while (page.hasNext());
    }

    private void printOrder(CSVPrinter printer, Order order) {
        try {
            printer.printRecord(
                    order.getCustomer(),
                    order.getId(),
                    order.getPackageSize(),
                    order.getPackageType(),
                    order.getPreferredDeliveryDate(),
                    order.getReceiverAddress(),
                    order.getSenderAddress(),
                    order.getUuid(),
                    order.getVersion()
            );
        } catch (IOException e) {
            log.error("Error occurred while printing records: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred while printing records: ", e);
        }
    }

}
