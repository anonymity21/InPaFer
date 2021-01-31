package util;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;

/**
 * Created by liangjingjing on 2019/3/14.
 */
public class LevelLogger {

    public static boolean logON = true;

    private static Logger logger_ = Logger.getLogger("SimilarFix");

    private static LevelLogger levelLogger_ = new LevelLogger();

    private LevelLogger() {
        File f = new File("res/conf/log4j.properties");
        if (f.exists()) {
            PropertyConfigurator.configure("res/conf/log4j.properties");
        } else {
            BasicConfigurator.configure();
        }
    }
}
