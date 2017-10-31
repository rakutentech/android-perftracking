# Performance Tracking

Measure performance of android applications.
 
## How it works

A gradle plugin instruments android applications code, adding measurement calls to the UI 
lifecycle, network connections and a few standard listeners. These measurement calls are collected
by a runtime component and sent to a backend periodically.

The modules are:

**Core** 
* Annotations & configuration for instrumentation, packages `core.annotations`, `core.base`, 
`core.detours`, `core.mixins` and `core.wrappers`
* Measurement collection & sending to backend, `core` package

**Stubs**
* Stubs of and instrumented APIs

**Plugin**
* Android Gradle Transformation that instruments the application code using [ASM](http://asm.ow2.org/)

**Runtime**
* Fetches configuration from backend & starts collection of measurements
* Runtime API for applications to start and prolong measurements and metrics

## Prerequisite

Add the following to your environment's global `gradle.properties`

```
DEFAULT_CONFIG_URL_PREFIX="url to your configuration server"
DEFAULT_LOCATION_URL_PREFIX="url to your location server"
```

## How to build it

```bash
$ git submodule init
& git submodule update
$ ./gradlew Runtime:assemble Plugin:assemble
```

## How to use it

Currently we do not host a public APIs but you can fork it and use your own (dummy) apis 

## Contributing

See [Contribution guidelines](./CONTRIBUTING.md)