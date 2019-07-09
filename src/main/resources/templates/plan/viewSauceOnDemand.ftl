<html>
<head>
    <title>Sauce Results</title>
    <meta name="decorator" content="plan">
</head>
<body>
[@cp.resultsSubMenu selectedTab='sauce' /]

[#if jobInformation?exists ]
<table style="width: 100%">
    <tr>
      <th align="left">Job Name</th>
      <th align="left">OS/Browser</th>
      <th align="left">Pass/Fail</th>
      <th align="left">Job Links</th>
    </tr>
    [#list jobInformation as job]
        <tr>
            <td>
                <a href="build/result/viewSauceJobResult.action?job=${job.jobId}">${job.name}</a>
            </td>
            <td>
                <a href="build/result/viewSauceJobResult.action?job=${job.jobId}">${job.jobId}</a>
            </td>
            <td>${job.getOs()} ${job.getBrowser()} ${job.getVersion()}</td>
            <td>${job.getStatus()!"complete"}</td>
            <td>
                [#if job.getVideoUrl().indexOf('us-east') == -1]
                    <a href="${job.getVideoUrl().replace('.flv','.mp4')}?auth=${job.hmac}">Video</a>
                    -
                [/#if]
                <a href="${job.getLogUrl()}?auth=${job.hmac}">Logs</a>
            </td>
        </tr>
    [/#list]
</table>
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