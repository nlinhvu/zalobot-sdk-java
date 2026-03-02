package dev.linhvu.zalobot.core.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class GetMeTests {

	@Test
	void getMe_isEmptyRecord() {
		GetMe getMe = new GetMe();
		assertThat(getMe).isNotNull();
	}

	@Test
	void getMe_equalsAndHashCode() {
		GetMe a = new GetMe();
		GetMe b = new GetMe();
		assertThat(a).isEqualTo(b);
		assertThat(a.hashCode()).isEqualTo(b.hashCode());
	}

	@Test
	void getMe_toString() {
		GetMe getMe = new GetMe();
		assertThat(getMe.toString()).isEqualTo("GetMe[]");
	}
}
