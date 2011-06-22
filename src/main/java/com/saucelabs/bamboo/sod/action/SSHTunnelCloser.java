package com.saucelabs.bamboo.sod.action;

import com.atlassian.bamboo.build.CustomBuildProcessor;
import com.atlassian.bamboo.v2.build.BaseConfigurablePlugin;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.saucelabs.bamboo.sod.config.SODMappedBuildConfiguration;
import com.saucelabs.bamboo.sod.util.SauceTunnelManager;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Closes any open SSH tunnels for the Build Plan at the completion of a build.  As this class implements
 * {@link com.atlassian.bamboo.build.CustomBuildProcessor}, this class will be invoked on the Bamboo Agent side.
 *
 * @author <a href="http://www.sysbliss.com">Jonathan Doklovic</a>
 * @author Ross Rowe
 */
public class SSHTunnelCloser extends BaseConfigurablePlugin implements CustomBuildProcessor {

    private static final Logger logger = Logger.getLogger(SSHTunnelCloser.class);

    private BuildContext buildContext;

    /**
     * Populated via dependency injection.
     */
    private SauceTunnelManager sauceTunnelManager;

    /**
     * Closes any SSH tunnels open for the Plan Key (retrieved from {@link com.atlassian.bamboo.v2.build.BuildContext#getPlanKey()}),
     * and sets the temp username and api key to be blank strings.
     * <p/>
     *
     * @return
     * @throws InterruptedException
     * @throws Exception
     */
    @NotNull
    public BuildContext call() {
        try {
            assert buildContext != null;
            final SODMappedBuildConfiguration config = new SODMappedBuildConfiguration(buildContext.getBuildDefinition().getCustomConfiguration());
            getSauceTunnelManager().closeTunnelsForPlan(buildContext.getPlanKey());
            config.setTempUsername("");
            config.setTempApikey("");
        } catch (Exception e) {
            //catch exceptions so that we don't stop the build from running
            logger.error("Error running Sauce OnDemand SSHTunnelCloser, attempting to continue", e);
        }

        return buildContext;
    }

    public void init(@NotNull BuildContext buildContext) {
        this.buildContext = buildContext;
    }

    public void setSauceTunnelManager(SauceTunnelManager sauceTunnelManager) {
        this.sauceTunnelManager = sauceTunnelManager;
    }

    public SauceTunnelManager getSauceTunnelManager() {
        if (sauceTunnelManager == null) {
            //this will occur when a remote agent runs, as it doesn't have Spring components available
            setSauceTunnelManager(SauceTunnelManager.getInstance());
        }
        return sauceTunnelManager;
    }
}
