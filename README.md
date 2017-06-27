# Bamboo Sauce OnDemand plugin.

## Development

You'll want to make sure you have the Atlassian Dev environment and SDK installed (https://developer.atlassian.com/docs/getting-started/set-up-the-atlassian-plugin-sdk-and-build-a-project)

To build (compile,test,jar) the plugin run: 

```
atlas-package
```

To run a local instance of Bamboo with the plugin (and its dependencies installed), run:

```
atlas-run
```

To run the local instance of Bamboo in debug mode, run:

```
atlas-debug
```

## Releasing

```
atlas-release
```

I(Gavin)'ve personally `mvn release:prepare` and `mvn release:perform` but the atlas-release should do both and anything else they suggest

Once this is complete, grab the new versioned jar file from the target directory, and upload it to the atlassian marketplace (https://marketplace.atlassian.com/manage/plugins/com.saucelabs.bamboo.bamboo-sauceondemand-plugin/versions/create)
