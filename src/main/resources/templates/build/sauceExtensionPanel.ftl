[#assign createMode=req.servletPath.contains('/build/admin/create/') /]
<div id="sauceExtensionPanel" class="sauce_panel">
    <div class="sauce_panel_header">User Extensions</div>
    <div class="sauce_panel_box">
        <div class="helpTextArea">
            <span>
                The JIRA Project Key is used to tie this build plan to a specific JIRA project.<br/><br/>
                The "allow release builds" flag controls if this plan can build releases or not.
            </span>
        </div>

       extensions
        <div class="clearer"></div>
    </div>
</div>