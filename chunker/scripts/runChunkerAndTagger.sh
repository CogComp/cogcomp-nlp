#!/bin/sh
java -Xmx512m -cp dist/*:lib/*:class/* edu.illinois.cs.cogcomp.lbj.chunk.ChunksAndPOSTags $1
