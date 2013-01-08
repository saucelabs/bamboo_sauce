<html>
<head>
    <title>Configure Sauce OnDemand</title>
    <meta name="decorator" content="adminpage">
</head>
<body>
<img src="${req.contextPath}/download/resources/com.saucelabs.bamboo.bamboo-sauceondemand-plugin:sauceImages/sauce_labs_horizontal.png" border="0"/>
<h1>Sauce OnDemand Configuration</h1>

<div class="paddedClearer"></div>
    [@ww.form action="/admin/sauceondemand/configureSauceOnDemandSave.action"
        id="sauceOnDemandConfigurationForm"
        submitLabelKey='global.buttons.update'
        cancelUri='/admin/administer.action']

        [@ui.bambooSection title="Credentials"]
            [@ww.textfield name='username' label='User Name' /]
            [@ww.textfield name="accessKey" label='Access Key' /]
        [/@ui.bambooSection]

        [@ui.bambooSection title="Proxy configuration"]
            [@ww.textfield name='proxyHost' label='Host' /]
            [@ww.textfield name='proxyPort' label='Port' /]
            [@ww.textfield name='proxyUsername' label='User Name' /]
            [@ww.password name='proxyPassword' label='Password' /]
        [/@ui.bambooSection]

    [/@ww.form]

    [@ui.bambooSection title="Check for Updates"]
        <a href="checkSauceConnectVersion.action">Check for updates to Sauce Connect</a>
    [/@ui.bambooSection]

</body>
</html>