package org.grobid.core.main.batch;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.Arrays;

import org.grobid.core.main.GrobidHomeFinder;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.Utilities;
import org.grobid.core.visualization.ISTEXCitationsVisualizer;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.exceptions.GrobidException;

/**
 * The entrance point for starting grobid from command line and perform batch processing
 * 
 * @author Patrice
 */
public class ISTEXMain {

    private static List<String> availableCommands = Arrays.asList("annotateCitations");

    /**
     * Arguments of the batch.
     */
    private static GrobidMainArgs gbdArgs;

    /**
     * Build the path to grobid.properties from the path to grobid-home.
     * 
     * @param pPath2GbdHome
     *            The path to Grobid home.
     * @return the path to grobid.properties.
     */
    protected final static String getPath2GbdProperties(final String pPath2GbdHome) {
        return pPath2GbdHome + File.separator + "config" + File.separator + "grobid.properties";
    }

    /**
     * Infer some parameters not given in arguments.
     */
    protected static void inferParamsNotSet() {
        String tmpFilePath;
        if (gbdArgs.getPath2grobidHome() == null) {
            tmpFilePath = new File("grobid-home").getAbsolutePath();
            System.out.println("No path set for grobid-home. Using: " + tmpFilePath);
            gbdArgs.setPath2grobidHome(tmpFilePath);
            gbdArgs.setPath2grobidProperty(new File("grobid.properties").getAbsolutePath());
        }
    }

    /**
     * Initialize the batch.
     */
    protected static void initProcess() {
        try {
            //MockContext.setInitialContext(gbdArgs.getPath2grobidHome(), gbdArgs.getPath2grobidProperty());
            String grobidHome = null;
            if (gbdArgs.getPath2grobidHome() == null)
                grobidHome = "../grobid-home/";
            else 
                grobidHome = gbdArgs.getPath2grobidHome();

            GrobidHomeFinder grobidHomeFinder = new GrobidHomeFinder(Arrays.asList(grobidHome));
            grobidHomeFinder.findGrobidHomeOrFail();
            GrobidProperties.getInstance(grobidHomeFinder);

            LibraryLoader.load();
        } catch (final Exception exp) {
            System.err.println("Grobid initialisation failed: " + exp);
        }
        GrobidProperties.getInstance();
    }

    /**
     * @return String to display for help.
     */
    protected static String getHelp() {
        final StringBuffer help = new StringBuffer();
        help.append("HELP GROBID\n");
        help.append("-h: displays help\n");
        help.append("-gH: gives the path to grobid home directory.\n");
        help.append("-dIn: gives the path to the directory where the files to be processed are located, to be used only when the called method needs it.\n");
        help.append("-dOut: gives the path to the directory where the result files will be saved. The default output directory is the curent directory.\n");
        //help.append("-s: is the parameter used for process using string as input and not file.\n");
        //help.append("-r: recursive directory processing, default processing is not recursive.\n");
        help.append("-exe: gives the command to execute. The value should be one of these:\n");
        help.append("\t" + availableCommands + "\n");
        return help.toString();
    }

    /**
     * Process batch given the args.
     * 
     * @param pArgs
     *            The arguments given to the batch.
     */
    protected static boolean processArgs(final String[] pArgs) {
        boolean result = true;
        if (pArgs.length == 0) {
            System.out.println(getHelp());
            result = false;
        } 
        else {
            String currArg;
            for (int i = 0; i < pArgs.length; i++) {
                currArg = pArgs[i];
                if (currArg.equals("-h")) {
                    System.out.println(getHelp());
                    result = false;
                    break;
                }
                if (currArg.equals("-gH")) {
                    gbdArgs.setPath2grobidHome(pArgs[i + 1]);
                    if (pArgs[i + 1] != null) {
                        gbdArgs.setPath2grobidProperty(getPath2GbdProperties(pArgs[i + 1]));
                    }
                    i++;
                    continue;
                }
                if (currArg.equals("-dIn")) {
                    if (pArgs[i + 1] != null) {
                        gbdArgs.setPath2Input(pArgs[i + 1]);
                        gbdArgs.setPdf(true);
                    }
                    i++;
                    continue;
                }
                if (currArg.equals("-s")) {
                    if (pArgs[i + 1] != null) {
                        gbdArgs.setInput(pArgs[i + 1]);
                        gbdArgs.setPdf(false);
                    }
                    i++;
                    continue;
                }
                if (currArg.equals("-dOut")) {
                    if (pArgs[i + 1] != null) {
                        gbdArgs.setPath2Output(pArgs[i + 1]);
                    }
                    i++;
                    continue;
                }
                if (currArg.equals("-exe")) {
                    final String command = pArgs[i + 1];
                    if (availableCommands.contains(command)) {
                        gbdArgs.setProcessMethodName(command);
                        i++;
                        continue;
                    } 
                    else {
                        System.err.println("-exe value should be one value from this list: " + availableCommands);
                        result = false;
                        break;
                    }
                }
                if (currArg.equals("-r")) {
                    gbdArgs.setRecursive(true);
                    continue;
                }
            }
        }
        return result;
    }

    /**
     * Starts Grobid from command line using the following parameters:
     * 
     * @param args
     *            The arguments
     */
    public static void main(final String[] args) throws Exception {
        gbdArgs = new GrobidMainArgs();

        if (processArgs(args) && (gbdArgs.getProcessMethodName() != null)) {
            inferParamsNotSet();
            initProcess();
            int nb = 0;
 
            // ISTEXCitationsVisualizer
            long time = System.currentTimeMillis();

            File path = new File(gbdArgs.getPath2Input());
            if (!path.exists()) {
                throw new GrobidException("Cannot annotate files because input directory can not be accessed: " + 
					gbdArgs.getPath2Input());
            }

            File pathOut = new File(gbdArgs.getPath2Output());
            if (!pathOut.exists()) {
                throw new GrobidException("Cannot annotate files because ouput directory can not be accessed: " + 
					gbdArgs.getPath2Output());
            }

            File pathHome = new File(gbdArgs.getPath2grobidHome());
            if (!pathHome.exists()) {
                throw new GrobidException("Cannot annotate files because GROBID home can not be accessed: " + 
                    gbdArgs.getPath2grobidHome());
            } else {
                GrobidProperties.set_GROBID_HOME_PATH(gbdArgs.getPath2grobidHome());
            }

            File pathProperties = new File(gbdArgs.getPath2grobidProperty());
            if (!pathProperties.exists()) {
                pathProperties = new File(gbdArgs.getPath2grobidHome() + "/config/grobid.properties");
                if (!pathProperties.exists()) {
                    throw new GrobidException("Cannot annotate files because GROBID properties file can not be accessed: " + 
                        gbdArgs.getPath2grobidProperty());
                } else
                    GrobidProperties.setGrobidPropertiesPath(gbdArgs.getPath2grobidProperty());
            } else
                GrobidProperties.setGrobidPropertiesPath(gbdArgs.getPath2grobidProperty());
            
            LibraryLoader.load();

            if (gbdArgs.getProcessMethodName().equals("annotateCitations")) {
                File[] refFiles = path.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        System.out.println(name);
                        return name.endsWith(".pdf") || name.endsWith(".PDF");
                    }
                });

                if (refFiles != null) {
                    System.out.println(refFiles.length + " files to be processed.");

                    for (final File file : refFiles) {
                        ISTEXCitationsVisualizer.annotateISTEXCitationsPDF(file, gbdArgs.getPath2Output());
                        nb++;
                    } 
                }
            }
            System.out.println(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
        }

    }

}
