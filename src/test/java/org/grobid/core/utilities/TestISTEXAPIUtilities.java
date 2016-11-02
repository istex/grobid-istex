package org.grobid.core.utilities;

import org.grobid.core.data.BiblioItem;
import org.grobid.core.main.LibraryLoader;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.BeforeClass;

import java.util.ArrayList;
import java.util.List;

public class TestISTEXAPIUtilities {

    //private ISTEXAPIUtilities istexAPIUtilities = null;

    @BeforeClass
    public static void init() {
        LibraryLoader.load();
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testAvailabilityOpenURL() throws Exception {
        BiblioItem biblio = new BiblioItem();
        biblio.setDOI("doi:10.1136/acupmed-2012-010150");
        String istexId = ISTEXAPIUtilities.checkAvailabilityOpenURL(biblio);
        System.out.println("Availability of " + biblio.getDOI() + " :  " + biblio.getURL());
    }


}