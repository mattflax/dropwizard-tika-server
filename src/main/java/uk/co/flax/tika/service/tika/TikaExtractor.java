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
package uk.co.flax.tika.service.tika;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaMetadataKeys;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.apache.tika.metadata.HttpHeaders.CONTENT_LENGTH;
import static org.apache.tika.metadata.HttpHeaders.CONTENT_TYPE;
import static uk.co.flax.tika.resources.TikaResource.*;

/**
 *
 *
 * Created by mlp on 27/06/16.
 */
public class TikaExtractor {

	private static final Logger LOGGER = LoggerFactory.getLogger(TikaExtractor.class);

	private static final String FILE_NAME = "File-Name";

	private final HttpHeaders headers;
	private final HttpServletRequest request;
	private final boolean requiresBody;

	private final Metadata metadata = new Metadata();
	private String bodyText = null;

	public TikaExtractor(HttpHeaders headers, HttpServletRequest request, String opKey) {
		this.headers = headers;
		this.request = request;
		this.requiresBody = (opKey.equalsIgnoreCase(TEXT_OPKEY) || opKey.equalsIgnoreCase(FULLDATA_OPKEY));
	}

	/**
	 * Extract the data from the HTTP request. The data can then be retrieved using
	 * {@link #getMetadata()}, {@link #getMetadataAsMap()}, and {@link #getBodyText()}.
	 * @throws IOException if there are problems reading the body content.
	 * @throws TikaException if Tika has problems extracting the metadata or content.
	 * @throws SAXException if the body content cannot be parsed.
	 */
	public void extract() throws IOException, TikaException, SAXException {
		final Detector detector = buildDetectorFromHeaders(headers);
		final AutoDetectParser parser = new AutoDetectParser(detector);
		final ParseContext context = new ParseContext();
		context.set(Parser.class, parser);

		setMetadataFromHeader(parser, metadata, headers);

		StringWriter textBuffer = new StringWriter();
		ContentHandler handler = buildContentHandler(textBuffer);

		parser.parse(new BufferedInputStream(request.getInputStream()), handler, metadata, context);
		bodyText = textBuffer.toString();
	}

	private Detector buildDetectorFromHeaders(HttpHeaders headers) throws IOException, TikaException {
		final MediaType mediaType = headers.getMediaType();
		if (mediaType == null || mediaType.equals(MediaType.APPLICATION_OCTET_STREAM_TYPE))
			return (new TikaConfig()).getMimeRepository();
		else
			return new Detector() {
				private static final long serialVersionUID = 1L;

				@Override
				public org.apache.tika.mime.MediaType detect(InputStream inputStream,
															 Metadata metadata) throws IOException {
					return org.apache.tika.mime.MediaType.parse(mediaType.toString());
				}
			};
	}

	/**
	 * Read the metadata from the request headers and add them to a Tika metadata
	 * object.
	 *
	 * @param parser the parser for the incoming document.
	 * @param metadata the metadata object, which will be modified by this method.
	 * @param httpHeaders the request headers.
	 */
	private void setMetadataFromHeader(AutoDetectParser parser, Metadata metadata, HttpHeaders httpHeaders) {
		final MultivaluedMap<String, String> headers = httpHeaders.getRequestHeaders();

		if (headers.containsKey(CONTENT_LENGTH)) {
			metadata.set(CONTENT_LENGTH, headers.getFirst(CONTENT_LENGTH));
		}

		if (headers.containsKey(FILE_NAME)) {
			metadata.set(TikaMetadataKeys.RESOURCE_NAME_KEY, headers.getFirst(FILE_NAME));
		}

		final MediaType mediaType = httpHeaders.getMediaType();
		if (mediaType != null && !mediaType.equals(MediaType.APPLICATION_OCTET_STREAM_TYPE)) {
			metadata.add(CONTENT_TYPE, mediaType.toString());

			final Detector detector = parser.getDetector();

			parser.setDetector(new Detector() {
				private static final long serialVersionUID = 1L;
				@Override
				public org.apache.tika.mime.MediaType detect(InputStream inputStream, Metadata metadata) throws IOException {
					String ct = metadata.get(CONTENT_TYPE);
					LOGGER.info("Content type " + ct);
					org.apache.tika.mime.MediaType type = null;
					// Make sure we never return null
					if (ct != null) {
						type = org.apache.tika.mime.MediaType.parse(ct);
					}
					if (type != null) {
						return type;
					} else {
						return detector.detect(inputStream, metadata);
					}
				}
			});
		}
	}

	private ContentHandler buildContentHandler(StringWriter textBuffer) {
		final ContentHandler handler;
		if (requiresBody) {
			handler = new BodyContentHandler(textBuffer);
		} else {
			handler = new DefaultHandler();
		}

		return handler;
	}

	/**
	 * @return the extracted body text from the document, if applicable, otherwise
	 * <code>null</code>.
	 */
	public String getBodyText() {
		return bodyText;
	}

	/**
	 * @return the metadata extracted from the document.
	 */
	public Metadata getMetadata() {
		return metadata;
	}

	/**
	 * @return the metadata from the document, converted to a Map.
	 */
	public Map<String, Object> getMetadataAsMap() {
		final Map<String, Object> retMap = new HashMap<>();

		for (String name : metadata.names()) {
			if (metadata.isMultiValued(name)) {
				retMap.put(name, Arrays.asList(metadata.getValues(name)));
			} else {
				retMap.put(name, metadata.get(name));
			}
		}

		return retMap;
	}

}
