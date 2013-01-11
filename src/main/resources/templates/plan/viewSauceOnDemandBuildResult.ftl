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
<table>
    <tr>
        <th>Job Id</th>
        <th>Name</th>
        <th>Status</th>
    </tr>
    [#list jobInformation as jobInfo]
        <tr>
            <td>
                <a href="build/result/viewSauceJobResult.action?jobId=${jobInfo.jobId}&buildKey=${buildKey}&buildNumber=${buildNumber}">${jobInfo.jobId}</a>
            </td>
            <td>
                ${jobInfo.name}
            </td>
            <td>
                ${jobInfo.status}
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