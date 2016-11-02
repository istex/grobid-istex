package org.grobid.service;

import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.KeyGen;
import org.grobid.core.engines.Engine;
import org.grobid.core.factory.GrobidPoolingFactory;
import org.grobid.core.utilities.IOUtilities;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.visualization.ISTEXCitationsVisualizer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
//import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Patrice
 */
public class ISTEXProcessFiles {

    /**
     * The class Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ISTEXProcessFiles.class);

    /**
     * Uploads the origin PDF, process it and return the PDF augmented with annotations.
     *
     * @param inputStream the data of origin PDF
     * @param fileName the name of origin PDF
     * @return a response object containing the annotated PDF
     */
    public static Response processPDFAnnotation(final InputStream inputStream,
                                                final String fileName) {
        Response response = null;
        boolean isparallelExec = true;
        File originFile = null;
        Engine engine = null;
        PDDocument out = null;
        try {
            originFile = IOUtilities.writeInputFile(inputStream);

            GrobidAnalysisConfig config = 
                new GrobidAnalysisConfig.GrobidAnalysisConfigBuilder().consolidateCitations(true).build();

            if (originFile == null) {
                response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
            } else {
                // starts conversion process
                final PDDocument document = PDDocument.load(originFile);
                engine = Engine.getEngine(isparallelExec);
				DocumentSource documentSource = DocumentSource.fromPdf(originFile);
                if (isparallelExec) {
                    Document teiDoc = engine.fullTextToTEIDoc(originFile, config);
                    out = ISTEXCitationsVisualizer.annotatePdfWithCitations(document, teiDoc);
                    
                    GrobidPoolingFactory.returnEngine(engine);
                    engine = null;
                } else {
                    synchronized (engine) {
                        //TODO: VZ: sync on local var does not make sense
                        Document teiDoc = engine.fullTextToTEIDoc(originFile, config);
                        out = ISTEXCitationsVisualizer.annotatePdfWithCitations(document, teiDoc);
                    } 
                }

                IOUtilities.removeTempFile(originFile);
                if (out != null) {
                    response = Response.status(Status.OK).type("application/pdf").build();
                    ByteArrayOutputStream ouputStream = new ByteArrayOutputStream();
                    out.save(ouputStream);
                    response = Response
                            .ok()
                            .type("application/pdf")
                            .entity(ouputStream.toByteArray())
                            .header("Content-Disposition", "attachment; filename=\"" + fileName
                                    //.replace(".pdf", ".annotated.pdf")
                                    //.replace(".PDF", ".annotated.PDF") 
                                    + "\"")
                            .build();
                }
                else {
                    response = Response.status(Status.NO_CONTENT).build();
                }
            }
        } catch (NoSuchElementException nseExp) {
            LOGGER.error("Could not get an engine from the pool within configured time. Sending service unavailable.");
            response = Response.status(Status.SERVICE_UNAVAILABLE).build();
        } catch (Exception exp) {
            LOGGER.error("An unexpected exception occurs. ", exp);
            response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(exp.getMessage()).build();
        } finally {
            IOUtilities.removeTempFile(originFile);
            if (out != null) {
                try {
                    out.close();
                }
                catch(Exception exp) {
                    LOGGER.error("Error when closing PDDocument. ", exp);
                }
            }
            if (isparallelExec && engine != null) {
                GrobidPoolingFactory.returnEngine(engine);
            }
        }
        return response;
    }

        /**
     * Uploads the origin PDF, process it and return PDF annotations for references in JSON.
     *
     * @param inputStream the data of origin PDF
     * @return a response object containing the JSON annotations
     */
    public static Response processJsonAnnotation(final InputStream inputStream) {
        Response response = null;
        boolean isparallelExec = true;
        File originFile = null;
        Engine engine = null;
        try {
            originFile = IOUtilities.writeInputFile(inputStream);
            GrobidAnalysisConfig config = 
                new GrobidAnalysisConfig.GrobidAnalysisConfigBuilder().consolidateCitations(true).build();

            String json = null;

            if (originFile == null) {
                response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
            } else {
                engine = Engine.getEngine(isparallelExec);
                DocumentSource documentSource = DocumentSource.fromPdf(originFile);
                if (isparallelExec) {
                    Document teiDoc = engine.fullTextToTEIDoc(originFile, config);
                    json = ISTEXCitationsVisualizer.getJsonAnnotations(teiDoc);
                    GrobidPoolingFactory.returnEngine(engine);
                    engine = null;
                } else {
                    synchronized (engine) {
                        //TODO: VZ: sync on local var does not make sense
                        Document teiDoc = engine.fullTextToTEIDoc(originFile, config);
                        json = ISTEXCitationsVisualizer.getJsonAnnotations(teiDoc);
                    } 
                }

                IOUtilities.removeTempFile(originFile);

                if (json != null) {
                    response = Response
                            .ok()
                            .type("application/json")
                            .entity(json)
                            .build();
                }
                else {
                    response = Response.status(Status.NO_CONTENT).build();
                }
            }
        } catch (NoSuchElementException nseExp) {
            LOGGER.error("Could not get an engine from the pool within configured time. Sending service unavailable.");
            response = Response.status(Status.SERVICE_UNAVAILABLE).build();
        } catch (Exception exp) {
            LOGGER.error("An unexpected exception occurs. ", exp);
            response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(exp.getMessage()).build();
        } finally {
            IOUtilities.removeTempFile(originFile);
            if (isparallelExec && engine != null) {
                GrobidPoolingFactory.returnEngine(engine);
            }
        }
        return response;
    }


}
