package dev.linhvu.zalobot.listener;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ExponentialBackOffTests {

	@Test
	void firstBackOff_returnsInitialInterval() {
		ExponentialBackOff backOff = new ExponentialBackOff(
				Duration.ofMillis(1000), Duration.ofMillis(30000));
		assertThat(backOff.nextBackOffMillis()).isEqualTo(1000);
	}

	@Test
	void subsequentBackOffs_doubleEachTime() {
		ExponentialBackOff backOff = new ExponentialBackOff(
				Duration.ofMillis(1000), Duration.ofMillis(30000));
		assertThat(backOff.nextBackOffMillis()).isEqualTo(1000);
		assertThat(backOff.nextBackOffMillis()).isEqualTo(2000);
		assertThat(backOff.nextBackOffMillis()).isEqualTo(4000);
	}

	@Test
	void backOff_capsAtMaxInterval() {
		ExponentialBackOff backOff = new ExponentialBackOff(
				Duration.ofMillis(1000), Duration.ofMillis(3000));
		assertThat(backOff.nextBackOffMillis()).isEqualTo(1000);
		assertThat(backOff.nextBackOffMillis()).isEqualTo(2000);
		assertThat(backOff.nextBackOffMillis()).isEqualTo(3000); // capped
		assertThat(backOff.nextBackOffMillis()).isEqualTo(3000); // stays capped
	}

	@Test
	void reset_returnsToInitialInterval() {
		ExponentialBackOff backOff = new ExponentialBackOff(
				Duration.ofMillis(1000), Duration.ofMillis(30000));
		backOff.nextBackOffMillis(); // 1000
		backOff.nextBackOffMillis(); // 2000
		backOff.reset();
		assertThat(backOff.nextBackOffMillis()).isEqualTo(1000);
	}

	@Test
	void customMultiplier_appliesCorrectly() {
		ExponentialBackOff backOff = new ExponentialBackOff(
				Duration.ofMillis(1000), Duration.ofMillis(30000), 3.0);
		assertThat(backOff.nextBackOffMillis()).isEqualTo(1000);
		assertThat(backOff.nextBackOffMillis()).isEqualTo(3000);
		assertThat(backOff.nextBackOffMillis()).isEqualTo(9000);
	}

	@Test
	void fullSequence_1sTo30s() {
		ExponentialBackOff backOff = new ExponentialBackOff(
				Duration.ofMillis(1000), Duration.ofMillis(30000));
		assertThat(backOff.nextBackOffMillis()).isEqualTo(1000);
		assertThat(backOff.nextBackOffMillis()).isEqualTo(2000);
		assertThat(backOff.nextBackOffMillis()).isEqualTo(4000);
		assertThat(backOff.nextBackOffMillis()).isEqualTo(8000);
		assertThat(backOff.nextBackOffMillis()).isEqualTo(16000);
		assertThat(backOff.nextBackOffMillis()).isEqualTo(30000); // capped
		assertThat(backOff.nextBackOffMillis()).isEqualTo(30000); // stays capped
	}

	@Test
	void initialEqualsMax_staysConstant() {
		ExponentialBackOff backOff = new ExponentialBackOff(
				Duration.ofMillis(5000), Duration.ofMillis(5000));
		assertThat(backOff.nextBackOffMillis()).isEqualTo(5000);
		assertThat(backOff.nextBackOffMillis()).isEqualTo(5000);
		assertThat(backOff.nextBackOffMillis()).isEqualTo(5000);
	}
}