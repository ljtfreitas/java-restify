package com.github.ljtfreitas.restify.http.client.message;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class HeadersTest {

	private Headers headers;

	@Before
	public void setup() {
		headers = new Headers().add("X-Source", "source");
	}

	@Test
	public void mustCreateNewHeadersInstanceWhenHeaderIsAdded() {
		Header newHeader = Header.of("X-Custom", "value");

		Headers output = headers.add(newHeader);

		assertThat(headers, not(hasItem(newHeader)));
		assertThat(output, hasItem(newHeader));
	}

	@Test
	public void mustCreateNewHeadersInstanceWhenHeaderIsAddedUsingNameAndValue() {
		Header newHeader = Header.of("X-Custom", "value");

		Headers output = headers.add(newHeader.name(), newHeader.value());

		assertThat(headers, not(hasItem(newHeader)));
		assertThat(output, hasItem(newHeader));
	}

	@Test
	public void mustCreateNewHeadersInstanceWhenHeaderIsAddedUsingNameAndCollectionOfValues() {
		Headers output = headers.add("X-Custom", Arrays.asList("value1", "value2"));

		assertThat(headers, not(hasItem(Header.of("X-Custom", "value1"))));
		assertThat(headers, not(hasItem(Header.of("X-Custom", "value2"))));

		assertThat(output, hasItem(Header.of("X-Custom", "value1")));
		assertThat(output, hasItem(Header.of("X-Custom", "value2")));
	}

	@Test
	public void mustCopyHeadersWhenNewHeaderIsAdded() {
		Header newHeader = Header.of("X-Custom", "value");

		Headers output = headers.add(newHeader);

		assertThat(headers, not(hasItem(newHeader)));

		assertThat(output, hasItem(Header.of("X-Source", "source")));
		assertThat(output, hasItem(newHeader));
	}

	@Test
	public void mustCreateNewHeadersInstanceWhenHeaderIsReplaced() {
		Header newHeader = Header.of("X-Source", "new-source");

		Headers output = headers.replace(newHeader);

		assertThat(headers, hasItem(Header.of("X-Source", "source")));
		assertThat(headers, not(hasItem(Header.of("X-Source", "new-source"))));

		assertThat(output, not(hasItem(Header.of("X-Source", "source"))));
		assertThat(output, hasItem(Header.of("X-Source", "new-source")));
	}

	@Test
	public void mustCreateNewHeadersInstanceWhenHeaderIsReplacedUsingNameAndValue() {
		Header newHeader = Header.of("X-Source", "new-source");

		Headers output = headers.replace(newHeader.name(), newHeader.value());

		assertThat(headers, hasItem(Header.of("X-Source", "source")));
		assertThat(headers, not(hasItem(Header.of("X-Source", "new-source"))));

		assertThat(output, not(hasItem(Header.of("X-Source", "source"))));
		assertThat(output, hasItem(Header.of("X-Source", "new-source")));
	}
}
