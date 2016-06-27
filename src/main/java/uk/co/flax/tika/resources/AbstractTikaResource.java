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

import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import uk.co.flax.tika.api.TikaDocument;
import uk.co.flax.tika.service.tika.TikaExtractor;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;

import static uk.co.flax.tika.resources.TikaResource.METADATA_OPKEY;
import static uk.co.flax.tika.resources.TikaResource.TEXT_OPKEY;

/**
 * JavaDoc for AbstractTikaResource.
 *
 * @author mlp
 */
abstract class AbstractTikaResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTikaResource.class);

	/**
	 * Read and extract data from a document supplied through a <code>PUT</code> request.
	 * @param opKey the type of operation required - one of {@link TikaResource#METADATA_OPKEY} (to
	 *              extract metadata), {@link TikaResource#TEXT_OPKEY} (to extract the text), or
	 *              {@link TikaResource#FULLDATA_OPKEY} to extract both metadata and the document text.
	 * @param request the incoming request object.
	 * @param headers the incoming request headers.
	 * @return a {@link TikaDocument} containing the extracted data, or suitable error
	 * messages if problems occurred.
	 */
	TikaDocument handlePut(String opKey, HttpServletRequest request, HttpHeaders headers) {
		TikaDocument ret;
		
		try {
			TikaExtractor extractor = new TikaExtractor(headers, request, opKey);
			extractor.extract();

			if (opKey.equalsIgnoreCase(METADATA_OPKEY)) {
				ret = new TikaDocument(extractor.getMetadataAsMap(), null);
			} else if (opKey.equalsIgnoreCase(TEXT_OPKEY)) {
				ret = new TikaDocument(null, extractor.getBodyText());
			} else {
				ret = new TikaDocument(extractor.getMetadataAsMap(), extractor.getBodyText());
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

	String handleGet() {
		return "Use PUT request with the required document in the request body to convert your document.";
	}

}
