<html>
<head>
    <title>Sauce OnDemand Results</title>
    <meta name="decorator" content="plan">
</head>
<body>
[@cp.resultsSubMenu selectedTab='sauce' /]

[#if jobInformation?exists ]
[#list jobInformation as jobInfo]
<script type="text/javascript" src="http://saucelabs.com/job-embed/${jobInfo.jobId}.js?auth=${jobInfo.hmac}"></script>
[/#list]
[#else]

<p>
Unable to find a Sauce Job result for ${buildKey}.
</p>

<p>Please verify that your Sauce tests are applying the value of the SAUCE_CUSTOM_DATA environment variable to the
selenium context, eg.
</p>

<pre>
String bambooData = System.getProperty("SAUCE_CUSTOM_DATA");
this.selenium.setContext(bambooData);
</pre>



[/#if]
</body>
</html>