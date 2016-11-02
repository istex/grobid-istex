package org.grobid.core.visualization;

import org.grobid.core.data.BiblioItem;
import org.grobid.core.main.LibraryLoader;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.BeforeClass;

import java.util.ArrayList;
import java.util.List;
import java.io.File;

public class TestISTEXCitationsVisualizer {

    private ISTEXCitationsVisualizer istexCitationsVisualizer = null;

    @BeforeClass
    public static void init() {
        LibraryLoader.load();
    }

    @Before
    public void setUp() throws Exception {
        //istexAPIUtilities = new ISTEXAPIUtilities();
    }

    @Test
    public void testCitationAnnotation() throws Exception {
        File input = new File("src/test/resources/test1.pdf");
        ISTEXCitationsVisualizer.annotateISTEXCitationsPDF(input, "/tmp/");

        input = new File("src/test/resources/test2.pdf");
        ISTEXCitationsVisualizer.annotateISTEXCitationsPDF(input, "/tmp/");

        input = new File("src/test/resources/test3.pdf");
        ISTEXCitationsVisualizer.annotateISTEXCitationsPDF(input, "/tmp/");
    }


}