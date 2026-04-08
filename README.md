# KubeMQ Spring Boot Starter

Production-quality Spring Boot and Spring Cloud Stream integration for [KubeMQ](https://kubemq.io) message broker.

## Modules

| Module | Description |
|--------|-------------|
| `kubemq-spring-boot-autoconfigure` | Auto-configuration, KubeMQTemplate, listener annotations, health, metrics |
| `kubemq-spring-boot-starter` | Dependency aggregator (add this to your project) |
| `kubemq-spring-cloud-stream-binder` | Spring Cloud Stream binder for Events, EventsStore, Queues |
| `kubemq-spring-boot-starter-kotlin` | Kotlin coroutine extensions, Flow adapters, DSL |
| `kubemq-spring-boot-starter-test` | MockKubeMQServer, TestContainers, test harness |

## Quick Start

Add the starter dependency:

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.kubemq:kubemq-spring-boot-starter:1.0.0")
}
```

Configure your application:

```yaml
# application.yml
kubemq:
  address: localhost:50000
  client-id: my-app
```

Send messages with `KubeMQTemplate`:

```java
@Service
public class OrderService {
    private final KubeMQTemplate template;

    public OrderService(KubeMQTemplate template) {
        this.template = template;
    }

    public void placeOrder(Order order) {
        template.sendEvent("orders", order);
    }
}
```

Receive messages with listener annotations:

```java
@Component
public class OrderConsumer {
    @KubeMQEventListener(channels = "orders")
    public void onOrder(EventMessageReceived event) {
        // process event
    }
}
```

## Requirements

- Java 17+
- Spring Boot 3.2.0+
- KubeMQ broker

## Building

```bash
./gradlew build
```

## License

Apache License 2.0 — see [LICENSE](LICENSE).
