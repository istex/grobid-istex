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

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.grobid.core.factory.AbstractEngineFactory;
import org.grobid.core.utilities.ISTEXProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.spi.resource.Singleton;

/**
 * Service for the GROBID ISTEX.
 * 
 * @author Patrice
 * 
 */

@Singleton
@Path(ISTEXPath.PATH_ISTEX)
public class ISTEXService implements ISTEXPath {

	/**
	 * The class Logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ISTEXService.class);

	private static final String INPUT = "input";

	public ISTEXService() {
		LOGGER.info("Initiating Servlet ISTEXService");
		AbstractEngineFactory.fullInit();
		ISTEXProperties.getInstance();
		LOGGER.info("Initiating of Servlet ISTEXService finished.");
	}

	/**
	 * @see org.grobid.service.process.GrobidRestProcessGeneric#isAlive()
	 */
	@Path(ISTEXPath.PATH_IS_ALIVE)
	@Produces(MediaType.TEXT_PLAIN)
	@GET
	public Response isAlive() {
		Response response = null;
		try {
			LOGGER.debug("called isAlive()...");

			String retVal = null;
			try {
				retVal = Boolean.valueOf(true).toString();
			} catch (Exception e) {
				LOGGER.error("GROBID Service is not alive, because of: ", e);
				retVal = Boolean.valueOf(false).toString();
			}
			response = Response.status(Status.OK).entity(retVal).build();
		} catch (Exception e) {
			LOGGER.error("" + e);
			response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		return response;
	}

	@Path(PATH_PDF_DOCUMENT)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("application/pdf")
	@POST
	public Response processAnnotatePDF(@FormDataParam(INPUT) InputStream inputStream,
									@FormDataParam("name") String fileName) {
		return ISTEXProcessFiles.processPDFAnnotation(inputStream, fileName);
	}

	@Path(PATH_JSON_DOCUMENT)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("application/json")
	@POST
	public Response processAnnotateJson(@FormDataParam(INPUT) InputStream inputStream) {
		return ISTEXProcessFiles.processJsonAnnotation(inputStream);
	}
}
