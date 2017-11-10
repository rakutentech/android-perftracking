#!/usr/bin/env groovy

@NonCPS
def config() {
  return [
      pullrequest: [
        tasks: [
          'Plugin:assemble', 
          'Runtime:assemble', 
          'Plugin:check', 
          'Runtime:check', 
          'Core:check',
        ],
      ],
      deploy: [
        tasks: [
        'ensurePreRelease',
        'Plugin:publish',
        'Runtime:publish',
        ],
      ],
      reports: [
        jacoco: [enabled: false],
        cobertura: [enabled: false],
        junit: [enabled: false],
        lint: [enabled: false],
      ],
    ]
}

return this