[#assign createMode=req.servletPath.contains('/build/admin/create/') /]
<div id="sauceGeneralPanel" class="sauce_panel">
    <div class="sauce_panel_header">General Settings</div>
    <div class="sauce_panel_box">
        <div class="helpTextArea">
            <span>
                One of the advantages to Sauce Labs' OnDemand setup is that all the code remains on your server, snug and secure behind whatever protection you feel is best.

                To enable access from our cloud to a specific test server on your private network, enable <a href="http://saucelabs.com/docs/sauce-connect">Sauce Connect</a> below.</span>
        </div>
        [@ww.checkbox label='Enable Sauce Connect' name='custom.sauceondemand.ssh.enabled' toggle='true' description='Invokes Sauce Connect to run Selenium tests within a SSH Tunnel' /]

        [@ww.select label='Selenium Version' name='custom.sauceondemand.selenium.version'
                list="{'1.x', '2.x'}" description='The version of Selenium that will be run'/]
        [@ww.select label="Browser" description="The OS/Browser combination to use for testing" name="custom.sauceondemand.browser" listKey="key" listValue="name" list=browserList /]<br clear="all" />
        [@ww.textfield name='custom.sauceondemand.max-duration' label='Max Duration' description='The maximum time (in seconds) allotted to run tests' /]<br clear="all"/>
        [@ww.textfield name='custom.sauceondemand.idle-timeout' label='Idle Timeout' description='The maximum time (in seconds) for a test to wait for a command' /]<br clear="all"/>
        [@ww.textfield name='custom.sauceondemand.selenium.url' label='Starting Browser URL' description='The initial url the browser should load'/]
        <div class="clearer"></div>
    </div>
</div>