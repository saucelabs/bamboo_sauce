<html>
<head>
    <title>Sauce OnDemand Results</title>
    <meta name="decorator" content="plan">
</head>
<body>
[@cp.resultsSubMenu selectedTab='sauce' /]

[#if jobInformation?exists ]
<table>
    <tr>
        <th>Job Id</th>
        <th>Status</th>
    </tr>
    [#list jobInformation as jobInfo]
        <tr>
            <td>
                <a href="${req.contextPath}//build/result/viewSauceJobResult.action?job=${jobInfo.jobId}">${jobInfo.jobId}</a>
            </td>
            <td>

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