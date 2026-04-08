package io.kubemq.spring.boot.test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that starts an embedded KubeMQ broker via TestContainers
 * for the annotated test class.
 *
 * <p>The container is started before the Spring context loads and its
 * gRPC address is automatically injected into {@code kubemq.address}.
 * The container is shared across all tests in the annotated class.
 *
 * <p>Requires Docker to be available on the test machine.
 *
 * <p>Usage:
 * <pre>{@code
 * @SpringBootTest
 * @EmbeddedKubeMQ
 * class IntegrationTest {
 *     @Autowired
 *     KubeMQTemplate template;
 *
 *     @Test
 *     void shouldSendAndReceive() {
 *         // real broker is running via TestContainers
 *     }
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface EmbeddedKubeMQ {

    /**
     * Docker image name for the KubeMQ community edition.
     *
     * @return Docker image name (default: {@code "kubemq/kubemq-community:latest"})
     */
    String image() default "kubemq/kubemq-community:latest";

    /**
     * Whether to reuse the container across test classes (requires
     * TestContainers reuse support to be enabled).
     *
     * @return {@code true} to enable container reuse (default: {@code false})
     */
    boolean reuse() default false;
}
