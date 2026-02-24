package dev.linhvu.zalobot.boot.autoconfigure;

import dev.linhvu.zalobot.client.ZaloBotClient;
import dev.linhvu.zalobot.listener.ConcurrentUpdateListenerContainer;
import dev.linhvu.zalobot.listener.ContainerProperties;
import dev.linhvu.zalobot.listener.ErrorHandler;
import dev.linhvu.zalobot.listener.UpdateListener;
import dev.linhvu.zalobot.listener.UpdateListenerContainer;
import dev.linhvu.zalobot.listener.ZaloUpdateListenerContainer;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * {@link org.springframework.boot.autoconfigure.AutoConfiguration Auto-configuration}
 * for the Zalo Bot listener container.
 *
 * <p>Activates after {@link ZaloBotClientAutoConfiguration} when an
 * {@link UpdateListenerContainer} is on the classpath, a {@link ZaloBotClient} bean
 * exists, and the listener is enabled (default: {@code true}).
 *
 * <p>Registers a {@link ContainerProperties} bean, a
 * {@link ConcurrentUpdateListenerContainer} bean (if an {@link UpdateListener} bean
 * is present), and a Spring {@link org.springframework.context.SmartLifecycle}
 * adapter to manage the container lifecycle with the application context.
 *
 * @see ZaloBotProperties.Listener
 */
@AutoConfiguration(after = ZaloBotClientAutoConfiguration.class)
@ConditionalOnClass(UpdateListenerContainer.class)
@ConditionalOnBean(ZaloBotClient.class)
@ConditionalOnProperty(name = "zalobot.listener.enabled", havingValue = "true", matchIfMissing = true)
public class ZaloBotListenerAutoConfiguration {

	private final ZaloBotProperties properties;

	ZaloBotListenerAutoConfiguration(ZaloBotProperties properties) {
		this.properties = properties;
	}

	@Bean
	@ConditionalOnMissingBean
	ContainerProperties zaloBotContainerProperties(
			ObjectProvider<UpdateListener> updateListenerProvider,
			ObjectProvider<ErrorHandler> errorHandlerProvider) {

		ZaloBotProperties.Listener listenerProps = this.properties.getListener();

		ContainerProperties cp = new ContainerProperties();
		cp.setPollTimeout(listenerProps.getPollTimeout());
		cp.setPollInterval(listenerProps.getPollInterval());
		cp.setShutdownTimeout(listenerProps.getShutdownTimeout());
		cp.setBackOffInterval(listenerProps.getBackOffInterval());
		cp.setMaxBackOffInterval(listenerProps.getMaxBackOffInterval());
		updateListenerProvider.ifAvailable(cp::setUpdateListener);
		errorHandlerProvider.ifAvailable(cp::setErrorHandler);
		return cp;
	}

	@Bean
	@ConditionalOnBean(UpdateListener.class)
	@ConditionalOnMissingBean(UpdateListenerContainer.class)
	UpdateListenerContainer zaloBotListenerContainer(
			ZaloBotClient client, ContainerProperties containerProperties) {

		int concurrency = this.properties.getListener().getConcurrency();
		ConcurrentUpdateListenerContainer container =
				new ConcurrentUpdateListenerContainer(client, containerProperties);
		container.setConcurrency(concurrency);
		return container;
	}

	@Bean
	@ConditionalOnBean(UpdateListenerContainer.class)
	ZaloBotListenerContainerLifecycle zaloBotListenerContainerLifecycle(
			UpdateListenerContainer container) {

		return new ZaloBotListenerContainerLifecycle(container);
	}
}
