package com.saucelabs.bamboo.sod.admin.action;

import com.atlassian.bamboo.configuration.ConfigurationAction;
import com.atlassian.bamboo.configuration.SystemInfo;
import com.saucelabs.bamboo.sod.util.SauceLibraryManager;
import org.apache.log4j.Logger;

/**
 * Handles the invocation of update checks for the Sauce Connect library. 
 * @author Ross Rowe
 */
public class CheckSauceConnectVersionAction extends ConfigurationAction {

    private static final Logger logger = Logger.getLogger(CheckSauceConnectVersionAction.class);
    
    private SystemInfo systemInfo;
    
    /**
     * Populated via dependency injection.
     */
    private transient SauceLibraryManager sauceLibraryManager;

    /**
     * Invoked when the 'Check for updates to Sauce Connect' link is clicked.  
     * @return 'success' if a new version of Sauce Connect is available, 'none' if no new versions are available, 
     * 'error' if an error occurs
     * @throws Exception
     */
    @Override
    public String doDefault() throws Exception {
        try {
            boolean laterVersionAvailable = sauceLibraryManager.checkForLaterVersion();
            if (laterVersionAvailable) {
                return SUCCESS;
            } else {
                addActionMessage("No update required, Sauce Connect is up to date");
                return NONE;
            }
        } catch (Exception e) {
            logger.error("Exception generated when attempting to check for updates", e);
            addErrorMessage("An error occurred when attempting to check for updates: " + e.getMessage());
        }
        return ERROR;
    }

    public void setSauceLibraryManager(SauceLibraryManager sauceLibraryManager) {
        this.sauceLibraryManager = sauceLibraryManager;
    }

    /**
     * Invoked when a user clicks 'Submit' on the Update Sauce Connect screen.
     *
     * @return 'success' if we were able to update the plugin jar file, 'error' if an exception occurred
     */
    public String doSubmit() {
        try {
            sauceLibraryManager.triggerReload();
            addActionMessage("Update of the Sauce Connect library was successful, please restart Bamboo");
            return SUCCESS;
        } catch (Exception e) {
            logger.error("Exception generated when attempting to apply updates", e);
            addErrorMessage("An error occurred when attempting to apply updates: " + e.getMessage());
        }
        return ERROR;
    }
    
    
    @Override
    public void validate()
    {
    }

    public SystemInfo getSystemInfo() {
        return systemInfo;
    }

    public void setSystemInfo(SystemInfo systemInfo) {
        this.systemInfo = systemInfo;
    }

}
