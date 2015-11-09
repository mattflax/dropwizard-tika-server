/**
 * Copyright (c) 2015 Lemur Consulting Ltd.
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

package uk.co.flax.tika.resources;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import uk.co.flax.tika.api.TikaDocument;

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

import static org.apache.tika.metadata.HttpHeaders.CONTENT_TYPE;

/**
 * JavaDoc for AbstractTikaResource.
 *
 * @author mlp
 */
abstract class AbstractTikaResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTikaResource.class);

	private static final String CONTENT_LENGTH = "Content-Length";
	private static final String FILE_NAME = "File-Name";
	private static final String RESOURCE_NAME = "resourceName";
	
	private static final String METADATA_OPKEY = "metadata";
	private static final String FULLDATA_OPKEY = "fulldata";
	private static final String TEXT_OPKEY = "text";

	/**
	 * Read and extract data from a document supplied through a <code>PUT</code> request.
	 * @param opKey the type of operation required - one of {@link #METADATA_OPKEY} (to
	 *              extract metadata), {@link #TEXT_OPKEY} (to extract the text), or
	 *              {@link #FULLDATA_OPKEY} to extract both metadata and the document text.
	 * @param request the incoming request object.
	 * @param headers the incoming request headers.
	 * @return a {@link TikaDocument} containing the extracted data, or suitable error
	 * messages if problems occurred.
	 */
	TikaDocument handlePut(String opKey, HttpServletRequest request, HttpHeaders headers) {
		TikaDocument ret;
		
		try {
			final Detector detector = createDetector(headers);
			final AutoDetectParser parser = new AutoDetectParser(detector);
			final ParseContext context = new ParseContext();
			context.set(Parser.class, parser);
			
			final Metadata metadata = new Metadata();
			setMetadataFromHeader(parser, metadata, headers);
			
			StringWriter textBuffer = new StringWriter();

			ContentHandler handler = null;
			if (opKey.equalsIgnoreCase(METADATA_OPKEY)) {
				handler = new DefaultHandler();
			} else if (opKey.equalsIgnoreCase(TEXT_OPKEY) || opKey.equalsIgnoreCase(FULLDATA_OPKEY)) {
				handler = new BodyContentHandler(textBuffer);
			}
			
			parser.parse(new BufferedInputStream(request.getInputStream()), handler, metadata, context);

			if (opKey.equalsIgnoreCase(METADATA_OPKEY)) {
				ret = new TikaDocument(convertMetadataToMap(metadata), null);
			} else if (opKey.equalsIgnoreCase(TEXT_OPKEY)) {
				ret = new TikaDocument(null, textBuffer.toString());
			} else {
				ret = new TikaDocument(convertMetadataToMap(metadata), textBuffer.toString());
			}
		} catch (IOException e) {
			LOGGER.error("IO exception: {}", e.getMessage());
			ret = new TikaDocument(e.getMessage());
		} catch (TikaException e) {
			LOGGER.error("Tika exception for document: {}", e.getMessage());
			ret = new TikaDocument(e.getMessage());
		} catch (SAXException e) {
			LOGGER.error("SAX exception parsing document: {}", e.getMessage());
			ret = new TikaDocument(e.getMessage());
		}
		
		return ret;
	}

	/**
	 * Create a MIME type detector, based on the incoming request headers.
	 * @param httpHeaders the request header data.
	 * @return a suitable MIME type detector for the document.
	 * @throws IOException
	 * @throws TikaException
	 */
	private Detector createDetector(HttpHeaders httpHeaders) throws IOException, TikaException {
		final MediaType mediaType = httpHeaders.getMediaType();
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
			metadata.set(RESOURCE_NAME, headers.getFirst(FILE_NAME));
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
					if (ct != null) {
						return org.apache.tika.mime.MediaType.parse(ct);
					} else {
						return detector.detect(inputStream, metadata);
					}
				}
			});
		}
	}

	/**
	 * Convert the Tika metadata to a Map.
	 * @param metadata the metadata to convert.
	 * @return a String - Object map of the metadata.
	 */
	private Map<String, Object> convertMetadataToMap(Metadata metadata) {
		Map<String, Object> retMap = new HashMap<>();
		
		for (String name : metadata.names()) {
			if (metadata.isMultiValued(name)) {
				retMap.put(name, Arrays.asList(metadata.getValues(name)));
			} else {
				retMap.put(name, metadata.get(name));
			}
		}
		
		return retMap;
	}

	String handleGet() {
		return "Use PUT request with the required document in the request body to convert your document.";
	}

}
