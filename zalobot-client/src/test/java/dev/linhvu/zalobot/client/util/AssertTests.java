package dev.linhvu.zalobot.client.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AssertTests {

	@Test
	void notNull_withNonNullObject_doesNotThrow() {
		assertThatCode(() -> Assert.notNull("hello", "should not throw"))
				.doesNotThrowAnyException();
	}

	@Test
	void notNull_withNullObject_throwsIllegalArgumentException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> Assert.notNull(null, "value must not be null"))
				.withMessage("value must not be null");
	}
}