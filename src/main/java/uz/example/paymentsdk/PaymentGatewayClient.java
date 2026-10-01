package uz.example.paymentsdk;

import java.time.Duration;

public class PaymentGatewayClient {
    private final String baseUrl;
    private final Duration timeout;

    public PaymentGatewayClient(String baseUrl, Duration timeout) {
        this.baseUrl = baseUrl;
        this.timeout = timeout;
    }

    public void connect(){
        System.out.println("Connecting to " + baseUrl);
    }

    public void close(){
        System.out.println("Closing connection to " + baseUrl);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Duration getTimeout() {
        return timeout;
    }
}
