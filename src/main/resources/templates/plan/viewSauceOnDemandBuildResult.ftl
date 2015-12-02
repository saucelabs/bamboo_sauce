<html>
<head>
    <title>Sauce Results</title>
    <meta name="decorator" content="result">
    <meta name="tab" content="sauce"/>
</head>
<body>

<h3>Sauce Results</h3>
[#if jobInformation?exists ]
<p span="viewSauceOnDemandBuildResult">
    The following Sauce Jobs were executed as part of this build:
</p>
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
                <a href="build/result/viewSauceJobResult.action?jobId=${job.jobId}&buildKey=${buildKey}&buildNumber=${buildNumber}">${job.jobId}</a>
            </td>
            <td>
                <a href="${from.urlName}/jobReport?jobId=${job.getJobId()}">${job.getName()}</a>
            </td>
            <td>${job.getOs()} ${job.getBrowser()} ${job.getVersion()}</td>
            <td>${job.getStatus()}</td>
            <td>
                <a href="${job.getVideoUrl()}">Video</a>
                -
                <a href="${job.getLogUrl()}">Logs</a>
            </td>
        </tr>
    [/#list]
</table>

[#else]
<p>
    Unable to find a Sauce Job result for ${buildKey}.
</p>

<p>Please verify that your Sauce tests are applying the value of the SAUCE_BAMBOO_BUILDNUMBER environment variable
    to the
    selenium context, eg.
</p>
    <pre>
    String bambooData = System.getenv("SAUCE_BAMBOO_BUILDNUMBER");
    this.selenium.setContext(bambooData);
    </pre>

[/#if]
</body>
</html>