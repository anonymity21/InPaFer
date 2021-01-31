import os

project="Math"
bugid="20"
btrace_home="/home/emily/WorkSpace/Tool/btrace"
traceDir="/home/emily/WorkSpace/Tool/btrace/traceRes/"
projectDir="/home/emily/WorkSpace/Data/Defects4J/projects/Math/"
w_buggy=projectDir+ project +"_" + bugid +"_buggy"
tracefile = traceDir + "math20_patch1"

jvmargs=" -a -Djvmargs=-javaagent:%s/btrace-agent.jar=noserver,debug=true,scriptOutputFile=%s,script=%s/AllLines.class" % (btrace_home, tracefile, btrace_home)
test = "org.apache.commons.math3.optimization.direct.CMAESOptimizerTest::testMath864"
cmd = 'defects4j test -n -t '+test+' -w '+w_buggy+jvmargs
os.system(cmd)
print(cmd)
