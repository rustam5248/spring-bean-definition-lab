package uz.example.beanlab.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class OrderServiceTest {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    ApplicationContext context;

    @Test
    void palceOrderSavesTheOrderInTheRepository(){
        Order placed = orderService.placeOrder("mobile");
        Optional<Order> found = orderRepository.findById(placed.id());

        assertThat(found).isPresent();
        assertThat(found.get().item()).isEqualTo("mobile");
        assertThat(found.get()).isEqualTo(placed);
    }
}
