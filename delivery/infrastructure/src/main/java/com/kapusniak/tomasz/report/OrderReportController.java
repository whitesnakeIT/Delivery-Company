package com.kapusniak.tomasz.report;

import com.kapusniak.tomasz.entity.OrderEntity;
import com.kapusniak.tomasz.message.JmsProducer;
import com.kapusniak.tomasz.message.Topic;
import com.kapusniak.tomasz.openapi.model.Order;
import com.kapusniak.tomasz.service.ReportService;
import com.kapusniak.tomasz.service.model.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders/report")
@Slf4j
@RequiredArgsConstructor
public class OrderReportController {

    private final JmsProducer jmsProducer;

    private final OrderService orderService;

    private final ReportService reportService;


    @GetMapping("/generate")
    public ResponseEntity<List<Order>> generateReportOfOrdersAndSendForQueue(
            @RequestParam(
                    defaultValue = "#{T(java.time.LocalDate).of(2023,07,13)}"
            ) @Valid LocalDate startDate,
            @RequestParam(
                    defaultValue = "#{T(java.time.LocalDate).now()}"
            ) @Valid LocalDate endDate
    ) {
        List<Order> orders = orderService.findAllByPreferredDeliveryDateBetween(startDate, endDate);
        jmsProducer.sendMessage(
                Topic.ORDERS.toString().toLowerCase(),
                orders);

        return new ResponseEntity<>(orders, HttpStatus.OK);

    }

    @GetMapping("/save")
    public ResponseEntity<String> saveReportOfAllOrdersToFile(@RequestParam(defaultValue = "report.csv") String fileName) {
        String[] headers = reportService.getTableHeaders(OrderEntity.class);
        StringWriter stringWriter = new StringWriter();
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(headers)
                .build();

        try (final CSVPrinter printer =
                     new CSVPrinter(stringWriter, csvFormat)) {

            reportService.printAllRecords(printer);
            reportService.writeAllRecordsToFile(fileName, stringWriter);

        } catch (IOException e) {
            log.error("Error occurred while writing to file: ", e);

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred while writing to file: ", e);
        }

        return new ResponseEntity<>("Report saved to file: " + fileName, HttpStatus.OK);
    }



}