package dev.linhvu.zalobot.boot.autoconfigure;

import dev.linhvu.zalobot.boot.ZaloBotClientCustomizer;
import dev.linhvu.zalobot.client.ZaloBotClient;
import dev.linhvu.zalobot.client.ZaloBotUrl;
import dev.linhvu.zalobot.client.observation.ZaloBotClientObservationConvention;
import io.micrometer.observation.ObservationRegistry;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

/**
 * {@link org.springframework.boot.autoconfigure.AutoConfiguration Auto-configuration}
 * for {@link ZaloBotClient}.
 *
 * <p>Activates when {@code ZaloBotClient} is on the classpath and the
 * {@code zalobot.bot-token} property is set. Registers a prototype-scoped
 * {@link ZaloBotClient.Builder} bean (applying any {@link ZaloBotClientCustomizer}
 * beans) and a singleton {@link ZaloBotClient} bean.
 *
 * @author Linh Vu
 * @since 0.0.1
 * @see ZaloBotProperties
 * @see ZaloBotClientCustomizer
 */
@AutoConfiguration
@ConditionalOnClass(ZaloBotClient.class)
@ConditionalOnProperty(name = "zalobot.bot-token")
@EnableConfigurationProperties(ZaloBotProperties.class)
public class ZaloBotClientAutoConfiguration {

	private final ZaloBotProperties properties;

	/**
	 * Creates the auto-configuration with the given Zalo Bot properties.
	 *
	 * @param properties the bound {@link ZaloBotProperties}
	 */
	ZaloBotClientAutoConfiguration(ZaloBotProperties properties) {
		this.properties = properties;
	}

	/**
	 * Creates a prototype-scoped {@link ZaloBotClient.Builder} pre-configured
	 * with properties, observation support, and any registered
	 * {@link ZaloBotClientCustomizer} beans.
	 *
	 * @param customizerProvider provider for client builder customizers
	 * @param observationRegistryProvider provider for the observation registry
	 * @param observationConventionProvider provider for a custom observation convention
	 * @return a pre-configured builder
	 */
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	@ConditionalOnMissingBean
	ZaloBotClient.Builder zaloBotClientBuilder(
			ObjectProvider<ZaloBotClientCustomizer> customizerProvider,
			ObjectProvider<ObservationRegistry> observationRegistryProvider,
			ObjectProvider<ZaloBotClientObservationConvention> observationConventionProvider) {

		ZaloBotProperties.Client clientProps = this.properties.getClient();
		ZaloBotUrl url = new ZaloBotUrl(
				clientProps.getScheme(), clientProps.getHost(), clientProps.getPort());

		ZaloBotClient.Builder builder = ZaloBotClient.builder()
				.botToken(this.properties.getBotToken())
				.zaloBotUrl(url);

		observationRegistryProvider.ifAvailable(builder::observationRegistry);
		observationConventionProvider.ifAvailable(builder::observationConvention);

		customizerProvider.orderedStream()
				.forEach(customizer -> customizer.customize(builder));
		return builder;
	}

	/**
	 * Builds and registers the singleton {@link ZaloBotClient} from the
	 * configured builder.
	 *
	 * @param builder the pre-configured client builder
	 * @return the built {@link ZaloBotClient}
	 */
	@Bean
	@ConditionalOnMissingBean
	ZaloBotClient zaloBotClient(ZaloBotClient.Builder builder) {
		return builder.build();
	}

}
