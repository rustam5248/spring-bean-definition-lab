package uz.example.beanlab.payment;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uz.example.paymentsdk.PaymentGatewayClient;

@Service
public class PaymentService {

    private final PaymentGatewayClient stripeClient;
    private final PaymentGatewayClient paypalClient;

    public PaymentService(@Qualifier("stripeClient") PaymentGatewayClient stripeClient,
                          @Qualifier("paypalClient") PaymentGatewayClient paypalClient) {
        this.stripeClient = stripeClient;
        this.paypalClient = paypalClient;
    }

    public PaymentGatewayClient getStripeClient(){
        return stripeClient;
    }
    public PaymentGatewayClient getPaypalClient(){
        return paypalClient;
    }

}
