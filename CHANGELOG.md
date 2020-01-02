# Changelog
All notable changes to this project will be documented in this file.

This project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
The change log format is inspired by [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).


## 4.3.0 Release - 2020-01-02

Minor feature release.

### Features
- Enable classification of attributes with multiple parents.

### Improvements
- Warn when no stated relationships or axioms present.


## 4.2.0 Release - 2019-06-06

Minor changes after feedback from the Modelling Advisory Group on the International Alpha release.

### Improvements (made via SNOMED OWL Toolkit library)
- Use class axioms to make 'Concept model object attribute' and 'Concept model data attribute' a child of 'Concept model attribute' (CLASS-112).
  - Removed workaround to create these relationships during NNF calculation.
