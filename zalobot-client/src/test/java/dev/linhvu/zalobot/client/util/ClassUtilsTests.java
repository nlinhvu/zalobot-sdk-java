package dev.linhvu.zalobot.client.util;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ClassUtilsTests {

	@Test
	void isPresent_withExistingClass_returnsTrue() {
		assertThat(ClassUtils.isPresent("java.lang.String",
				Thread.currentThread().getContextClassLoader())).isTrue();
	}

	@Test
	void isPresent_withNonExistentClass_returnsFalse() {
		assertThat(ClassUtils.isPresent("com.nonexistent.Foo",
				Thread.currentThread().getContextClassLoader())).isFalse();
	}

	@Test
	void isPresent_withNullClassLoader_usesDefault() {
		assertThat(ClassUtils.isPresent("java.lang.String", null)).isTrue();
	}

	@Test
	void isPresent_withExplicitClassLoader_works() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		assertThat(ClassUtils.isPresent("java.util.List", cl)).isTrue();
		assertThat(ClassUtils.isPresent("does.not.Exist", cl)).isFalse();
	}

}