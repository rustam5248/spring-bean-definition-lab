package uz.example.beanlab.order;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final Clock clock;

    public OrderService(OrderRepository orderRepository,  Clock clock) {
        this.orderRepository = orderRepository;
        this.clock = clock;
    }

    public Order placeOrder(String item){
        Order order = new Order(UUID.randomUUID().toString(), item, Instant.now(clock));
        return orderRepository.save(order);
    }
}
