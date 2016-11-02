/**
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package org.grobid.service;

/**
 * This interface only contains the path extensions for accessing the istex grobid service.
 * @author Patrice
 *
 */
public interface ISTEXPath {
	/**
	 * path extension for istex service.
	 */
	public static final String PATH_ISTEX = "/";

	/**
	 * path extension for is alive request.
	 */
	public static final String PATH_IS_ALIVE = "isalive";

	/**
	 * path extension for processing the PDF document
	 */
	public static final String PATH_PDF_DOCUMENT = "annotatePDFDocument";	

	/**
	 * path extension for JSON annotations of the PDF document
	 */
	public static final String PATH_JSON_DOCUMENT = "annotateJsonDocument";	
}
