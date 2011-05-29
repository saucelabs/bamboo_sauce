[#assign createMode=req.servletPath.contains('/build/admin/create/') /]
<div id="sauceVariablePanel" class="sauce_panel">
    <div class="sauce_panel_header">Environment Variables</div>
    <div class="sauce_panel_box">
        <div class="helpTextArea">
            <span>
                When Sauce OnDemand runs in the course of a build, it sets several environment variables that can be used to configure Selenium in your tests:
            </span>
        </div>

        [@ui.bambooSection title='Variables for use with DefaultSelenium']
        <div class="helpTextArea">
            <strong>SELENIUM_HOST</strong> - The hostname of the selenium server<br/><br/>
            <strong>SELENIUM_PORT</strong> - The port of the selenium server<br/><br/>
            <strong>SELENIUM_BROWSER</strong> - The browser string. For Sauce OnDemand this is the JSON configuration<br/><br/>
            <strong>SELENIUM_URL</strong> - The initial URL to load when the test begins<br/><br/>
            <strong>SAUCE_HOST</strong> - The hostname to use in test urls<br/><br/>
        </div>
        [/@ui.bambooSection]

        [@ui.bambooSection title='Variables for use with SeleniumFactory']
        <div class="helpTextArea">
            <strong>SELENIUM_DRIVER</strong> - Auto-discovered by SeleniumFactory to configure Sauce OnDemand<br/><br/>
            <strong>SELENIUM_URL.url</strong> - The initial URL to load when the test begins (auto-discovered)<br/><br/>
            <strong>SAUCE_HOST</strong> - The hostname to use in test urls<br/><br/>
        </div>
        [/@ui.bambooSection]


        <div class="clearer"></div>
    </div>
</div>