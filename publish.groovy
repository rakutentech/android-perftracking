#!/usr/bin/env groovy

@NonCPS
def config() {
  return [
      build: [
        tasks: ['preReleaseCheck', 'Plugin:assemble', 'Runtime:assemble'],
        artifacts: ['Plugin/build/libs/Plugin-*.jar', 'Runtime/build/outputs/aar/Runtime-*.jar']
      ],
      test: [
        tasks: ['Plugin:check', 'Runtime:check', 'Core:check'],
      ],
      deploy: [
        tasks: [
        'Plugin:publish',
        'Runtime:publish',
        'Runtime:uploadJavadoc'
        ],
      ]
    ]
}

return this