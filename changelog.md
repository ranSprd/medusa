# Medusa changelog

All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

The main types of changes used in this file are:

- **Added** For new features.
- **Changes** There are 2 types of changes
    - **Breaking Changes** These changes break compatibility in some way with previous major versions. 
    - **Additional Changes** These changes kind of changes are incremental over previous versions. 
                             They affect the API, runtime, or persisted data but in a compatible way.
- **Deprecated** Is used for soon-to-be removed features.
- **Removed** For now removed features.
- **Fixed** For any bug fixes.
- **Security** Implemented code in case of vulnerabilities.

## [Unreleased]

### Added

- health check
- self service metrics 

### Breaking changes

- set/overwrite label fields based on a value (mappings)
- ignore boolean fields

### Fixed

- automatic generated labels only for same object 
- fix: validate prometheus metric/label names

## 0.1.1 SNAPSHOT

### Added

- payload can contain arrays, each entry is converted into a metric
- add support for counter metric type

### Breaking changes

- combine topic and topic pattern in configuration 

## 0.1.0 SNAPSHOT 
2023-12-10 (Date of Last Commit)

### Added

- Initial *proof of concept* version. It consists of a small web-api which delivers collected
  metric data, a mqtt listener and some logic code for transform incoming mqtt messages to
  metrics
- support of prometheus gauge type 
- statistics about unprocessed incoming messages

