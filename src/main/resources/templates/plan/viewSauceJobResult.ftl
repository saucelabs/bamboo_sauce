<html>
<head>
    <title>Sauce OnDemand Job Result</title>
    <meta name="decorator" content="result">
    <meta name="tab" content="sauce"/>
</head>
<body>
[#if jobInformation?exists ]
<h3>Sauce Results for ${jobInformation.jobId}</h3>

<script type="text/javascript" src="https://saucelabs.com/job-embed/${jobInformation.jobId}.js?auth=${jobInformation.hmac}"></script>
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