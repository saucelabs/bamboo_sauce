<html>
<head>
    <title>Sauce Job Result</title>
    <meta name="decorator" content="result">
    <meta name="tab" content="sauce"/>
</head>
<body>
[#if jobInformation?exists ]
<h3>Sauce Results for ${jobInformation.jobId}</h3>

<iframe src="${sauceREST.appServer}job-embed/${jobInformation.jobId}?auth=${jobInformation.hmac}" style="width: 1024px; height: 1000px;"></iframe>

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