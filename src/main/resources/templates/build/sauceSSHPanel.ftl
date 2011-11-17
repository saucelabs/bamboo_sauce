[#assign createMode=req.servletPath.contains('/build/admin/create/') /]
<div id="sauceSSHPanel" class="sauce_panel">
    <div class="sauce_panel_header">Sauce Connect Settings</div>
    <div class="sauce_panel_box">
        <div class="helpTextArea">
            <span>
                One of the advantages to Sauce Labs' OnDemand setup is that all the code remains on your server, snug and secure behind whatever protection you feel is best.<br/><br>

                To enable access from our cloud to a specific test server on your private network, Bamboo Sauce uses encrypted ssh tunnels which you can enable/configure below.<br/><br/>

                <strong>SSH Host</strong> - The *internal* server you want to test using our cloud. (i.e. localhost)<br/>
                <strong>SSH Port(s)</strong> - The *internal* ports you want to test using our cloud. (comma delimited list)<br/>

                Once you create a tunnel the domain will be accessible to your test scripts as if it's a site on the internet.<br/>
                So, for example, if I choose my domain to be "example.org", I'd use that domain name in my test scripts to access the private server.
            </span>
        </div>

        [@ww.checkbox label='Enable Sauce Connect' name='custom.sauceondemand.ssh.enabled' toggle='true' description='Invokes Sauce Connect to run Selenium tests within a SSH Tunnel' /]

        [@ui.bambooSection dependsOn='custom.sauceondemand.ssh.enabled' showOn='true']
            [@ww.textfield name='custom.sauceondemand.ssh.local.host' label='Local Host' description="The local hostname"/]<br clear="all"/>
            [@ww.textfield name='custom.sauceondemand.ssh.local.ports' label='Local Port' description="The local port" /]<br clear="all"/>
        [/@ui.bambooSection]
        <div class="clearer"></div>
    </div>
</div>