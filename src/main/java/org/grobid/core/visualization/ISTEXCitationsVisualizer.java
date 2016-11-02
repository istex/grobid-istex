package org.grobid.core.visualization;

import com.google.common.collect.Multimap;
import net.sf.saxon.trans.XPathException;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.color.PDGamma;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.BibDataSetContext;
import org.grobid.core.document.Document;
import org.grobid.core.document.xml.XmlBuilderUtils;
import org.grobid.core.engines.Engine;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.layout.BoundingBox;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.BibDataSetContextExtractor;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.ISTEXAPIUtilities;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

/**
 * Visualize citation markers and references for ISTEX collections
 */

public class ISTEXCitationsVisualizer {

    public static PDDocument annotatePdfWithCitations(PDDocument document, Document teiDoc) throws IOException, COSVisitorException, XPathException {
        String tei = teiDoc.getTei();
        List<String> uris = new ArrayList<String>();
        for (BibDataSet cit : teiDoc.getBibDataSets()) {
            //String url = ISTEXAPIUtilities.checkAvailabilitySearchAPI(cit.getResBib()); 
            String uri = null;
            try {
                uri = ISTEXAPIUtilities.checkAvailabilityOpenURL(cit.getResBib()); 
            } catch(Exception e) {
                e.printStackTrace();
            }
            uris.add(uri);
        }

        return CitationsVisualizer.annotatePdfWithCitations(document, teiDoc, uris);
    }

    public static String getJsonAnnotations(Document teiDoc) throws IOException, COSVisitorException, XPathException {
        String tei = teiDoc.getTei();
        List<String> uris = new ArrayList<String>();
        for (BibDataSet cit : teiDoc.getBibDataSets()) {
            //String url = ISTEXAPIUtilities.checkAvailabilitySearchAPI(cit.getResBib()); 
            String uri = null;
            try {
                uri = ISTEXAPIUtilities.checkAvailabilityOpenURL(cit.getResBib()); 
            } catch(Exception e) {
                e.printStackTrace();
            }
            uris.add(uri);

            /*for (BoundingBox b : cit.getResBib().getCoordinates()) {
                String teiId = cit.getResBib().getTeiId();
                if (url != null) {
                    annotatePage(document, b.toString(), teiId.hashCode(), 
                        contexts.containsKey(teiId) ? 1.5f : 0.5f, url);
                }
                //annotating reference markers
                for (BibDataSetContext c : contexts.get(teiId)) {
//System.out.println(c.getContext());
                    String mrect = c.getDocumentCoords();
                    for (String coords : mrect.split(";")) {
                        annotatePage(document, coords, teiId.hashCode(), 1.0f, "");
                    }
                }
            }*/
        }

        return CitationsVisualizer.getJsonAnnotations(teiDoc, uris);
    }

    public static void annotateISTEXCitationsPDF(File input, String output) {
        try {
            final PDDocument document = PDDocument.load(input);
            File outPdf = new File(output + "/" + input.getName().replace(".pdf", ".istex.pdf"));

            final Engine engine = GrobidFactory.getInstance().getEngine();
            GrobidAnalysisConfig config = 
				new GrobidAnalysisConfig.GrobidAnalysisConfigBuilder().consolidateCitations(true).build();

            Document teiDoc = engine.fullTextToTEIDoc(input, config);
            PDDocument out = annotatePdfWithCitations(document, teiDoc);
            if (out != null) {
                out.save(outPdf);
            }
            System.out.println(input.getPath() + "\n" + Engine.getCntManager());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static String annotateISTEXCitationsJSON(File input) {
        String json = null;
        try {
            final Engine engine = GrobidFactory.getInstance().getEngine();
            GrobidAnalysisConfig config = 
                new GrobidAnalysisConfig.GrobidAnalysisConfigBuilder().consolidateCitations(true).build();

            Document teiDoc = engine.fullTextToTEIDoc(input, config);
            json = getJsonAnnotations(teiDoc);
            System.out.println(input.getPath() + "\n" + Engine.getCntManager());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return json;
    }

}
