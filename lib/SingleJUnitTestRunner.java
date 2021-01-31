
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class SingleJUnitTestRunner {
    public static void main(String... args) throws ClassNotFoundException {
        String[] classAndMethod = args[0].split("::");
       // String project = args[1];
        //String bugid = args[2];

        Request request = Request.method(Class.forName(classAndMethod[0]), classAndMethod[1]);
        //String writeFilePath = "/home/emily/WorkSpace/Data/testResult/" + project + "/" + bugid + ".txt";

        Result result = new JUnitCore().run(request);

        if (result.wasSuccessful()) {
            System.out.println("Success.\n");

        }
        else {
            Failure failure = result.getFailures().get(0);

            System.out.println("\nTrace:\n"+failure.getTrace());
            System.out.println("\nMessage:\n"+failure.getMessage());
            System.out.println("\nTestHeader:\n"+failure.getTestHeader());
            System.out.println("\nRunTime: \n" + String.valueOf(result.getRunTime()) + " ms");
            System.out.println("\nFailure.\n");
        }

        System.exit(0);
    }

}