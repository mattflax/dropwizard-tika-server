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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

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

/**
 * JavaDoc for TikaResource.
 *
 * @author mlp
 */
@Path("/tika/{opKey}")
public class TikaResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(TikaResource.class);

	private static final String CONTENT_LENGTH = "Content-Length";
	private static final String FILE_NAME = "File-Name";
	private static final String RESOURCE_NAME = "resourceName";
	
	public static final String METADATA_OPKEY = "metadata";
	public static final String FULLDATA_OPKEY = "fulldata";
	public static final String TEXT_OPKEY = "text";

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	public TikaDocument handlePut(@PathParam("opKey") String opKey, 
			@Context HttpServletRequest request,
			@Context HttpHeaders headers) {
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
				ret = new TikaDocument((Map<String,Object>)null, textBuffer.toString());
			} else {
				ret = new TikaDocument(convertMetadataToMap(metadata), textBuffer.toString());
			}
		} catch (IOException e) {
			LOGGER.error("IO exception: {}", e.getMessage());
			ret = new TikaDocument("ERROR", e.getMessage());
		} catch (TikaException e) {
			LOGGER.error("Tika exception for document: {}", e.getMessage());
			ret = new TikaDocument("ERROR", e.getMessage());
		} catch (SAXException e) {
			LOGGER.error("SAX exception parsing document: {}", e.getMessage());
			ret = new TikaDocument("ERROR", e.getMessage());
		}
		
		return ret;
	}

	public Detector createDetector(HttpHeaders httpHeaders) throws IOException, TikaException {
		final javax.ws.rs.core.MediaType mediaType = httpHeaders.getMediaType();
		if (mediaType == null || mediaType.equals(javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE))
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
	 * Set possible metadata from http headers
	 * 
	 * @param parser
	 * @param metadata
	 * @param httpHeaders
	 */
	public void setMetadataFromHeader(AutoDetectParser parser,
			org.apache.tika.metadata.Metadata metadata, HttpHeaders httpHeaders) {
		MediaType mediaType = httpHeaders.getMediaType();

		final List<String> fileName = httpHeaders.getRequestHeader(FILE_NAME), cl = httpHeaders
				.getRequestHeader(CONTENT_LENGTH);
		if (cl != null && !cl.isEmpty())
			metadata.set(CONTENT_LENGTH, cl.get(0));

		if (fileName != null && !fileName.isEmpty())
			metadata.set(RESOURCE_NAME, fileName.get(0));

		if (mediaType != null && !mediaType.equals(MediaType.APPLICATION_OCTET_STREAM_TYPE)) {
			metadata.add(org.apache.tika.metadata.HttpHeaders.CONTENT_TYPE, mediaType.toString());

			final Detector detector = parser.getDetector();

			parser.setDetector(new Detector() {
				private static final long serialVersionUID = 1L;
				@Override
				public org.apache.tika.mime.MediaType detect(InputStream inputStream,
						Metadata metadata) throws IOException {
					String ct = metadata.get(org.apache.tika.metadata.HttpHeaders.CONTENT_TYPE);
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
	
}
