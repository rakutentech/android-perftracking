# Performance Tracking
[![codecov](https://codecov.io/gh/rakutentech/android-perftracking/branch/master/graph/badge.svg)](https://codecov.io/gh/rakutentech/android-perftracking)
[![CircleCI](https://circleci.com/gh/rakutentech/android-perftracking.svg?style=svg)](https://circleci.com/gh/rakutentech/android-perftracking)

:skull: *This product was shut down internally at the end of 2020. The repo is now archived.*

Measure performance of android applications.
 
## How it works

A gradle plugin instruments android applications code, adding measurement calls to the UI 
lifecycle, network connections and a few standard listeners. These measurement calls are collected
by a runtime component and sent to a backend periodically.

The modules are:

**performance-tracking-core** 
* Annotations & configuration for instrumentation, packages `core.annotations`, `core.base`, 
`core.detours`, `core.mixins` and `core.wrappers`
* Measurement collection & sending to backend, `core` package

**performance-tracking-stubs**
* Stubs of and instrumented APIs

**performance-tracking-plugin**
* Android Gradle Transformation that instruments the application code using [ASM](http://asm.ow2.org/)

**performance-tracking**
* Fetches configuration from backend & starts collection of measurements
* Runtime API for applications to start and prolong measurements and metrics

## How to build it

```bash
$ git submodule init
$ git submodule update
$ ./gradlew performance-tracking:assemble performance-tracking-plugin:assemble \
    -PDEFAULT_CONFIG_URL_PREFIX="url to your configuration server" \
    -PDEFAULT_LOCATION_URL_PREFIX="url to your location server"
```

## How to use it

Currently we do not host a public APIs but you can fork it and use your own (dummy) apis 

## Contributing

See [Contribution guidelines](./CONTRIBUTING.md)
