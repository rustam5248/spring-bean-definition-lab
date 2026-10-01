package uz.example.experments.modes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ParameterInjectionConfig {

    @Bean
    ExpensiveResources expensiveResources() {
        return new ExpensiveResources();
    }

    @Bean ResourceUser userA(ExpensiveResources resource) { return new ResourceUser("A", resource); }
    @Bean ResourceUser userB(ExpensiveResources resource) { return new ResourceUser("B", resource); }
}
