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

package uk.co.flax.tika.api;

import java.util.Map;

/**
 * JavaDoc for TikaDocument.
 *
 * @author mlp
 */
public class TikaDocument {

	public static final String OK_STATUS = "OK";
	public static final String ERROR_STATUS = "ERROR";
	
	private final String status;
	private final String message;
	
	private final Map<String, Object> metadata;
	private final String text;
	
	public TikaDocument(String message) {
		this(ERROR_STATUS, message, null, null);
	}
	
	public TikaDocument(Map<String, Object> metadata, String text) {
		this(OK_STATUS, null, metadata, text);
	}
	
	public TikaDocument(String status, String message, Map<String, Object> metadata, String text) {
		this.status = status;
		this.message = message;
		this.metadata = metadata;
		this.text = text;
	}

	public String getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public String getText() {
		return text;
	}

}
