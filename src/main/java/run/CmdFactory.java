package run;

import config.Constant;
import entity.Subject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CmdFactory {

    /**
     * build execution command for compiling a subject
     *
     * @param subject : subject to be compiled
     * @return commands need to be executed
     */
    public static String[] createBuildSubjectCmd(Subject subject) {
        //log.info("----- Compile Subject " + subject.toString() + "------");
        return createD4JCmd(subject, "compile", Constant.COMPILE_TIMEOUT);
    }

    public static String[] createTestSubjectCmd(Subject subject, int timeout) {
        return createD4JCmd(subject, "test", timeout);
    }

    public static String[] createTestSingleTestCaseCmd(Subject subject, int timeout, String clazzAndMethod) {
        return createD4JCmd(subject, "test -t " + clazzAndMethod, timeout);
    }

    public static String[] createTestSingleTestCaseCmd(Subject subject, int timeout, String clazz, String method) {
        return createD4JCmd(subject, "test -t " + clazz + "::" + method, timeout);
    }

    public static String[] createTestSingleTestCaseCmd(Subject subject, String clazz, String method) {
        return createD4JCmd(subject, "test -t " + clazz + "::" + method, -1);
    }

    public static String[] createTestSingleTestCaseCmd(Subject subject, String failTest) {
        return createD4JCmd(subject, "test -t " + failTest, -1);
    }

    // TODO
    public static String[] createBTraceCmd(Subject subject, String Arg, int timeout) {
        StringBuilder stringBuilder = new StringBuilder();
/*        if(timeout > 0){
            stringBuilder.append(Constant.COMMAND_TIMEOUT).append(timeout).append(" ");
        }*/
        stringBuilder.append(Constant.COMMAND_D4J).append(Arg);
        log.info(stringBuilder.toString());
        return new String[]{"/bin/bash", "-c", stringBuilder.toString()};
    }

    public static String[] createTestSingleTestByJUnit(Subject subject, String Arg) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Constant.COMMAND_CD).append(subject.getHome()).append(" && ");
        if(Constant.TEST_TIMEOUT > 0){
            stringBuilder.append(Constant.COMMAND_TIMEOUT).append(Constant.TEST_TIMEOUT).append(" ");
        }
        //stringBuilder.append(Constant.COMMAND_TIMEOUT).append(Constant.TEST_TIMEOUT).append(" ");
        stringBuilder.append(Arg);
        log.info(stringBuilder.toString());
        return new String[]{"/bin/bash", "-c", stringBuilder.toString()};
    }

    /**
     * create d4j command based on the given argument {@code args}
     *
     * @param subject : subject to be focused
     * @param args    : command to be executed, e.g., "test", "compile", etc.
     * @return command need to be executed
     */
    private static String[] createD4JCmd(Subject subject, String args, int timeout) {
        //System.out.println(stringBuilder.toString() );
        StringBuilder stringBuilder = new StringBuilder();
        ///String stringBuilder = Constant.COMMAND_CD + subject.getHome() + " && " +
        stringBuilder.append(Constant.COMMAND_CD).append(subject.getHome()).append(" && ");
        if(timeout > 0){
            stringBuilder.append(Constant.COMMAND_TIMEOUT).append(timeout).append(" ");
        }
        stringBuilder.append( Constant.COMMAND_D4J).append(args);
        return new String[]{"/bin/bash", "-c", stringBuilder.toString()};

    }

    public static String[] createDiffCmd(String sourceFile, String targetFile, String patchFile) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Constant.COMMAND_DIFF).append(sourceFile).append(" ")
                .append(targetFile).append(">").append(patchFile);
        log.info(stringBuilder.toString());
        return new String[]{"/bin/bash", "-c", stringBuilder.toString()};
    }

    public static String[] createPatchCmd(String targetFile, String patchFile) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Constant.COMMAND_PATCH).append(targetFile).append(" ").append(patchFile);
        log.info(stringBuilder.toString());
        return new String[]{"/bin/bash", "-c", stringBuilder.toString()};
    }

    public static String[] createDownloadCmd(Subject subject) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(Constant.COMMAND_D4J)
                .append(" checkout -p ").append(subject.getName())
        .append(" -v ").append(subject.getId()).append("b")
        .append(" -w ").append(subject.getHome());
        log.info(stringBuilder.toString());
        return new String[]{"/bin/bash", "-c", stringBuilder.toString()};
    }
    public static String[] createDownloadFixedCmd(Subject subject) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(Constant.COMMAND_D4J)
                .append(" checkout -p ").append(subject.getName())
                .append(" -v ").append(subject.getId()).append("f")
                .append(" -w ").append(subject.getHome());
        log.info(stringBuilder.toString());
        return new String[]{"/bin/bash", "-c", stringBuilder.toString()};
    }
}
