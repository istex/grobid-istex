package org.grobid.core.utilities;
;
import java.io.InputStream;
import java.io.StringReader;
import java.io.FileInputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Properties;
import java.util.Iterator;
import org.apache.commons.codec.binary.Base64;

import org.apache.commons.lang3.StringUtils;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.grobid.core.data.BiblioItem;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidPropertyException;

import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import org.codehaus.jackson.*;
import org.codehaus.jackson.map.*;

/**
 * For using ISTEX API and OpenURL service to retrieve/solve bibliographic references. 
 *
 * @author Patrice Lopez
 */
public class ISTEXAPIUtilities {

    private static String istexLogin = null;
    private static String istexPasswd = null;

    private static String OPEN_URL_ISTEX_BASE = "https://api.istex.fr/document/openurl?noredirect";
    private static String DOI_BASE_QUERY = "&rft_id=info:doi/%s";
    private static String ISSN_BASE_QUERY = "&rft.issn=%s";
    private static String ISBN_BASE_QUERY = "&rft.isbn=%s";
    private static String DATE_BASE_QUERY = "&rft.date=%s";
    private static String VOLUME_BASE_QUERY = "&rft.volume=%s";
    private static String ISSUE_BASE_QUERY = "&rft.issue=%s";
    private static String SPAGE_BASE_QUERY = "&rft.spage=%s";
    private static String EPAGE_BASE_QUERY = "&rft.epage=%s";
    private static String TITLE_BASE_QUERY = "&rft.atitle=%s";
    private static String JOURNAL_TITLE_BASE_QUERY = "&rft.jtitle=%s";
    private static String AUTHOR_BASE_QUERY = "&rft.aulast=%s";

    private static void loadProperties() {
        Properties props = new Properties();
        String path = "src/main/resources/grobid-istex.properties";
        try {
            props.load(new FileInputStream(path));
            istexLogin = props.getProperty("org.grobid.istex.login");
            istexPasswd = props.getProperty("org.grobid.istex.passwd");
        } catch(Exception e) {
            throw new GrobidPropertyException("Cannot open property file: " + path, e);
        }
    }

    public static String getISTEXLogin() {
        return istexLogin;
    }

    public static String getISTEXpasswd() {
        return istexPasswd;
    }

    /** 
     * Check if a bibliographical item is available in ISTEX resources via  
     * OpenURL. If available, a link to the ISTEX full text is added to the
     * BiblioItem
     */
    public static String checkAvailabilityOpenURL(BiblioItem biblio) {
        StringBuilder subpath = new StringBuilder();
        String doi = null;
        if (StringUtils.isNotBlank(biblio.getDOI())) {
            // some cleaning of the doi
            doi = biblio.getDOI();
            if ( doi.startsWith("doi:") || doi.startsWith("DOI:") ) {
                doi = doi.substring(4, doi.length());
                doi = doi.trim();
            }

            doi = doi.replace(" ", "");
        }
        if (doi != null) {         
            subpath.append(String.format(DOI_BASE_QUERY, doi));
        } 
        /*else {
            String issn = null;
            if (StringUtils.isNotBlank(biblio.getISSN())) {
                // check if we have the dash at position 5, which is expected by ISTEX OpenURL
                issn = biblio.getISSN();
            }
            if (issn != null)
                subpath.append(String.format(ISSN_BASE_QUERY, issn));

            String volume = null;
            if (StringUtils.isNotBlank(biblio.getVolume())) {
                volume = biblio.getVolume();
                volume = volume.replace(" ", "");
            }
            if (volume != null)
                subpath.append(String.format(VOLUME_BASE_QUERY, volume));

            String issue = null;
            if (StringUtils.isNotBlank(biblio.getIssue())) {
                issue = biblio.getIssue();
                issue = issue.replace(" ", "");
            }
            if (issue != null)
                subpath.append(String.format(ISSUE_BASE_QUERY, issue));

            String firstPage = null;
            String pageRange = biblio.getPageRange();
            int beginPage = biblio.getBeginPage();
            if (beginPage != -1) {
                firstPage = "" + beginPage;
            } else if (pageRange != null) {
                StringTokenizer st = new StringTokenizer(pageRange, "--");
                if (st.countTokens() == 2) {
                    firstPage = st.nextToken();
                } else if (st.countTokens() == 1)
                    firstPage = pageRange;
            }
            if (firstPage != null)
                subpath.append(String.format(SPAGE_BASE_QUERY, firstPage));

            String aut = biblio.getFirstAuthorSurname();
            if (aut != null) {
                aut = TextUtilities.removeAccents(aut);
            }
            try {
                if (StringUtils.isNotBlank(aut))
                    subpath.append(String.format(AUTHOR_BASE_QUERY, URLEncoder.encode(aut, "UTF-8")));
            } catch(Exception e) {
                e.printStackTrace();
            }

            String title = biblio.getTitle();
            if (title != null) {
                title = TextUtilities.removeAccents(title);
                title = title.replace("/", " ");
                title = title.replace(":", "");
            }
            try {
                if (StringUtils.isNotBlank(title))
                    subpath.append(String.format(TITLE_BASE_QUERY, URLEncoder.encode(title, "UTF-8")));
            } catch(Exception e) {
                e.printStackTrace();
            }

            String journalTitle = biblio.getJournal();
            if (journalTitle != null) {
                journalTitle = TextUtilities.removeAccents(journalTitle);
                journalTitle = journalTitle.replace("/", " ");
                journalTitle = journalTitle.replace(":", "");
            }
            try {
                if (StringUtils.isNotBlank(journalTitle))
                    subpath.append(String.format(JOURNAL_TITLE_BASE_QUERY, URLEncoder.encode(journalTitle, "UTF-8")));
            } catch(Exception e) {
                e.printStackTrace();
            }

        }*/
        
        if (subpath.length() == 0)
            return null;

        String fullTextURL = null;
        HttpURLConnection urlConn = null;
        try {
            URL url = new URL(OPEN_URL_ISTEX_BASE + subpath);
            String userCredentials = istexLogin + ":" + istexPasswd;
            String basicAuth = "Basic " + new String(Base64.encodeBase64(userCredentials.getBytes()));

            System.out.println("Sending: " + url.toString());
        
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestProperty("Authorization", basicAuth);
        } 
        catch (Exception e) {
            throw new GrobidException("Problem for connecting to ISTEX service.", e);
        }

        if (urlConn != null) {
            try {
                urlConn.setDoOutput(true);
                urlConn.setDoInput(true);
                urlConn.setRequestMethod("GET");

                urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                
                int status = urlConn.getResponseCode();
                if (status != 404) {
                    InputStream in = urlConn.getInputStream();
                    String json = TextUtilities.convertStreamToString(in);
//System.out.println(json);

                    // get the full text url is available
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        JsonNode node = objectMapper.readValue(json, JsonNode.class);
                        JsonNode resourceNode = node.get("resourceUrl");
                        JsonNode codeNode = node.get("code");

                        if (!resourceNode.isNull()) {
                            fullTextURL = resourceNode.getTextValue();
                            biblio.setURL(fullTextURL);
System.out.println("Found item: " + fullTextURL);
                        }
                    } catch(Exception e) {
                        // LOGGER.debug("could not parse JSON response");
                    }
                }

                urlConn.disconnect();
            } catch (Exception e) {
                throw new GrobidException("Problem parsing the OpenURL ISTEX service'response.", e);
            }
        }
        return fullTextURL;
    }

    private static String API_SEARCH_ISTEX_BASE = "https://api.istex.fr/document/?size=1&output=fulltext,host,doi,author,title&q=";
    private static String API_DOI_BASE_QUERY = "doi:%s";
    private static String API_TITLE_BASE_QUERY = "title:%s";
    private static String API_AUTHOR_BASE_QUERY = "author.name:%s";
    private static String API_ISSN_BASE_QUERY = "host.issn:%s serie.issn:%s";
    private static String API_ISBN_BASE_QUERY = "host.isbn:%s serie.isbn:%s";
    private static String API_VOLUME_BASE_QUERY = "host.volume:%s serie.volume:%s";
    private static String API_ISSUE_BASE_QUERY = "host.issue:%s serie.issue:%s";
    private static String API_PAGE_BASE_QUERY = "host.pages.first:%s serie.pages.first:%s";

    /** 
     * Check if a bibliographical item is available in ISTEX resources via  
     * the search web API. If available, a link to the ISTEX full text is 
     * added to the BiblioItem.
     *
     * This is experimental to compare the resolution rate with the OpenURL
     * service, but it might be useful at some point in the future for 
     * implementing a real bibliographical record matching solution.
     */
    public static String checkAvailabilitySearchAPI(BiblioItem biblio) {
        StringBuilder subpath = new StringBuilder();

        String doi = null;
        if (StringUtils.isNotBlank(biblio.getDOI())) {
            // some cleaning of the doi
            doi = biblio.getDOI();
            if ( doi.startsWith("doi:") || doi.startsWith("DOI:") ) {
                doi = doi.substring(4, doi.length());
                doi = doi.trim();
            }

            doi = doi.replace(" ", "");
        }
        if (doi != null)
            subpath.append(String.format(API_DOI_BASE_QUERY, doi));

        String aut = biblio.getFirstAuthorSurname();
        String title = biblio.getTitle();
        String journalTitle = biblio.getJournal();
        if (aut != null) {
            aut = TextUtilities.removeAccents(aut);
        }
        if (title != null) {
            title = TextUtilities.removeAccents(title);
            title = title.replace("/", " ");
            title = title.replace(":", "");
        }
        if (journalTitle != null) {
            journalTitle = TextUtilities.removeAccents(journalTitle);
            journalTitle = journalTitle.replace("/", " ");
            journalTitle = journalTitle.replace(":", "");
        }
        
        try {
            if (StringUtils.isNotBlank(title))
                subpath.append(String.format(API_TITLE_BASE_QUERY, URLEncoder.encode(title, "UTF-8")));

            //if (StringUtils.isNotBlank(journalTitle))
                //subpath.append(String.format(API_JOURNAL_TITLE_BASE_QUERY, URLEncoder.encode(journalTitle, "UTF-8")));

            if (StringUtils.isNotBlank(aut))
                subpath.append("%20AND%20" + String.format(API_AUTHOR_BASE_QUERY, URLEncoder.encode(aut, "UTF-8")));
        } catch(Exception e) {
            e.printStackTrace();
        }


        if (subpath.length() == 0)
            return null;

        String fullTextURL = null;
        HttpURLConnection urlConn = null;
        try {
            URL url = new URL(API_SEARCH_ISTEX_BASE + subpath);
            String userCredentials = istexLogin + ":" + istexPasswd;
            String basicAuth = "Basic " + new String(Base64.encodeBase64(userCredentials.getBytes()));

            System.out.println("Sending: " + url.toString());
        
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestProperty("Authorization", basicAuth);
        } 
        catch (Exception e) {
            throw new GrobidException("Problem for connecting to ISTEX service.", e);
        }

        if (urlConn != null) {
            try {
                urlConn.setDoOutput(true);
                urlConn.setDoInput(true);
                urlConn.setRequestMethod("GET");

                urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                InputStream in = urlConn.getInputStream();
                String json = TextUtilities.convertStreamToString(in);
System.out.println(json);

                // get the full text url is available
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    JsonNode node = objectMapper.readValue(json, JsonNode.class);
                    JsonNode resourceNode = node.get("resourceUrl");
                    JsonNode codeNode = node.get("code");
                    JsonNode hits = node.path("hits");
                    if (!hits.isNull()) {
                        Iterator<JsonNode> iterator = hits.getElements();
                        if (iterator.hasNext()) {
                            JsonNode result = iterator.next();
                        }
                    }

                    if (!resourceNode.isNull()) {
                        fullTextURL = resourceNode.getTextValue();
                        biblio.setURL(fullTextURL);
                    }
                } catch(Exception e) {

                }

                urlConn.disconnect();
            } catch (Exception e) {
                
            }
        }
        return fullTextURL;
    }
}