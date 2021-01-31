package patchfilter.model.run;


import lombok.extern.slf4j.Slf4j;
import patchfilter.model.config.Constant;
import patchfilter.model.entity.Subject;

import java.util.List;

@Slf4j
public class Runner {

    private final static String __name__ = "@Runner ";
    private final static String SUCCESSTEST = "Failing tests: 0";


    public static boolean testSingleTest(Subject subject, String clazzAndMethod) {
        List<String> message = null;
        try {
            System.out.println("TESTING : " + clazzAndMethod);
            message = Executor.execute(CmdFactory.createTestSingleTestCaseCmd(subject, 30, clazzAndMethod));
        } catch (Exception e) {
            // LevelLogger.fatal(__name__ + "#buildSubject run test single test case failed !", e);
        }

        boolean success = false;
        for (int i = message.size() - 1; i >= 0; i--) {
//			System.out.println(message.get(i));
            if (message.get(i).contains(SUCCESSTEST)) {
                success = true;
                break;
            }
        }

        return success;
    }

    public static boolean testSingleTest(Subject subject, String clazz, String method) {
        return testSingleTest(subject, clazz + "::" + method);
    }


    public static boolean runFailTest(Subject subject, String failTest) {
        List<String> message = null;
        try {
            System.out.println("TESTING : " + subject.getName() + "_" + subject.getId());
            message = Executor.execute(CmdFactory.createTestSingleTestCaseCmd(subject, failTest));
            //System.out.println(message);
        } catch (Exception e) {
            // LevelLogger.fatal(__name__ + "#buildSubject run test single test case failed !", e);
        }

        boolean success = false;
        for (int i = message.size() - 1; i >= 0; i--) {
//			System.out.println(message.get(i));
            if (message.get(i).contains(SUCCESSTEST)) {
                success = true;
                break;
            }
        }

        return success;
    }

    public static boolean runTestSuite(Subject subject) {
        List<String> message = null;
        try {
            System.out.println("TESTING : " + subject.getName() + "_" + subject.getId());
            message = Executor.execute(CmdFactory.createTestSubjectCmd(subject, 10 * 60));
        } catch (Exception e) {
            // LevelLogger.fatal(__name__ + "#buildSubject run test single test case failed !", e);
        }

        boolean success = false;
        for (int i = message.size() - 1; i >= 0; i--) {
//			System.out.println(message.get(i));
            if (message.get(i).contains(SUCCESSTEST)) {
                success = true;
                break;
            }
        }

        return success;
    }

    public static boolean compileSubject(Subject subject) {

        //System.out.println("----- Compile " + subject.toString() + " -----");
        log.info("Compile " + subject.toString());
        List<String> message = null;
        try {
            message = Executor.execute(CmdFactory.createBuildSubjectCmd(subject));
        } catch (Exception e) {
            // LevelLogger.fatal(__name__ + "#buildSubject run build subject failed !", e);
        }

        boolean success = true;
        for (int i = 0; i < message.size(); i++) {
            if (message.get(i).contains(Constant.ANT_BUILD_FAILED)) {
                success = false;
            }
        }
        //log.info(message.toString());

        return success;
    }

    public static boolean traceSubject(Subject subject, String arg) {
        List<String> message = null;
        try {
            message = Executor.execute(CmdFactory.createBTraceCmd(subject, arg, 90));
        } catch (Exception e) {
            // LevelLogger.fatal(__name__ + "#buildSubject run build subject failed !", e);
            System.out.println(e);
        }
        return true;
    }

    public static String JUnitTestSubject(Subject subject, String failingTest) {
        log.info("----- Begin Run Failing Test And Get Trace ----- " + failingTest);
        List<String> message = null;
        try {
            String junitArg = Constant.COMMAND_JAVA_HOME + "/bin/java -Xms4g -Xmx8g -cp \""
                    + subject.getDependency() + "\" " + Constant.JUNIT_RUN_MAIN + " " + failingTest;
            message = Executor.execute(CmdFactory.createTestSingleTestByJUnit(subject, junitArg));
        } catch (Exception e) {
            // LevelLogger.fatal(__name__ + "#buildSubject run build subject failed !", e);
            System.out.println(e);
        }
        String result = "";
        for (String s : message) {
            result = result + s + " ";
        }

        return result;
    }


    /*
     * targetFile: fixedFile
     * patchFile: diffFile
     */
    public static boolean patchFile(String targetFile, String patchFile) {
        List<String> message = null;
        boolean success = true;
        try {
            message = Executor.execute(CmdFactory.createPatchCmd(targetFile, patchFile));
        } catch (Exception e) {
            // LevelLogger.fatal(__name__ + "#buildSubject run build subject failed !", e);
            System.out.println(e);
        }
        for (int i = message.size() - 1; i >= 0; i--) {
            if (message.get(i).contains(Constant.PATCH_FAILED)) {
                success = false;
                break;
            }
        }
        return success;
    }

    public static void diff2File(String sourceFile, String targetFile, String patchFile) {
        List<String> message = null;
        try {
            message = Executor.execute(CmdFactory.createDiffCmd(sourceFile, targetFile, patchFile));
        } catch (Exception e) {
            // LevelLogger.fatal(__name__ + "#buildSubject run build subject failed !", e);
            System.out.println(e);
        }
        //return  message.get(0);
/*        for(int i = message.size() - 1; i >= 0; i--){
            if (message.get(i).contains(Constant.ANT_BUILD_FAILED)) {
                success = false;
                break;
            }
        }*/
    }
}
