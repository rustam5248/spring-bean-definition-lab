package uz.example.beanlab.payment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import uz.example.paymentsdk.PaymentGatewayClient;

import java.time.Duration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class PaymentConfigTest {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private PaymentService paymentService;

    @Test
    void bothClientsShouldBeInjected() {
        PaymentGatewayClient stripeClient =
                paymentService.getStripeClient();

        PaymentGatewayClient paypalClient =
                paymentService.getPaypalClient();

        assertThat(stripeClient.getBaseUrl())
                .isEqualTo("https://api.stripe.example");

        assertThat(stripeClient.getTimeout())
                .isEqualTo(Duration.ofSeconds(5));

        assertThat(paypalClient.getBaseUrl())
                .isEqualTo("https://api.paypal.example");

        assertThat(paypalClient.getTimeout())
                .isEqualTo(Duration.ofSeconds(10));
    }

}
