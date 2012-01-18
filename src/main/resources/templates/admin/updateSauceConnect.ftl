<html>
<head>
    <title>Configure Sauce OnDemand</title>
    <meta name="decorator" content="adminpage">
</head>
<body>
<img src="${req.contextPath}/download/resources/com.saucelabs.bamboo.bamboo-sauceondemand-plugin:sauceImages/sauce_labs_horizontal.png" border="0"/>
<h1>Sauce Connect Update</h1>

<div class="paddedClearer"></div>

   A new version of the <a href="http://saucelabs.com/docs/sauce-connect">Sauce Connect</a> library is available.  Click 'Update Sauce Connect' to install it within Bamboo.  Note: the Sauce Connect
   library is approximately 30 Mb, so the update process might take some time.
   
   [@ww.form action="/admin/sauceondemand/checkSauceConnectVersionSubmit.action"
        id="sauceConnectForm"
        submitLabelKey='Update Sauce Connect'
        cancelUri='/admin/administer.action']

    [/@ww.form]
    
    
</body>
</html>