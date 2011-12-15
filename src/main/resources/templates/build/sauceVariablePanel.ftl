[#assign createMode=req.servletPath.contains('/build/admin/create/') /]
<div id="sauceVariablePanel" class="sauce_panel">
    <div class="sauce_panel_header">Environment Variables</div>
    <div class="sauce_panel_box">
        <div class="helpTextArea">
            <span>
                When the Sauce OnDemand plugin runs in the course of a build, it sets several environment variables that are used to configure Selenium in your tests.  These variables can be referenced by running the following (in Java):
                <pre>System.getenv("SELENIUM_HOST")</pre>
 For more information, visit the <a href="https://studio.plugins.atlassian.com/wiki/display/BSAD">Sauce OnDemand plugin home page</a>
            </span>

        </div>

 		[@ui.bambooSection title='Variables for use with WebDriver']
        <div class="helpTextArea">
            <strong>SELENIUM_PLATFORM</strong> - The name of the operating system for the selected browser (eg. VISTA, LINUX)<br/><br/>
            <strong>SELENIUM_BROWSER</strong> - The name of the selected browser<br/><br/>
			<strong>SELENIUM_VERSION</strong> - The version number of the selected browser<br/><br/>
        </div>
        [/@ui.bambooSection]

        [@ui.bambooSection title='Variables for use with DefaultSelenium']
        <div class="helpTextArea">
            <strong>SELENIUM_HOST</strong> - The hostname of the selenium server<br/><br/>
            <strong>SELENIUM_PORT</strong> - The port of the selenium server<br/><br/>
            <strong>SELENIUM_BROWSER</strong> - The browser string. For Sauce OnDemand this is the JSON configuration<br/><br/>
            <strong>SELENIUM_URL</strong> - The initial URL to load when the test begins<br/><br/>
            <strong>SAUCE_HOST</strong> - The hostname to use in test urls<br/><br/>
            <strong>SAUCE_BAMBOO_BUILDNUMBER</strong> - The identifier for the Bamboo build being executed. This is used to correlate a Sauce job with a Bamboo build.  The value should be used as part of a call to setContext(), eg.
<pre>
String bambooData = System.getenv("SAUCE_BAMBOO_BUILDNUMBER");
this.selenium.setContext(bambooData);
</pre>
        </div>
        [/@ui.bambooSection]

        [@ui.bambooSection title='Variables for use with <a href="http://selenium-client-factory.infradna.com/">SeleniumFactory</a>']
        <div class="helpTextArea">
            <strong>SELENIUM_DRIVER</strong> - Auto-discovered by SeleniumFactory to configure Sauce OnDemand<br/><br/>
            <strong>SELENIUM_URL</strong> - The initial URL to load when the test begins (auto-discovered)<br/><br/>
            <strong>SAUCE_HOST</strong> - The hostname to use in test urls<br/><br/>
        </div>
        [/@ui.bambooSection]


        <div class="clearer"></div>
    </div>
</div>
