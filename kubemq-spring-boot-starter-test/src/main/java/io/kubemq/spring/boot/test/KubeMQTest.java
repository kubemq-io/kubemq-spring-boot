package io.kubemq.spring.boot.test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that configures a Spring Boot test with KubeMQ infrastructure.
 *
 * <p>Combines {@link SpringBootTest} with automatic KubeMQ test infrastructure
 * based on the selected {@link #mode()}:
 * <ul>
 *   <li>{@link KubeMQTestMode#MOCK} -- starts a gRPC InProcess mock server
 *       and registers it in the context (no Docker required)</li>
 *   <li>{@link KubeMQTestMode#EMBEDDED} -- starts a KubeMQ TestContainers
 *       instance and configures connection details automatically</li>
 *   <li>{@link KubeMQTestMode#EXTERNAL} -- connects to an existing broker
 *       using the application's configured address</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 * @KubeMQTest(mode = KubeMQTestMode.MOCK)
 * class MyEventTest {
 *     @Autowired
 *     KubeMQTemplate template;
 *
 *     @Autowired
 *     MockKubeMQServer mockServer;
 *
 *     @Test
 *     void shouldSendEvent() {
 *         template.sendEvent("test-channel", "hello");
 *         // assert...
 *     }
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootTest
public @interface KubeMQTest {

    /**
     * The testing mode to use.
     *
     * @return the test mode (default: {@link KubeMQTestMode#MOCK})
     */
    KubeMQTestMode mode() default KubeMQTestMode.MOCK;

    /**
     * Alias for {@link SpringBootTest#properties()}.
     */
    @AliasFor(annotation = SpringBootTest.class, attribute = "properties")
    String[] properties() default {};

    /**
     * Alias for {@link SpringBootTest#classes()}.
     */
    @AliasFor(annotation = SpringBootTest.class, attribute = "classes")
    Class<?>[] classes() default {};
}
