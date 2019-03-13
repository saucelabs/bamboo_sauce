package com.saucelabs.bamboo.sod.admin.action;

import com.atlassian.bamboo.configuration.AdministrationConfiguration;
import com.atlassian.bamboo.configuration.AdministrationConfigurationAccessor;
import com.atlassian.bamboo.configuration.AdministrationConfigurationManager;
import com.atlassian.bamboo.configuration.AdministrationConfigurationPersister;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.bamboo.ww2.aware.permissions.GlobalAdminSecurityAware;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.component.ComponentLocator;
import com.google.common.base.Strings;
import com.opensymphony.xwork2.ActionContext;
import com.saucelabs.bamboo.sod.config.SODKeys;
import com.saucelabs.ci.BrowserFactory;
import org.apache.commons.lang.StringUtils;

/**
 * Handles the validation and storage of the Sauce administration variables.
 *
 * The values entered here apply to all projects in the Bamboo environment.
 *
 *
 * @author <a href="http://www.sysbliss.com">Jonathan Doklovic</a>
 * @author Ross Rowe
 */
public class ConfigureSODAction extends BambooActionSupport implements GlobalAdminSecurityAware
{

    /**
     * Populated via dependency injection.
     */
    private BrowserFactory sauceBrowserFactory;

    /**
     * Config
     */
    private String sauceConnectDirectory;
    private String username;
    private String accessKey;
    private String dataCenter;

    private Integer sauceConnectMaxRetries = 0;
    private Integer sauceConnectRetryWaitTime = 0;

    public ConfigureSODAction(PluginAccessor pluginAccessor
    ) {
        super();
        setAdministrationConfigurationAccessor(ComponentLocator.getComponent(AdministrationConfigurationAccessor.class));
        setAdministrationConfigurationManager(ComponentLocator.getComponent(AdministrationConfigurationManager.class));
        setAdministrationConfigurationPersister(ComponentLocator.getComponent(AdministrationConfigurationPersister.class));
    }


    /**
     * Invoked when the Sauce Administration screen is opened, populates the underlying variables
     * with some default values.
     *
     * @return 'input'
     */
    public String doEdit() {
        final AdministrationConfiguration adminConfig = this.getAdministrationConfiguration();
        setUsername(adminConfig.getSystemProperty(SODKeys.SOD_USERNAME_KEY));
        setAccessKey(adminConfig.getSystemProperty(SODKeys.SOD_ACCESSKEY_KEY));
        setDataCenter(adminConfig.getSystemProperty(SODKeys.SOD_DATACENTER_KEY));
        setSauceConnectDirectory(adminConfig.getSystemProperty(SODKeys.SOD_SAUCE_CONNECT_DIRECTORY));
        setSauceConnectMaxRetries(adminConfig.getSystemProperty(SODKeys.SOD_SAUCE_CONNECT_MAX_RETRIES));
        setSauceConnectRetryWaitTime(adminConfig.getSystemProperty(SODKeys.SOD_SAUCE_CONNECT_RETRY_WAIT_TIME));
        return INPUT;
    }

    /**
     * Invoked when a user clicks 'Save' on the Sauce Administration screen.
     *
     * @return 'success'
     */
    public String doSave()
    {
        final AdministrationConfiguration adminConfig = this.getAdministrationConfiguration();
        adminConfig.setSystemProperty(SODKeys.SOD_USERNAME_KEY, getUsername());
        adminConfig.setSystemProperty(SODKeys.SOD_ACCESSKEY_KEY, getAccessKey());
        adminConfig.setSystemProperty(SODKeys.SOD_DATACENTER_KEY, getDataCenter());
        adminConfig.setSystemProperty(SODKeys.SOD_SAUCE_CONNECT_DIRECTORY, getSauceConnectDirectory());
        adminConfig.setSystemProperty(SODKeys.SOD_SAUCE_CONNECT_MAX_RETRIES, getSauceConnectMaxRetries().toString());
        adminConfig.setSystemProperty(SODKeys.SOD_SAUCE_CONNECT_RETRY_WAIT_TIME, getSauceConnectRetryWaitTime().toString());

        administrationConfigurationManager.saveAdministrationConfiguration(adminConfig);
        //this is a bit of a hack to support unit testing
        //getBamboo() won't be null at runtime, but we can't mock the method
        if (ActionContext.getContext() != null && ActionContext.getContext().getApplication() != null) {
            getBamboo().restartComponentsFollowingConfigurationChange();
        }

        addActionMessage(getText("config.updated"));
        return SUCCESS;
    }



    /**
     * Performs validation over the variables that are populated from the administration.
     * interface.
     */
    @Override
    public void validate()
    {
        if (StringUtils.isBlank(username))
        {
            addFieldError("username", "User Name is required.");
        }

        if (StringUtils.isBlank(accessKey))
        {
            addFieldError("accessKey", "Access Key is required.");
        }

        if (sauceConnectMaxRetries < 0) {
            addFieldError("sauceConnectMaxRetries", "Max Retries must be greater than or equal to 0");
        }

        if (sauceConnectRetryWaitTime < 0) {
            addFieldError("sauceConnectRetryWaitTime", "Max Retry Wait time must be greater than or equal to 0");
        }

        if (sauceConnectMaxRetries > 0 && sauceConnectRetryWaitTime == 0) {
            addFieldError("sauceConnectRetryWaitTime", "Wait time should be non zero when retries is set");
        }

    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getAccessKey()
    {
        return accessKey;
    }

    public void setAccessKey(String accesskey)
    {
        this.accessKey = accesskey;
    }

    public String getDataCenter()
    {
        return dataCenter;
    }

    public void setDataCenter(String dataCenter)
    {
        this.dataCenter = dataCenter;
    }

    public void setSauceConnectDirectory(String sauceConnectDirectory) {
        this.sauceConnectDirectory = sauceConnectDirectory;
    }

    public String getSauceConnectDirectory() {
        return sauceConnectDirectory;
    }

    public Integer getSauceConnectMaxRetries() {
        return sauceConnectMaxRetries;
    }

    public void setSauceConnectMaxRetries(Integer sauceConnectMaxRetries) {
        this.sauceConnectMaxRetries = sauceConnectMaxRetries;
    }

    public void setSauceConnectMaxRetries(String sauceConnectMaxRetries) {
        this.sauceConnectMaxRetries = Strings.isNullOrEmpty(sauceConnectMaxRetries) ?
            0 : Integer.parseInt(sauceConnectMaxRetries, 10);
    }
    public Integer getSauceConnectRetryWaitTime() {
        return sauceConnectRetryWaitTime;
    }

    public void setSauceConnectRetryWaitTime(Integer sauceConnectRetryWaitTime) {
        this.sauceConnectRetryWaitTime = sauceConnectRetryWaitTime;
    }

    public void setSauceConnectRetryWaitTime(String sauceConnectRetryWaitTime) {
        this.sauceConnectRetryWaitTime = Strings.isNullOrEmpty(sauceConnectRetryWaitTime) ?
            0 : Integer.parseInt(sauceConnectRetryWaitTime, 10);
    }
}