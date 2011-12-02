package it.com.saucelabs.bamboo.sod;

import org.junit.Ignore;
import org.junit.Test;

public class InvokeBuildTest  {

    /**
     * We use HtmlUnit for this test, rather than Selenium/Sauce Connect, as we want to invoke a build that will itself run
     * a Selenium test under Sauce OnDemand.  Once the build is completed, we verify that it was successful by connecting
     * to Sauce OnDemand to see if the corresponding Sauce Job exists. 
     * @throws Exception
     */
    @Test
    @Ignore
    public void saucePlan() throws Exception {
        //using HtmlUnit, log in

        //
    }
}
