package uz.example.experments.modes;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class LiteComponentConfig {
    @Bean
    ExpensiveResources expensiveResources() {
        return new ExpensiveResources();
    }
    @Bean
    ResourceUser userA() {return new ResourceUser("A",  expensiveResources());}

    @Bean
    ResourceUser userB() {return new ResourceUser("B",  expensiveResources());}

}
