[#assign createMode=req.servletPath.contains('/build/admin/create/') /]
<div id="sauceSSHPanel" class="sauce_panel">
    <div class="sauce_panel_header">SSH Tunnel Settings</div>
    <div class="sauce_panel_box">
        <div class="helpTextArea">
            <span>
                One of the advantages to Sauce Labs' OnDemand setup is that all the code remains on your server, snug and secure behind whatever protection you feel is best.<br/><br>

                To enable access from our cloud to a specific test server on your private network, Bamboo Sauce uses encrypted ssh tunnels which you can enable/configure below.<br/><br/>

                <strong>SSH Host</strong> - The *internal* server you want to test using our cloud. (i.e. localhost)<br/>
                <strong>SSH Port(s)</strong> - The *internal* ports you want to test using our cloud. (comma delimited list)<br/>
                <strong>Tunnel Port(s)</strong> - The external ports to map to your internal ports (must match length of ssh ports)<br/>
                <strong>SSH Domains</strong> - The list of domains to use in your tests.<br/><br/>

                Once you create a tunnel the domain will be accessible to your test scripts as if it's a site on the internet.<br/>
                So, for example, if I choose my domain to be "example.org", I'd use that domain name in my test scripts to access the private server.
            </span>
        </div>

        [@ww.checkbox label='Enable SSH Tunneling' name='custom.sauceondemand.ssh.enabled' toggle='true' description='Enables SSH Tunneling for Sauce OnDemand' /]

        [@ui.bambooSection dependsOn='custom.sauceondemand.ssh.enabled' showOn='true']
            [@ww.textfield name='custom.sauceondemand.ssh.local.host' label='Local Host' description="The local hostname"/]<br clear="all"/>
            [@ww.textfield name='custom.sauceondemand.ssh.local.ports' label='Local Port' description="The local port" /]<br clear="all"/>
            [@ww.textfield name='custom.sauceondemand.ssh.remote.ports' label='Remote Port' description="The remote port" /]<br clear="all"/>
            [@ww.checkbox label='Auto-Generate Domain' name='custom.sauceondemand.ssh.auto-domain' toggle='true' description='Auto-generates a domain to use for testing' /]<br clear="all"/>
            [@ww.textfield name='custom.sauceondemand.ssh.domains' label='Domain' description="The domain to make requests against" dependsOn='custom.sauceondemand.ssh.auto-domain' showOn='false'/]<br clear="all"/>
        [/@ui.bambooSection]
        <div class="clearer"></div>
    </div>
</div>