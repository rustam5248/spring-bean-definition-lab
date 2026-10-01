# Spring Bean Definition Lab — `@Component` vs `@Bean`

A small Spring Boot project built to close a specific development gap: my explanation of `@Component` versus `@Bean` was assessed as basic. IoC, DI and constructor injection were already understood, so this lab focuses on what actually happens inside the container when a bean is declared one way or the other.

Every claim in this document is backed by a test in this repository. Where I made a prediction before running an experiment, I recorded the prediction and kept it even when it was wrong.

**Author:** [Your name]
**Period:** [start date] – [end date]
**Spring Boot:** 3.5.x · **Java:** 21

---

## How to run

```bash
./mvnw test
```

All tests pass. Exercises 1 and 2 use `@SpringBootTest`. Exercise 3 uses `@SpringBootTest` plus a negative assertion about component scanning. Exercise 4 does not start the application at all — it builds four isolated containers with `AnnotationConfigApplicationContext` so the configurations cannot interfere with each other.

---

## Summary in my own words

`@Component` is placed on a class I own, and Spring discovers it automatically through classpath scanning, calls its constructor, and manages it. `@Bean` is placed on a method inside a configuration class, and I write the creation logic myself, which means I decide the constructor arguments, the number of instances, and the conditions under which the bean exists.

I use `@Component` for my own application classes — services, repositories, controllers — because they are mine to annotate and need no special construction. I use `@Bean` when the class is not mine (a JDK or third-party class that I cannot annotate), when I need several differently-configured instances of the same type, or when creation depends on configuration or environment.

The part I did not know before this lab is that a `@Bean` method does not always behave the same way. Inside a `@Configuration` class it runs in "full" mode, where the class is proxied and calls between bean methods return the existing singleton. Inside a `@Component`, or with `proxyBeanMethods = false`, it runs in "lite" mode, where the same call is an ordinary Java call that creates a new, unmanaged object.

---

## Exercise 1 — Stereotypes and constructor injection

### Goal

Confirm how Spring registers classes I own, what name it gives them, and what stereotype annotations add beyond plain `@Component`.

### What I built

`Order` as an immutable record (`id`, `item`, `createdAt`), `OrderRepository` annotated with `@Repository` backed by a `ConcurrentHashMap`, and `OrderService` annotated with `@Service` receiving the repository through its constructor. No field injection and no `@Autowired` on the constructor, since a class with a single constructor does not need it.

### How I experimented

In `OrderServiceTest` I injected `OrderService`, `OrderRepository` and the `ApplicationContext` itself. I asserted that an order placed through the service can be read back from the repository, and I inspected the container directly with `context.getBeanNamesForType(OrderService.class)`.

I then ran two variations. First I replaced `@Repository` with plain `@Component` and re-ran the suite. Second I added a throwaway class named `PDFExporter` to see how its bean name would be generated.

### Result

The bean name is `orderService` — decapitalized simple class name — and `getBeanNamesForType` returned exactly one name, confirming a single instance of that type. Because the service and the repository injected into the test are the same singletons the service uses internally, the saved order was found.

Replacing `@Repository` with `@Component` kept all tests green. Registration is identical, because stereotype annotations are themselves meta-annotated with `@Component`. What differs is semantics: `@Repository` enables persistence exception translation, which converts vendor-specific database exceptions into Spring's `DataAccessException` hierarchy, and it communicates intent to other developers.

The naming rule has an edge case: `PDFExporter` is registered as `PDFExporter`, not `pDFExporter`, because when the first two characters are both uppercase the name is left untouched.

### Why this matters in real work

Default bean names are what `@Qualifier` and name-based lookups rely on, so a class rename silently changes a bean name. And choosing `@Repository` over `@Component` is not decoration — losing exception translation means catching driver-specific exceptions in business code.

---

## Exercise 2 — `@Bean` for a class I cannot annotate

### Goal

Demonstrate the case where `@Component` is simply not an option, and show the practical benefit that comes with controlling construction myself.

### What I built

`TimeConfig`, a `@Configuration` class with a single `@Bean` method returning `Clock.systemUTC()`. `OrderService` now receives the `Clock` in its constructor and builds timestamps with `Instant.now(clock)` instead of `Instant.now()`.

I made this a separate commit so the diff shows exactly why `@Bean` was needed: `java.time.Clock` is a JDK class, and there is no way to place `@Component` on it.

### How I experimented

In `OrderServiceClockTest` I added a nested `@TestConfiguration` class contributing a second `Clock` bean, fixed to `2026-01-01T10:00:00Z` and marked `@Primary`, then asserted that the order's `createdAt` is exactly that instant.

I then deliberately broke it: I renamed the test's `fixedClock()` method to `clock()`, so both definitions carried the same bean name.

### Result

With `@Primary`, the fixed clock won and the timestamp became fully deterministic. Testing time-dependent logic required no mocking framework and no sleeping.

With the duplicate name, the context failed to start with a `BeanDefinitionOverrideException`, reporting that a bean with that name was already defined and that overriding is disabled. The failure analysis suggested either renaming one of the beans or setting `spring.main.allow-bean-definition-overriding=true`.

This taught me that a bean's identity is its **name**, not its type. Two beans of the same type coexist happily; two beans with the same name do not, because Spring Boot disables definition overriding by default so that accidental collisions fail loudly at startup rather than silently in production.

### Why this matters in real work

Nearly every externally-provided object is declared this way: `ObjectMapper`, `RestClient`, `WebClient`, `DataSource`, `RedisTemplate`, SDK clients. The override error is also one I am now able to diagnose instantly, and it appears whenever two starters or two configuration classes claim the same bean name.

---

## Exercise 3 — Several beans of one type, and the limits of scanning

### Goal

Show a requirement that `@Component` cannot express at all, and prove that a `@Component` outside the scanned packages does nothing.

### What I built

A simulated third-party library in `com.external.paymentsdk`, deliberately placed outside the application's base package. It contains `PaymentGatewayClient`, a plain class with a `(baseUrl, timeout)` constructor and public `connect()` and `close()` methods and no Spring annotations, and `SdkInternalHelper`, a class that *is* annotated with `@Component`.

In my own package, `PaymentConfig` declares two beans of the same type: `stripeClient` with `initMethod = "connect"` and an explicit `destroyMethod = "close"`, and `paypalClient` with `initMethod = "connect"` and no destroy method declared. `PaymentService` receives both through its constructor using `@Qualifier`.

### How I experimented

I asserted that both clients are injected with different base URLs and timeouts. I asserted that `context.getBean(SdkInternalHelper.class)` throws `NoSuchBeanDefinitionException`. I watched the console output for lifecycle callbacks. Then I removed both `@Qualifier` annotations twice: once with the constructor parameters renamed to `client1` and `client2`, and once with them named `stripeClient` and `paypalClient`.

### Result

Two beans of one class, each configured differently, worked exactly as intended — something a single `@Component` on that class could never produce.

`SdkInternalHelper` was absent from the context despite its `@Component` annotation, because `@SpringBootApplication` only scans its own package and sub-packages. This is the real explanation behind the common complaint that "Spring cannot find my bean."

The console showed `CLOSE` for **both** clients on shutdown, even though only `stripeClient` declared a destroy method. The reason is destroy-method inference: by default Spring looks for a public no-argument `close()` or `shutdown()` method and calls it. I had assumed the `paypalClient` would leak its connection, so this was a prediction I got wrong.

The qualifier experiment produced the more interesting lesson. With parameters named `client1` and `client2`, startup failed with the message that the constructor required a single bean but two were found, listing both candidate names. With the parameters named `stripeClient` and `paypalClient`, **it worked without any qualifier at all** — Spring falls back to matching the parameter name against the bean name. That fallback depends on the compiler's `-parameters` flag, which `spring-boot-starter-parent` enables by default. I now treat `@Qualifier` as the explicit, rename-safe choice, because an innocent parameter rename would otherwise break the application at startup.

### Why this matters in real work

Primary and read-replica datasources, several HTTP clients for different partners, multiple Kafka factories — all of these are the same type configured differently, and all require `@Bean`.

---

## Exercise 4 — Full mode versus lite mode

This is the core of the lab and the thing I could not have explained before.

### Goal

Determine whether a call from one `@Bean` method to another returns the container's singleton or a new object, and establish what decides it.

### What I built

Four configuration classes in an unscanned package, each with identical method bodies: `expensiveResource()` returning a new `ExpensiveResource`, and `userA()` / `userB()` each calling `expensiveResource()` and wrapping the result in a `ResourceUser` record.

The only differences are the class-level annotations: `FullModeConfig` is `@Configuration`; `LiteComponentConfig` is `@Component`; `ProxyDisabledConfig` is `@Configuration(proxyBeanMethods = false)`; `ParameterInjectionConfig` is also `@Configuration(proxyBeanMethods = false)` but receives `ExpensiveResource` as a **method parameter** instead of calling the method.

### How I experimented

Before writing any assertions I wrote down my predictions. Then, in `BeanModesTest`, I built a separate `AnnotationConfigApplicationContext` per configuration, retrieved the `ExpensiveResource` bean and both `ResourceUser` beans, and compared object identity with `isSameAs` / `isNotSameAs`. I also printed the runtime class of each configuration object and compared it with the declared class.

### Prediction versus actual

| Configuration | Predicted: same object? | Actual: same object? | Configuration proxied? |
|---|---|---|---|
| `FullModeConfig` (`@Configuration`) | Yes | **Yes** | Yes — runtime class contains `$$SpringCGLIB$$` |
| `LiteComponentConfig` (`@Component`) | [your prediction] | **No** — three distinct objects | No |
| `ProxyDisabledConfig` (`proxyBeanMethods = false`) | [your prediction] | **No** | No |
| `ParameterInjectionConfig` (parameters) | [your prediction] | **Yes** | No |

### Result and explanation

In full mode Spring subclasses the configuration class with CGLIB at runtime and intercepts every call to a `@Bean` method. When `userA()` calls `expensiveResource()`, the interceptor returns the singleton already held by the container instead of executing the method body. This is why the runtime class is not the class I wrote.

In lite mode there is no subclass and no interception, so `expensiveResource()` is an ordinary Java method call. Each caller gets a brand-new object. Critically, **those extra objects are invisible to Spring**: they receive no `@PostConstruct` or `@PreDestroy` callbacks, they are never wrapped by proxies for `@Transactional` or caching, and they do not appear in the context. Nothing fails, nothing is logged, and the application starts normally.

Passing the dependency as a method parameter produced a single shared instance in every mode, because resolution is then done by the container rather than by a Java call. This is the same principle as constructor injection, which is why it felt familiar.

### Why this matters in real work

If `ExpensiveResource` were a connection pool, a thread pool or an HTTP client, lite mode would silently create duplicates — extra pools nobody monitors and nobody closes. The defect has no compile-time or startup signal, which makes it exactly the kind of bug that surfaces late and under load.

Two practical rules came out of this. Keep `@Bean` methods inside `@Configuration` classes, as the Spring reference documentation recommends, so full mode always applies. And prefer method-parameter injection over inter-method calls, because it is correct regardless of mode and it allows `proxyBeanMethods = false`, which Spring Boot uses throughout its own auto-configuration to avoid the cost of creating CGLIB proxies at startup.

One related constraint also became clear: in full mode, `@Bean` methods must not be `private` or `final`, because CGLIB generates a subclass and cannot override them.

---

## Decision guide

| Question | `@Component` | `@Bean` |
|---|---|---|
| Where is it placed? | On a class | On a method in a configuration class |
| How does Spring find it? | Classpath scanning of the base package and below | Explicit declaration, processed at configuration parsing |
| Who writes the creation logic? | Spring, by calling the constructor | I do, inside the method body |
| Works for JDK / third-party classes? | No, the source cannot be annotated | Yes, this is its primary purpose |
| Several beans of one type? | Not from one class | Yes, one method per instance |
| Custom init / destroy for classes I don't own? | No | Yes, via `initMethod` / `destroyMethod` (destroy is inferred for `close()` / `shutdown()`) |
| Conditional creation | Possible with conditions on the class | Possible, and keeps the whole decision in one readable place |
| Typical examples | `@Service`, `@Repository`, `@Controller` classes I wrote | `ObjectMapper`, `DataSource`, `Clock`, SDK clients |

---

## What surprised me

Destroy-method inference calling `close()` on a bean that never declared a destroy method. Parameter-name matching resolving an ambiguous injection without any `@Qualifier`, and depending on a compiler flag to do so. And above all, that moving the same `@Bean` method from a `@Configuration` class into a `@Component` changes its behaviour completely while producing no error of any kind.

---

## Sources

- Spring Framework Reference — *Basic Concepts: @Bean and @Configuration*, *Using the @Bean Annotation*, *Using the @Configuration Annotation*, *Classpath Scanning and Managed Components* (docs.spring.io)
- Baeldung — *Spring @Component Annotation*
- Dan Vega — *Spring @Component vs @Bean*

I verified claims against the official reference first. Some widely circulated articles on this topic are wrong — one reverses the two annotations entirely — which is itself a reason to start from primary documentation.
