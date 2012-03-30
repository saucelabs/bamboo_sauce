<html>
<head>
    <title>Sauce OnDemand Results</title>
    <meta name="decorator" content="result">
    <meta name="tab" content="sauce"/>
</head>
<body>

<h3>Sauce Results</h3>
[#if jobInformation?exists ]
<p span="viewSauceOnDemandBuildResult">
[#list jobInformation as jobInfo]
<iframe src="https://saucelabs.com/job-embed/${jobInfo.jobId}" style="width:1024px; height:420px;">
    <a href="https://saucelabs.com/job-embed/${jobInfo.jobId}">Your browser does not support iframes. Click here to see the embedded job results.</a>
</iframe>
[/#list]
</p>
[#else]
<p>
Unable to find a Sauce Job result for ${buildKey}.
</p>

<p>Please verify that your Sauce tests are applying the value of the SAUCE_BAMBOO_BUILDNUMBER environment variable to the
selenium context, eg.
</p>
<pre>
String bambooData = System.getenv("SAUCE_BAMBOO_BUILDNUMBER");
this.selenium.setContext(bambooData);
</pre>

[/#if]
</body>
</html>
