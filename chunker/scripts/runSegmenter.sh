#!/bin/sh
java -cp class/*:dist/*:lib/* edu.illinois.cs.cogcomp.lbjava.nlp.seg.SegmentTagPlain edu.illinois.cs.cogcomp.lbj.chunk.Chunker $1
