#!/bin/sh
destpath="./classes"
coursedir="/homes/raphael/courses/cs541/public"
cuppath="$coursedir/cup"
classpath="$destpath:$cuppath/java-cup-10k.jar"
jasminpath="$cuppath/cup.jar:$cuppath/jasmin-sable.jar"
vpath="$destpath"
jcflags="-Xlint:all,-auxiliaryclass,-rawtypes -deprecation -classpath $classpath -d $destpath"
jflags="-classpath $classpath"
testdir="../tests"

for test in $(ls $testdir/*.csx_go); do
    testnum="$(echo $test | cut -d "/" -f 3 | cut -d "-" -f 2 | cut -d "." -f 1 )"
	java $jflags P5 $test
    java -classpath $jasminpath jasmin.Main test.j 
	java -classpath .:./classes "p$testnum""csx"
done
