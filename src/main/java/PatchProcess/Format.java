package PatchProcess;

import entity.Subject;
import lombok.extern.slf4j.Slf4j;
import spoon.Launcher;
import spoon.SpoonException;
import spoon.compiler.Environment;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.FactoryImpl;
import spoon.support.DefaultCoreFactory;
import spoon.support.StandardEnvironment;
import spoon.support.compiler.VirtualFile;
import util.FileIO;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by liangjingjing on 2019/3/12.
 * <p>
 * This file is formating java file to a standard format
 */

@Slf4j
public class Format {
    private Subject subject;

    public Format(Subject subject) {
        this.subject = subject;
    }

    private Factory createFactory() {
        Environment env = getEnvironment();

        return new FactoryImpl(new DefaultCoreFactory(), env);
    }

    public Environment getEnvironment() {
        String targetPath = subject.getHome() + subject.getSbin();
        StandardEnvironment env = new StandardEnvironment();
        env.setComplianceLevel(5);
        env.setVerbose(false);
        env.setDebug(true);
        env.setTabulationSize(5);
        env.useTabulations(true);
        env.setLevel("OFF");
        env.setCommentEnabled(false);
        env.setSourceClasspath(new String[]{targetPath});
        return env;
    }

    public void formatSingleFile(String file) {
        FileIO.deleteComments(file);
        Launcher launcher = new Launcher(createFactory());
        launcher.addInputResource(new VirtualFile(FileIO.readFileToString(file)));
        Collection<CtType<?>> allTypes = launcher.buildModel().getAllTypes();
/*        if (allTypes.size() != 1) {
            throw new SpoonException("parseClass only considers one class. Please consider using a Launcher object for more advanced usage.");
        }
        try {
            CtClass l = (CtClass<?>) allTypes.stream().findFirst().get();
            StringBuilder content = new StringBuilder(FileIO.readFileToString(file).split("\n")[0]);
            FileIO.writeStringToFile(file, content.append("\n").append(l.toString()).toString());
            FileIO.deleteComments(file);
        } catch (ClassCastException e) {
            throw new SpoonException("parseClass only considers classes (and not interfaces and enums). Please consider using a Launcher object for more advanced usage.");
        }*/

        StringBuilder result = new StringBuilder();
        StringBuilder content = new StringBuilder(FileIO.readFileToString(file).split("\n")[0]);
        for (CtType<?> ct : allTypes) {
            try {
                //CtClass l = (CtClass<?>) allTypes.stream().findFirst().get();
                CtClass l = (CtClass<?>) ct;
                result.append(l.toString());
            } catch (ClassCastException e) {
                throw new SpoonException("parseClass only considers classes (and not interfaces and enums). Please consider using a Launcher object for more advanced usage.");
            }
        }
        FileIO.writeStringToFile(file, content.append("\n").append(result.toString()).toString());
        FileIO.deleteComments(file);

    }

    public void formatAllFile(Set<String> fileList) {
        for (String file : fileList) {
            formatSingleFile(file);
        }
       // log.info("Format all FixedFile ");
    }
}
