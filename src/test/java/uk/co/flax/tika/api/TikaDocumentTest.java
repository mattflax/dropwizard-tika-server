/**
 * Copyright (c) 2016 Lemur Consulting Ltd.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.flax.tika.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Serialization tests for TikaDocument.
 *
 * Created by mlp on 15/02/16.
 */
public class TikaDocumentTest {

	private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

	@Test
	public void errorDocumentTest() throws Exception {
		final TikaDocument doc = new TikaDocument("Failure");

		final String expected = MAPPER.writeValueAsString(
				MAPPER.readValue(fixture("fixtures/errorDocument.json"), TikaDocument.class));

		assertThat(MAPPER.writeValueAsString(doc)).isEqualTo(expected);
	}

	@Test
	public void metadataDocumentTest() throws Exception {
		final Map<String, Object> metadata = new LinkedHashMap<>();
		metadata.put("title", "Title");
		metadata.put("author", "Fred Bloggs");
		metadata.put("pages", 12);

		final TikaDocument doc = new TikaDocument(metadata, null);

		final String expected = MAPPER.writeValueAsString(
				MAPPER.readValue(fixture("fixtures/metadataDocument.json"), TikaDocument.class));

		assertThat(MAPPER.writeValueAsString(doc)).isEqualTo(expected);
	}

	@Test
	public void textDocumentTest() throws Exception {
		final String text = "This is the text of our test document";

		final TikaDocument doc = new TikaDocument(null, text);

		final String expected = MAPPER.writeValueAsString(
				MAPPER.readValue(fixture("fixtures/textDocument.json"), TikaDocument.class));

		assertThat(MAPPER.writeValueAsString(doc)).isEqualTo(expected);
	}

	@Test
	public void fulldataDocumentTest() throws Exception {
		final String text = "This is the text of our test document";
		final Map<String, Object> metadata = new LinkedHashMap<>();
		metadata.put("title", "Title");
		metadata.put("author", "Fred Bloggs");
		metadata.put("pages", 12);

		final TikaDocument doc = new TikaDocument(metadata, text);

		final String expected = MAPPER.writeValueAsString(
				MAPPER.readValue(fixture("fixtures/fulldataDocument.json"), TikaDocument.class));

		assertThat(MAPPER.writeValueAsString(doc)).isEqualTo(expected);
	}

}
