package dev.linhvu.zalobot.boot.autoconfigure;

import dev.linhvu.zalobot.client.ZaloBotClient;
import dev.linhvu.zalobot.listener.ContainerProperties;
import dev.linhvu.zalobot.listener.ErrorHandler;
import dev.linhvu.zalobot.listener.UpdateListener;
import dev.linhvu.zalobot.listener.UpdateListenerContainer;
import dev.linhvu.zalobot.listener.ZaloBotUpdateListenerContainer;
import dev.linhvu.zalobot.listener.observation.ZaloBotListenerObservationConvention;
import io.micrometer.observation.ObservationRegistry;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;

/**
 * {@link org.springframework.boot.autoconfigure.AutoConfiguration Auto-configuration}
 * for the Zalo Bot listener container.
 *
 * <p>Activates after {@link ZaloBotClientAutoConfiguration} when
 * {@link UpdateListenerContainer} is on the classpath, a {@link ZaloBotClient}
 * bean exists, and the {@code zalobot.listener.enabled} property is not
 * explicitly set to {@code false}.
 *
 * <p>Registers:
 * <ul>
 *   <li>{@link ContainerProperties} bean populated from {@link ZaloBotProperties.Listener}.</li>
 *   <li>{@link ZaloBotUpdateListenerContainer} bean (only when an {@link UpdateListener}
 *       bean is present).</li>
 *   <li>{@link ZaloBotListenerContainerLifecycle} to integrate the container with
 *       Spring's lifecycle.</li>
 * </ul>
 *
 * @author Linh Vu
 * @since 0.0.1
 * @see ZaloBotProperties.Listener
 * @see ZaloBotClientAutoConfiguration
 */
@AutoConfiguration(after = ZaloBotClientAutoConfiguration.class)
@ConditionalOnClass(UpdateListenerContainer.class)
@ConditionalOnBean(ZaloBotClient.class)
@ConditionalOnProperty(name = "zalobot.listener.enabled", havingValue = "true", matchIfMissing = true)
public class ZaloBotListenerAutoConfiguration {

	private final ZaloBotProperties properties;

	/**
	 * Creates the auto-configuration with the given Zalo Bot properties.
	 *
	 * @param properties the bound {@link ZaloBotProperties}
	 */
	ZaloBotListenerAutoConfiguration(ZaloBotProperties properties) {
		this.properties = properties;
	}

	/**
	 * Creates {@link ContainerProperties} from the bound
	 * {@link ZaloBotProperties.Listener} settings, wiring in the update listener,
	 * error handler, and observation components when available.
	 *
	 * @param updateListenerProvider provider for the user's {@link UpdateListener}
	 * @param errorHandlerProvider provider for a custom {@link ErrorHandler}
	 * @param observationRegistryProvider provider for the observation registry
	 * @param observationConventionProvider provider for a custom listener observation convention
	 * @return the configured container properties
	 */
	@Bean
	@ConditionalOnMissingBean
	ContainerProperties zaloBotContainerProperties(
			ObjectProvider<UpdateListener> updateListenerProvider,
			ObjectProvider<ErrorHandler> errorHandlerProvider,
			ObjectProvider<ObservationRegistry> observationRegistryProvider,
			ObjectProvider<ZaloBotListenerObservationConvention> observationConventionProvider) {

		ZaloBotProperties.Listener listenerProps = this.properties.getListener();

		ContainerProperties cp = new ContainerProperties();
		cp.setPollTimeout(listenerProps.getPollTimeout());
		cp.setShutdownTimeout(listenerProps.getShutdownTimeout());
		cp.setBackOffInterval(listenerProps.getBackOffInterval());
		cp.setMaxBackOffInterval(listenerProps.getMaxBackOffInterval());
		cp.setQueueCapacity(listenerProps.getQueueCapacity());
		cp.setProcessingConcurrency(listenerProps.getProcessingConcurrency());

		updateListenerProvider.ifAvailable(cp::setUpdateListener);
		errorHandlerProvider.ifAvailable(cp::setErrorHandler);

		if (listenerProps.isObservationEnabled()) {
			observationRegistryProvider.ifAvailable(cp::setObservationRegistry);
			observationConventionProvider.ifAvailable(cp::setObservationConvention);
		}

		return cp;
	}

	/**
	 * Creates the {@link ZaloBotUpdateListenerContainer} that polls the Zalo Bot API
	 * and dispatches updates to the registered listener.
	 *
	 * @param client the Zalo Bot API client
	 * @param containerProperties the container configuration properties
	 * @return the listener container
	 */
	@Bean
	@ConditionalOnBean(UpdateListener.class)
	@ConditionalOnMissingBean(UpdateListenerContainer.class)
	UpdateListenerContainer zaloBotListenerContainer(
			ZaloBotClient client, ContainerProperties containerProperties) {

		return new ZaloBotUpdateListenerContainer(client, containerProperties);
	}

	/**
	 * Creates a {@link SmartLifecycle} adapter to integrate the listener
	 * container with Spring's application lifecycle.
	 *
	 * @param container the listener container to manage
	 * @return the lifecycle adapter
	 */
	@Bean
	@ConditionalOnBean(UpdateListenerContainer.class)
	ZaloBotListenerContainerLifecycle zaloBotListenerContainerLifecycle(
			UpdateListenerContainer container) {

		return new ZaloBotListenerContainerLifecycle(container);
	}
}
