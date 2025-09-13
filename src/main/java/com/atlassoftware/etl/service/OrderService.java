package com.atlassoftware.etl.service;

import com.atlassoftware.etl.repository.OrderRepository;
import com.opencsv.CSVReader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.List;

import com.atlassoftware.etl.model.Order;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    private List<String[]> importCSV(InputStream source) throws Exception {
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(source))) {
            return csvReader.readAll();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao importar CSV: ", e);
        }
    }

    public void saveDataToDatabase(InputStream source) throws Exception {
        List<String[]> data = this.importCSV(source);
        List<Order> orders = data.stream()
                .skip(1) // pula cabeçalho
                .map(line -> {
                    Order order = new Order();
                    order.setId(Long.parseLong(line[0]));
                    order.setCustomerName(line[1].toUpperCase());
                    order.setActive(Boolean.parseBoolean(line[2]));
                    order.setProduct(line[3]);
                    order.setQuantity(Integer.parseInt(line[4]));
                    order.setPrice(new BigDecimal(line[5]));
                    order.setTotal(order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())));
                    return order;
                })
                .filter(Order::getActive) // só salva os ativos
                .toList();

        orderRepository.saveAll(orders);
    }
}
