package org.grobid.core.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.exceptions.GrobidPropertyException;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.GrobidPropertyKeys;
import org.grobid.core.utilities.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles all properties, which can be set for the grobid-istex
 * project. it is directly extended by the {@link GrobidProperties} class and
 * therefore also contains all properties neccessary for the grobid-core
 * project. A file defining properties for grobid-istex must have the name '
 * {@value #FILE_GROBID_ISTEX_PROPERTIES}' and can be contained in path either
 * located by the system property {@value GrobidProperties#PROP_GROBID_HOME} or
 * the system property {@value #PROP_GROBID_HOME} or given by context property
 * (retrieved via InitialContext().lookup(...)). If both are set this class will
 * try to load the file in {@value GrobidProperties#PROP_GROBID_HOME} first.
 * 
 * @author Florian / Patrice
 * 
 */
public class ISTEXProperties {

	/**
	 * The Logger.
	 */
	public static final Logger LOGGER = LoggerFactory
			.getLogger(ISTEXProperties.class);

	/**
	 * Internal property object, where all properties are defined.
	 */
	protected static Properties props = null;

	/**
	 * Path to grobid-istex.property.
	 */
	protected static File GROBID_ISTEX_PROPERTY_PATH = null;

	/**
	 * The context of the application.
	 */
	protected static Context context;

	/**
	 * A static {@link GrobidProperties} object containing all properties used
	 * by grobid.
	 */
	private static ISTEXProperties grobidIstexProperties = null;

	/**
	 * Returns a static {@link GrobidIstexProperties} object. If no one is
	 * set, than it creates one. {@inheritDoc #GrobidProperties()}
	 * 
	 * @return
	 */
	public static ISTEXProperties getInstance() {
		if (grobidIstexProperties == null)
			return getNewInstance();
		else
			return grobidIstexProperties;
	}
	
	/**
	 * Reload GrobidIstexProperties.
	 */
	public static void reload() {
		getNewInstance();
	}

	/**
	 * Creates a new {@link ISTEXProperties} object, initializes it and
	 * returns it. {@inheritDoc #ISTEXProperties()} First checks to find
	 * the grobid home folder by resolving the given context. When no context
	 * properties exist, The detection will be given to
	 * {@link GrobidProperties#detectGrobidHomePath()}.
	 * 
	 * @return
	 */
	protected static synchronized ISTEXProperties getNewInstance() {
		LOGGER.debug("Start GrobidIstexProperties.getNewInstance");
		try {
			grobidIstexProperties = new ISTEXProperties();
		} catch (NamingException nexp) {
			throw new GrobidPropertyException(
					"Could not get the initial context", nexp);
		}
		return grobidIstexProperties;
	}

	/**
	 * Returns all grobid-properties.
	 * 
	 * @return properties object
	 */
	public static Properties getProps() {
		return props;
	}

	/**
	 * @param props
	 *            the props to set
	 */
	protected static void setProps(Properties pProps) {
		props = pProps;
	}

	/**
	 * Return the context.
	 * 
	 * @return the context.
	 */
	public static Context getContext() {
		return context;
	}

	/**
	 * Set the context.
	 * 
	 * @param pContext
	 *            the context.
	 */
	public static void setContext(Context pContext) {
		context = pContext;
	}

	/**
	 * Loads all properties given in property file {@link #GROBID_HOME_PATH}.
	 */
	protected static void init() {
		LOGGER.debug("Initiating property loading");
		try {
			setContext(new InitialContext());
		} catch (NamingException nexp) {
			throw new GrobidPropertyException(
					"Could not get the initial context", nexp);
		}
	}

	/**
	 * Initializes a {@link ISTEXProperties} object by reading the
	 * property file.
	 * 
	 * @throws NamingException
	 * 
	 */
	public ISTEXProperties() throws NamingException {
		LOGGER.debug("Instanciating GrobidIstexProperties");
		init();
		setProps(new Properties());
		String grobidIstexPath;
		try {
			grobidIstexPath = "src/main/resources/grobid-istex.properties";
		} catch (Exception exp) {
			throw new GrobidPropertyException(
					"Could not load the path to grobid-istex.properties from the context",
					exp);
		}
		File grobidIstexPropFile = new File(grobidIstexPath);

		// exception if prop file does not exist
		if (grobidIstexPropFile == null || !grobidIstexPropFile.exists()) {
			throw new GrobidPropertyException(
					"Could not read grobid-istex.properties, the file '"
							+ grobidIstexPropFile + "' does not exist.");
		}

		// load server properties and copy them to this properties
		try {
			GROBID_ISTEX_PROPERTY_PATH = grobidIstexPropFile
					.getCanonicalFile();
			Properties istexProps = new Properties();
			istexProps.load(new FileInputStream(grobidIstexPropFile));
			getProps().putAll(istexProps);
		} catch (FileNotFoundException e) {
			throw new GrobidPropertyException(
					"Cannot load properties from file " + grobidIstexPropFile
							+ "''.");
		} catch (IOException e) {
			throw new GrobidPropertyException(
					"Cannot load properties from file " + grobidIstexPropFile
							+ "''.");
		}

		// prevent NullPointerException if GrobidProperties is not yet
		// instantiated
		if (GrobidProperties.getGrobidHomePath() == null) {
			GrobidProperties.getInstance();
		}
		GrobidProperties.setContextExecutionServer(true);
	}

	public static File getGrobidPropertiesPath() {
		return GROBID_ISTEX_PROPERTY_PATH;
	}

	/**
	 * Return the value corresponding to the property key. If this value is
	 * null, return the default value.
	 * 
	 * @param pkey
	 *            the property key
	 * @return the value of the property.
	 */
	protected static String getPropertyValue(String pkey) {
		return getProps().getProperty(pkey);
	}

	/**
	 * Return the value corresponding to the property key. If this value is
	 * null, return the default value.
	 * 
	 * @param pkey
	 *            the property key
	 * @return the value of the property.
	 */
	public static void setPropertyValue(String pkey, String pValue) {
		if (StringUtils.isBlank(pValue))
			throw new GrobidPropertyException("Cannot set property '" + pkey
					+ "' to null or empty.");
		getProps().put(pkey, pValue);
	}

	/**
	 * Returns the login for ISTEX API.
	 * 
	 * @return login for ISTEX API
	 */
	public static String getIstexLogin() {
		return getPropertyValue("org.grobid.istex.login");
	}

	/**
	 * Returns the password for ISTEX API.
	 * 
	 * @return password for ISTEX API
	 */
	public static String getIstexPassword() {
		return getPropertyValue("org.grobid.istex.passwd");
	}

	/**
	 * Update grobid.properties with the key and value given as argument.
	 * 
	 * @param pKey
	 *            key to replace
	 * @param pValue
	 *            value to replace
	 * @throws IOException
	 */
	public static void updatePropertyFile(String pKey, String pValue)
			throws IOException {
		GrobidProperties.updatePropertyFile(getGrobidPropertiesPath(), pKey,
				pValue);
	}

}
