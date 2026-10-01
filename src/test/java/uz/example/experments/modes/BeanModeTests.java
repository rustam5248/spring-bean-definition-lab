package uz.example.experments.modes;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class BeanModeTests {

    @Test
    void fullMode() {
        try (var ctx = new AnnotationConfigApplicationContext(
                FullModeConfig.class)) {

            ExpensiveResources shared =
                    ctx.getBean(ExpensiveResources.class);

            ResourceUser a =
                    ctx.getBean("userA", ResourceUser.class);

            ResourceUser b =
                    ctx.getBean("userB", ResourceUser.class);

            Object config =
                    ctx.getBean(FullModeConfig.class);

            System.out.println(
                    "Config runtime class: "
                            + config.getClass().getName()
            );

            assertThat(a.expensiveResources())
                    .isSameAs(shared);

            assertThat(b.expensiveResources())
                    .isSameAs(shared);

            assertThat(config.getClass())
                    .isNotEqualTo(FullModeConfig.class);
        }
    }

    @Test
    void liteComponentMode() {
        try (var ctx = new AnnotationConfigApplicationContext(LiteComponentConfig.class)) {
            ExpensiveResources shared =
                    ctx.getBean(ExpensiveResources.class);
            ResourceUser a =
                    ctx.getBean("userA", ResourceUser.class);
            ResourceUser b =
                    ctx.getBean("userB", ResourceUser.class);
            Object config =
                    ctx.getBean(LiteComponentConfig.class);

            System.out.println(
                    "Config runtime class: "
                            + config.getClass().getName()
            );

            assertThat(a.expensiveResources())
                    .isNotSameAs(shared);

            assertThat(b.expensiveResources())
                    .isNotSameAs(shared);

            assertThat(a.expensiveResources())
                    .isNotSameAs(b.expensiveResources());

            assertThat(config.getClass())
                    .isEqualTo(LiteComponentConfig.class);
        }
    }

    @Test
    void proxyDisabledConfig() {
        try (var ctx = new AnnotationConfigApplicationContext(ProxyDisabledConfig.class)) {
            ExpensiveResources shared =
                    ctx.getBean(ExpensiveResources.class);
            ResourceUser a =
                    ctx.getBean("userA", ResourceUser.class);
            ResourceUser b =
                    ctx.getBean("userB", ResourceUser.class);
            Object config =
                    ctx.getBean(ProxyDisabledConfig.class);

            System.out.println(
                    "Config runtime class: "
                            + config.getClass().getName()
            );

            assertThat(a.expensiveResources())
                    .isNotSameAs(shared);

            assertThat(b.expensiveResources())
                    .isNotSameAs(shared);

            assertThat(a.expensiveResources())
                    .isNotSameAs(b.expensiveResources());

            assertThat(config.getClass())
                    .isEqualTo(ProxyDisabledConfig.class);
        }
    }

    @Test
    void parametrInjectionConfig() {
        try (var ctx = new AnnotationConfigApplicationContext(ParameterInjectionConfig.class)) {
            ExpensiveResources shared =
                    ctx.getBean(ExpensiveResources.class);
            ResourceUser a =
                    ctx.getBean("userA", ResourceUser.class);
            ResourceUser b =
                    ctx.getBean("userB", ResourceUser.class);
            Object config =
                    ctx.getBean(ParameterInjectionConfig.class);

            System.out.println(
                    "Config runtime class: "
                            + config.getClass().getName()
            );

            assertThat(a.expensiveResources()).isSameAs(shared);
            assertThat(b.expensiveResources()).isSameAs(shared);

            assertThat(config.getClass())
                    .isEqualTo(ParameterInjectionConfig.class);
        }
    }
}
