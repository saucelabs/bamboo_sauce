[#assign createMode=req.servletPath.contains('/build/admin/create/') /]
<div id="sauceGeneralPanel" class="sauce_panel">
    <div class="sauce_panel_header">General Settings</div>
    <div class="sauce_panel_box">
        <div class="helpTextArea">
            <span>
                One of the advantages to Sauce Labs' OnDemand setup is that all the code remains on your server, snug and secure behind whatever protection you feel is best.

                To enable access from our cloud to a specific test server on your private network, enable <a
                    href="http://saucelabs.com/docs/sauce-connect">Sauce Connect</a> below.</span>
        </div>
    [@ww.checkbox label='Enable Sauce Connect' name='custom.sauceondemand.ssh.enabled' toggle='true' description='Enabling Sauce Connect will launch a SSH tunnel, which creates a secure and reliable tunnel from our cloud to your private network that can only be accessed by you' /]

    [@ww.checkbox label='Verbose Sauce Connect Logging' name='custom.sauceondemand.ssh.verbose' toggle='false' description='If selected, the output from the Sauce Connect process will be included in the console output for the Jenkins job' /]

    [@ww.checkbox label='Override Default Authentication' name='custom.sauceondemand.auth.enabled' toggle='true' description='Specify a different username/access key for this build' /]

    [@ui.bambooSection dependsOn='custom.sauceondemand.auth.enabled' showOn='true']
        [@ww.textfield name='custom.sauceondemand.user_name' label='User Name' description="The Sauce username"/]
        <br clear="all"/>
        [@ww.textfield name='custom.sauceondemand.access_key' label='Access Key' description="The Sauce access key" /]
        <br
                clear="all"/>
    [/@ui.bambooSection]

    [@ww.checkbox label='Selenium RC Support?' name='custom.sauceondemand.seleniumrc' toggle='true' description='' /]

    [@ui.bambooSection dependsOn='custom.sauceondemand.seleniumrc' showOn='false']
        [@ww.select
        label="WebDriver Browser(s)" description="The OS/Browser combination to use for testing with WebDriver"
        name="custom.sauceondemand.browser"
        id="custom.sauceondemand.browser"
        listKey="key"
        listValue="name"
        list=webDriverBrowserList
        multiple="true"
        value="selectedBrowsers"
        size="20"
        cssStyle="max-width: 100%"/]
    [/@ui.bambooSection]

    [@ui.bambooSection dependsOn='custom.sauceondemand.seleniumrc' showOn='true']
        [@ww.select
        label="Selenium RC Browser(s)" description="The OS/Browser combination to use for testing with WebDriver"
        name="custom.sauceondemand.rcbrowser"
        id="custom.sauceondemand.rcbrowser"
        listKey="key"
        listValue="name"
        list=seleniumRCBrowserList
        multiple="true"
        value="selectedRCBrowsers"
        size="20"
        cssStyle="max-width: 100%"/]
    [/@ui.bambooSection]

    [#--[@ww.checkbox label='Appium Support?' name='custom.sauceondemand.appium' toggle='true' description='' /]--]

    [#--[@ui.bambooSection dependsOn='custom.sauceondemand.appium' showOn='true']--]
    [#--[@ww.select--]
    [#--label="Appium Browser(s)" description="The OS/Browser combination to use for testing with Appium"--]
    [#--name="custom.sauceondemand.browser"--]
    [#--listKey="key"--]
    [#--listValue="name"--]
    [#--list=appiumBrowserList--]
    [#--multiple="true"--]
    [#--value="selectedBrowsers"--]
    [#--size="10"--]
    [#--cssStyle="height: 160px; width: 600px;"/]--]
    [#--[/@ui.bambooSection]--]

        <br clear="all"/>
    [@ww.textfield name='custom.sauceondemand.max-duration' label='Max Duration' description='The maximum time (in seconds) allotted to run tests' /]
        <br clear="all"/>
    [@ww.textfield name='custom.sauceondemand.idle-timeout' label='Idle Timeout' description='The maximum time (in seconds) for a test to wait for a command' /]
        <br clear="all"/>
    [@ww.textfield name='custom.sauceondemand.selenium.url' label='Starting Browser URL' description='The initial url the browser should load'/]
        <div class="clearer"></div>


    [@ww.textfield name='custom.sauceondemand.selenium.host' label='Selenium Host' description="The name of the Selenium host to be used.  For tests run using Sauce Connect, this should be localhost.
        ondemand.saucelabs.com can also be used to connect directly to Sauce."/]
        <br clear="all"/>
    [@ww.textfield name='custom.sauceondemand.selenium.port' label='Selenium Port' description="The name of the Selenium Port to be used.  For tests run using Sauce Connect, this should be 4445.  If using ondemand.saucelabs.com for the Selenium Host, then use 4444." /]
        <br
                clear="all"/>

        <br clear="all"/>
    [@ww.textfield name='custom.sauceondemand.sauceConnectOptions' label='Sauce Connect Options' description='Additional command line options for Sauce Connect' /]

    </div>
</div>