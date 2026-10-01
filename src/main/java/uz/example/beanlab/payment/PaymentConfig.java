package uz.example.beanlab.payment;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uz.example.paymentsdk.PaymentGatewayClient;

import java.time.Duration;

@Configuration
public class PaymentConfig {

    @Bean(initMethod = "connect", destroyMethod = "close")
    PaymentGatewayClient stripeClient(){
        return new PaymentGatewayClient("https://api.stripe.example", Duration.ofSeconds(5));
    }
    @Bean(initMethod = "connect")
    PaymentGatewayClient paypalClient(){
        return new PaymentGatewayClient("https://api.paypal.example", Duration.ofSeconds(10));
    }
}
