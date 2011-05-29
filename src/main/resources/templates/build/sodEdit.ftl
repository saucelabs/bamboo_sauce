${webResourceManager.requireResource("com.saucelabs.bamboo.bamboo-sauceondemand-plugin:sodCSS")}
${webResourceManager.requireResource("com.saucelabs.bamboo.bamboo-sauceondemand-plugin:sodJS")}
[#include "sodScript.ftl"]
<!--[if IE 7]>
<style type="text/css">
    #sauceTabs > ul > li.selected{
        border-right: 1px solid #fff !important;
    }
    #sauceTabs > ul > li {
        border-right: 1px solid #ddd !important;
    }
    #sauceTabs > div {
        z-index: -1 !important;
        left:1px;
    }
</style>
<![endif]-->

[@ui.bambooSection title='<img src="${req.contextPath}/download/resources/com.saucelabs.bamboo.bamboo-sauceondemand-plugin:sauceImages/sauce_icon.jpg" />&nbsp;&nbsp;Sauce OnDemand' ]
    [#assign createMode=req.servletPath.contains('/build/admin/create/') /]

    [#if hasValidSauceConfig]
        [@ww.checkbox label='Enable Sauce OnDemand' name='custom.sauceondemand.enabled' toggle='true' description='Enables Selenium testing with Sauce OnDemand' /]

        [@ui.bambooSection dependsOn='custom.sauceondemand.enabled' showOn='true']

        <div id="sauceBuilderTabs">
            <ul>
                <li>General Settings</li>
                <li>Video & Profile</li>
                <li>SSH Tunneling</li>
                [#-- <li>User Extensions</li> --]
                <li>Environment Vars</li>
            </ul>

            [#include "sauceGeneralPanel.ftl"]
            [#include "sauceVideoPanel.ftl"]
            [#include "sauceSSHPanel.ftl"]
            [#-- [#include "sauceExtensionPanel.ftl"] --]
            [#include "sauceVariablePanel.ftl"]
        </div>


        [/@ui.bambooSection]
    [#else]
        <div class="warningBox">You must configure your <a href="${req.contextPath}/admin/sauceondemand/configureSauceOnDemand.action">Sauce OnDemand account settings</a> in the Bamboo Administration area to use this plugin.</div>
    [/#if]
[/@ui.bambooSection]
