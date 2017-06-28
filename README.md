# Bamboo Sauce OnDemand plugin.

## Development

Development depends on a working Docker environment with docker-compose. The `Dockerfile` at the root of this repository defines a Debian base with the java 8 jdk and Atlassian Plugin SDK.

You must first run docker-compose with `--build` to set up your build environment:

```
docker-compose up --build
```
Note: Subsequent `docker-compose up` shouldn't need `--build` unless you've changed `Dockerfile`.

Once the `atlas-plugin-sdk` container has been created and running, you can attach to the running container with:
```
docker-compose exec atlas-plugin-sdk bash
```

From that shell it should be possible to run the following commands:

To build (compile,test,jar) the plugin:

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

### Additional Useful Commands
```
# To start bamboo@version with default credentials
atlas-run --product bamboo --version 5.15.7 -Duser=admin -Dpassword=admin
```

## Releasing

```
atlas-release
```

I(Gavin)'ve personally `mvn release:prepare` and `mvn release:perform` but the atlas-release should do both and anything else they suggest

Once this is complete, grab the new versioned jar file from the target directory, and upload it to the atlassian marketplace (https://marketplace.atlassian.com/manage/plugins/com.saucelabs.bamboo.bamboo-sauceondemand-plugin/versions/create)
