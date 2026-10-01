package uz.example.beanlab.order;

import org.springframework.stereotype.Repository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class OrderRepository {
    private final Map<String, Order> store = new ConcurrentHashMap<>();

    public Order save(Order order){
        store.put(order.id(), order);
        return order;
    }

    public Optional<Order> findById(String id){
        return Optional.ofNullable(store.get(id));
    }
}
