[#assign createMode=req.servletPath.contains('/build/admin/create/') /]
<div id="sauceGeneralPanel" class="sauce_panel">
    <div class="sauce_panel_header">General Settings</div>
    <div class="sauce_panel_box">
        <div class="helpTextArea">
            <span>
                <strong>Selenium Version</strong> - Specifies the version of Selenium your tests are using. <br/><br/>
                <strong>Browser</strong> - Select a browser from our list of supported OS/browser/version combos. <br/><br/>
                <strong>Max Duration</strong> - As a safety measure to prevent broken tests from running indefinitely, you can limit the duration of tests. (input in seconds)<br/><br/>
                <strong>Idle Timeout</strong> - As another safety measure to prevent tests from running for a long time after a connection is lost. (input in seconds)<br/><br/>
                <strong>Starting Browser URL</strong> - The initial URL Selenium should load when running tests<br/><br/>
            </span>
        </div>
        [@ww.select label='Selenium Version' name='custom.sauceondemand.selenium.version'
                list="{'1', '2'}" description='The version of Selenium that will be run'/]
        [@ww.select label="Browser" description="The OS/Browser combination to use for testing" name="custom.sauceondemand.browser" listKey="key" listValue="name" list=browserList /]<br clear="all" />
        [@ww.textfield name='custom.sauceondemand.max-duration' label='Max Duration' description='The maximum time (in seconds) allotted to run tests' /]<br clear="all"/>
        [@ww.textfield name='custom.sauceondemand.idle-timeout' label='Idle Timeout' description='The maximum time (in seconds) for a test to wait for a command' /]<br clear="all"/>
        [@ww.textfield name='custom.sauceondemand.selenium.url' label='Starting Browser URL' description='The initial url the browser should load'/]
        <div class="clearer"></div>
    </div>
</div>