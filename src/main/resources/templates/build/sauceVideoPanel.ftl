[#assign createMode=req.servletPath.contains('/build/admin/create/') /]
<div id="sauceVideoPanel" class="sauce_panel">
    <div class="sauce_panel_header">Video & Firefox Profile</div>
    <div class="sauce_panel_box">
        <div class="helpTextArea">
            <span>
                <strong>Record Video</strong> - Sauce Labs records a video of every test you run, however, there is a small performance penalty for screen recording during a test run. You can avoid this penalty by optionally turning off the recording feature. <br/><br/>
                <strong>Firefox Profile URL</strong> -  Custom Firefox profiles are available; allowing you to customize the Firefox browser on the OnDemand service.<br/><br/>

                    The profile needs to be formatted as the zipped contents of the Firefox profile directory you wish to use. Additionally, it needs to be served from an accessible URL<br/><br/>
            </span>
        </div>

        [@ww.checkbox name='custom.sauceondemand.record-video' label='Record Video?' description='Will record video of the test when checked.' toggle="true"/]<br clear="all"/>
        [@ww.textfield name='custom.sauceondemand.firefox-profile' label='Firefox Profile URL' description='The url to a custom firefox profile zip.' /]

        <div class="clearer"></div>
    </div>
</div>