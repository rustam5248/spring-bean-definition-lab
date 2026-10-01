package uz.example.experments.modes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ProxyDisabledConfig {
    @Bean
    ExpensiveResources expensiveResources() {
        return new ExpensiveResources();
    }
    @Bean
    ResourceUser userA() {return new ResourceUser("A",  expensiveResources());}

    @Bean
    ResourceUser userB() {return new ResourceUser("B",  expensiveResources());}

}
