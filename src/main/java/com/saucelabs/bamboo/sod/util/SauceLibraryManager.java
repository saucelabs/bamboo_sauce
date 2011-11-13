package com.saucelabs.bamboo.sod.util;

import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.atlassian.plugin.PluginAccessor;
import com.saucelabs.sauceconnect.SauceConnect;
import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Handles checking for and installing updates to the SauceConnect library.  Updates are checked by
 * sending a request to https://saucelabs.com/versions.json, and comparing the version attribute of the
 * 'Sauce Connect 2' entry in the JSON response to the SauceConnect.RELEASE() variable.
 * <p/>
 * If the version number in the JSON response is greater than the value in the SauceConnect library,
 * we need to perform a HTTP get on the URL specified in the download_url attribute of the JSON response
 * (which will be a ZIP file).  We then unzip the zip file, and include the Sauce-Connect.jar file into the
 * Bamboo plugin.
 * <p/>
 *
 * @author Ross Rowe
 */
public class SauceLibraryManager {

    private static final Logger logger = Logger.getLogger(SauceLibraryManager.class);

    private static final String VERSION_CHECK_URL = "https://saucelabs.com/versions.json";

    /**
     * Populated via dependency injection.
     */
    private AdministrationConfigurationManager administrationConfigurationManager;

    private PluginAccessor pluginAccessor;

    /**
     * Populated by dependency injection.
     */
    private SauceFactory sauceAPIFactory;

    private static final int BUFFER = 1024;

    private static final String PLUGIN_KEY = "com.saucelabs.bamboo.bamboo-sauceondemand-plugin";

    /**
     * Performs a REST request to https://saucelabs.com/versions.json to retrieve the list of
     * sauce connect version information.  If the version information in the response is later
     * than the current version, then return true.
     *
     * @return boolean indicating whether an updated sauce connect jar is available
     * @throws IOException
     * @throws JSONException      thrown if an error occurs during the parsing of the JSON response
     * @throws URISyntaxException
     */
    public boolean checkForLaterVersion() throws IOException, JSONException, URISyntaxException {
        //retrieve contents of version url and parse as JSON
        getSauceAPIFactory().setupProxy(administrationConfigurationManager);
        SauceFactory sauceFactory = new SauceFactory();
        String response = sauceFactory.doREST(VERSION_CHECK_URL);
//        String response = IOUtils.toString(getClass().getResourceAsStream("/versions.json"));
        int version = extractVersionFromResponse(response);
        //compare version attribute against SauceConnect.RELEASE()
        return version > (Integer) SauceConnect.RELEASE();
    }

    /**
     * Performs a REST request to https://saucelabs.com/versions.json to retrieve the download url
     * to be used to retrieve the latest version of Sauce Connect, then updates the Bamboo Sauce plugin jar
     * to include this jar.
     *
     * @throws JSONException      thrown if an error occurs during the parsing of the JSON response
     * @throws IOException
     * @throws URISyntaxException
     */
    public void triggerReload() throws JSONException, IOException, URISyntaxException {
        SauceFactory sauceFactory = new SauceFactory();
        String response = sauceFactory.doREST(VERSION_CHECK_URL);
//        String response = IOUtils.toString(getClass().getResourceAsStream("/versions.json"));
        File jarFile = retrieveNewVersion(response);
        updatePluginJar(jarFile);
    }

    private int extractVersionFromResponse(String response) throws JSONException {
        JSONObject versionObject = new JSONObject(response);
        JSONObject sauceConnect2 = versionObject.getJSONObject("Sauce Connect 2");
        String versionText = sauceConnect2.getString("version");
        //extract the last digits after the -
        String versionNumber = StringUtils.substringAfter(versionText, "-r");
        if (StringUtils.isBlank(versionNumber)) {
            //TODO throw an error
            return 0;
        } else {
            return Integer.parseInt(versionNumber);
        }
    }

    /**
     * Performs a HTTP GET to retrieve the contents of the download url (assumed to be a zip
     * file), then unzips the zip file to the file system.
     *
     * @param response
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public File retrieveNewVersion(String response) throws JSONException, IOException {
        //perform HTTP get for download_url
        String downloadUrl = extractDownloadUrlFromResponse(response);
        SauceFactory sauceFactory = new SauceFactory();
        byte[] bytes = sauceFactory.doHTTPGet(downloadUrl);
//        byte[] bytes = FileUtils.readFileToByteArray(new File("C:/Sauce-Connect.zip"));
        //unzip contents to temp directory
        return unzipByteArray(bytes);
    }

    /**
     * Extracts the contents of the byte array to the temp drive.
     *
     * @param byteArray
     * @return a {@link File} instance pointing to the Sauce Connect jar file
     */
    private File unzipByteArray(byte[] byteArray) {
        File destPath = new File(System.getProperty("java.io.tmpdir"));

        File jarFile = null;
        try {
            ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(byteArray));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                int count;
                byte data[] = new byte[BUFFER];
                // write the files to the disk

                File destFile = new File(destPath, entry.getName());
                if (!destFile.getParentFile().exists()) {
                    boolean result = destFile.getParentFile().mkdirs();
                    if (!result) {
                        logger.error("Unable to create directories, attempting to continue");
                    }
                }

                FileOutputStream fos = new FileOutputStream(destFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
                if (entry.getName().endsWith("jar")) {
                    jarFile = destFile;
                }
            }
            zis.close();
        }
        catch (IOException e) {
            logger.error("Error unzipping contents", e);
        }
        return jarFile;
    }


    /**
     * Retrieves the download url from the JSON response
     *
     * @param response
     * @return String representing the URL to use to download the sauce connect jar
     * @throws JSONException thrown if an error occurs during the parsing of the JSON response
     */
    private String extractDownloadUrlFromResponse(String response) throws JSONException {
        JSONObject versionObject = new JSONObject(response);
        JSONObject sauceConnect2 = versionObject.getJSONObject("Sauce Connect 2");
        return sauceConnect2.getString("download_url");
    }

    /**
     * Updates the Bamboo Sauce plugin jar file to include the updated Sauce Connect jar file.  We have to
     * use reflection in order to retrieve information about the plugin, as the plugin classes aren't available
     * to our class loader.
     * <p/>
     * We update both the running bamboo plugin jar file (which is contained in the BAMBOO_HOME/caches/plugins/transformed-plugins
     * directory) and the 'master' plugin jar file  (which is contained in BAMBOO_HOME/plugins).  This means that the update to
     * the plugin jar file won't require a restart of Bamboo in order to take effect, and will survive across restarts.
     *
     * @param newJarFile the updated sauce connect jar file
     * @throws IOException        thrown if an error occurs during the Jar file modification
     * @throws URISyntaxException thrown if an error occurs retrieving the URL for the Bamboo plugin Jar file
     */
    public void updatePluginJar(File newJarFile) throws IOException, URISyntaxException {
        File runningJarFile = new File
                (SauceLibraryManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        addFileToJar(runningJarFile, new TFile(newJarFile));
        Object plugin = pluginAccessor.getPlugin(PLUGIN_KEY);
        Class pluginClass = pluginAccessor.getPlugin(PLUGIN_KEY).getClass();
        try {
            //have to use reflection here as the plugin API classes aren't available to the plugin
            Method getPluginArtifactMethod = pluginClass.getDeclaredMethod("getPluginArtifact");
            Object pluginArtifact = getPluginArtifactMethod.invoke(plugin);
            Class pluginArtifactClass = pluginArtifact.getClass();
            Method toFileMethod = pluginArtifactClass.getDeclaredMethod("toFile");
            File originalJarFile = (File) toFileMethod.invoke(pluginArtifact);
            addFileToJar(originalJarFile, new TFile(newJarFile));
        } catch (NoSuchMethodException e) {
            throw new IOException("Unexpected error invoking plugin logic", e);
        } catch (InvocationTargetException e) {
            throw new IOException("Unexpected error invoking plugin logic", e);
        } catch (IllegalAccessException e) {
            throw new IOException("Unexpected error invoking plugin logic", e);
        }
    }

    /**
     * Adds the updatedSauceConnectJarFile to the pluginJarFile.
     *
     * @param pluginJarFile
     * @param updatedSauceConnectJarFile
     * @throws IOException
     */
    public void addFileToJar(File pluginJarFile, TFile updatedSauceConnectJarFile) throws IOException {

        TFile.setDefaultArchiveDetector(new TArchiveDetector("jar"));
        search(new TFile(pluginJarFile), updatedSauceConnectJarFile);
        TFile.umount();
    }

    /**
     * Iterates over the contents of the pluginJarFile to find the sauce-connect-3.0.jar file.
     *
     * @param jarFileEntry
     * @param updatedSauceConnectJarFile
     * @throws IOException
     */
    private void search(TFile jarFileEntry, TFile updatedSauceConnectJarFile) throws IOException {
        if (jarFileEntry.getName().endsWith("sauce-connect-3.0.jar")) {
            update(jarFileEntry, updatedSauceConnectJarFile);
        } else if (jarFileEntry.isDirectory()) {
            for (TFile member : jarFileEntry.listFiles())
                search(member, updatedSauceConnectJarFile);
        }
    }

    /**
     * Updates the plugin jar file to include the updatedSauceConnectJarFile.
     *
     * @param pluginJarFile
     * @param updatedSauceConnectJarFile
     * @throws IOException
     */
    private void update(TFile pluginJarFile, TFile updatedSauceConnectJarFile) throws IOException {
        updatedSauceConnectJarFile.cp_rp(pluginJarFile);
    }

    public void setAdministrationConfigurationManager(AdministrationConfigurationManager administrationConfigurationManager) {
        this.administrationConfigurationManager = administrationConfigurationManager;
    }

    public SauceFactory getSauceAPIFactory() {
        if (sauceAPIFactory == null) {
            setSauceAPIFactory(new SauceFactory());
        }
        return sauceAPIFactory;
    }

    public void setSauceAPIFactory(SauceFactory sauceAPIFactory) {
        this.sauceAPIFactory = sauceAPIFactory;
    }

    public PluginAccessor getPluginAccessor() {
        return pluginAccessor;
    }

    public void setPluginAccessor(PluginAccessor pluginAccessor) {
        this.pluginAccessor = pluginAccessor;
    }
}
