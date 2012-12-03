package com.saucelabs.bamboo.sod.util;

import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.atlassian.plugin.PluginAccessor;

import java.io.IOException;
import java.net.URISyntaxException;

//import de.schlichtherle.truezip.file.TFile;

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
public class BambooSauceLibraryManager {//extends SauceLibraryManager {

    /**
     * Populated via dependency injection.
     */
    private AdministrationConfigurationManager administrationConfigurationManager;

    private PluginAccessor pluginAccessor;

    /**
     * Populated by dependency injection.
     */
    private BambooSauceFactory sauceAPIFactory;

    private static final String PLUGIN_KEY = "com.saucelabs.bamboo.bamboo-sauceondemand-plugin";


    //@Override
    public boolean checkForLaterVersion() throws IOException, URISyntaxException {
        return false;
//        //retrieve contents of version url and parse as JSON
//        getSauceAPIFactory().setupProxy(administrationConfigurationManager);
//        return super.checkForLaterVersion();
    }

    //@Override
    public void triggerReload() throws IOException, URISyntaxException {
//        //retrieve contents of version url and parse as JSON
//        getSauceAPIFactory().setupProxy(administrationConfigurationManager);
//        super.triggerReload();
    }

    public void setAdministrationConfigurationManager(AdministrationConfigurationManager administrationConfigurationManager) {
        this.administrationConfigurationManager = administrationConfigurationManager;
    }

    public BambooSauceFactory getSauceAPIFactory() {
        if (sauceAPIFactory == null) {
            setSauceAPIFactory(new BambooSauceFactory());
        }
        return sauceAPIFactory;
    }

    public void setSauceAPIFactory(BambooSauceFactory sauceAPIFactory) {
        this.sauceAPIFactory = sauceAPIFactory;
    }

    public PluginAccessor getPluginAccessor() {
        return pluginAccessor;
    }

    public void setPluginAccessor(PluginAccessor pluginAccessor) {
        this.pluginAccessor = pluginAccessor;
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
     * @throws java.io.IOException        thrown if an error occurs during the Jar file modification
     * @throws java.net.URISyntaxException thrown if an error occurs retrieving the URL for the Bamboo plugin Jar file
     */
//    public void updatePluginJar(File newJarFile) throws IOException, URISyntaxException {
//        File runningJarFile = new File
//                (BambooSauceLibraryManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());
//        addFileToJar(runningJarFile, new TFile(newJarFile));
//        Object plugin = pluginAccessor.getPlugin(PLUGIN_KEY);
//        Class pluginClass = pluginAccessor.getPlugin(PLUGIN_KEY).getClass();
//        try {
//            //have to use reflection here as the plugin API classes aren't available to the plugin
//            Method getPluginArtifactMethod = pluginClass.getDeclaredMethod("getPluginArtifact");
//            Object pluginArtifact = getPluginArtifactMethod.invoke(plugin);
//            Class pluginArtifactClass = pluginArtifact.getClass();
//            Method toFileMethod = pluginArtifactClass.getDeclaredMethod("toFile");
//            File originalJarFile = (File) toFileMethod.invoke(pluginArtifact);
//            addFileToJar(originalJarFile, new TFile(newJarFile));
//        } catch (NoSuchMethodException e) {
//            throw new IOException("Unexpected error invoking plugin logic", e);
//        } catch (InvocationTargetException e) {
//            throw new IOException("Unexpected error invoking plugin logic", e);
//        } catch (IllegalAccessException e) {
//            throw new IOException("Unexpected error invoking plugin logic", e);
//        }
//    }

}
