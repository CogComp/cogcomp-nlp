# README For FEX Functionality

## Introduction

In addition to the programmatic generation using feature extractors, we allow specification of
feature extractors through a special file that defines feature extractors. This file is the .fex file.

## What happens
FeatureManifest loads the file and sends it to ManifestParser. Contains logic for converting
    definitions, variables, names into FeatureExtractors.
ManifestParser actually parses the file, and stores definitions, variables, names, etc.


## Structure of the .fex file

The file has a lisp-like syntax. As in lisp, a semicolon (;) is the comment symbol. This may come anywhere
in the line.

file := (define name definition* featureslist)
definition := NULL | (define name body) | (defvar name value)
body := something parsable by FeatureManifest.createFex
featureslist := leaf | (list leaf*) | (conjoin leaf leaf)
keywordbody := (conjoin ??) | (list leaf*) | (conjoin-and-include) | (bigram) | (trigram) | (transform-input) | (if)
name := <string>
leaf := must be in KnownFexes.fexes

