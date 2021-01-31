package auxiliary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class Dumper {

    private static URLClassLoader classLoader = null;

    private static boolean removeNewLine = true;
    private final static long MAX_OUTPUT_FILE_SIZE = 1000;
    private final static int MAX_DEPTH = 3;
    private final static int ARRAY_MAX_LENGTH = 5;

    //need fix for mac
    private final static String LIB_FILE_PATH = "/Users/liangjingjing/WorkSpace/Project/PatchFilterBackUp/patchfilter/interactive-patch-filter/src/main/java/auxiliary/lib";
     // for server
   // private final static String LIB_FILE_PATH = "/home/emily/WorkSpace/Project/PatchFilterBackUp/patchfilter/interactive-patch-filter/src/main/java/auxiliary/lib";
    private static Dumper instance = new Dumper();

    protected static Dumper getInstance() {
        init();
        return instance;
    }

    class DumpContext {
        int maxDepth = 0;
        int maxArrayElements = 0;
        int callCount = 0;
        // HashMap<Object, Integer> visited = new HashMap<Object, Integer>();
        List visited = new ArrayList();
    }

    public static Object write(String OUT_FILE_NAME, String VarName, Object obj) {
        if (obj == null) {
            return obj;
        }
        File file = new File(OUT_FILE_NAME);
        //System.out.println(file.getName());
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                return obj;
            }
        }
        if ((file.length() >> 20) > MAX_OUTPUT_FILE_SIZE) {
            return obj;
        }

        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
        } catch (IOException e) {
            return obj;
        }

        try {
            bufferedWriter.write(VarName);
            bufferedWriter.write(dump(obj));
            bufferedWriter.write("\n");
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return obj;
    }
    public static int write(String OUT_FILE_NAME, String VarName, int obj) {

        File file = new File(OUT_FILE_NAME);
        //System.out.println(file.getName());
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                return obj;
            }
        }

        if ((file.length() >> 20) > MAX_OUTPUT_FILE_SIZE) {
            return obj;
        }

        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
        } catch (IOException e) {
            return obj;
        }

        try {
            bufferedWriter.write(VarName);
            bufferedWriter.write(dump(obj));
            bufferedWriter.write("\n");
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return obj;
    }

    public static boolean write(String OUT_FILE_NAME, String VarName, boolean obj) {

        File file = new File(OUT_FILE_NAME);
        //System.out.println(file.getName());
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                return obj;
            }
        }

        if ((file.length() >> 20) > MAX_OUTPUT_FILE_SIZE) {
            return obj;
        }

        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
        } catch (IOException e) {
            return obj;
        }

        try {
            bufferedWriter.write(VarName);
            bufferedWriter.write(dump(obj));
            bufferedWriter.write("\n");
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return obj;
    }
    public static float write(String OUT_FILE_NAME, String VarName, float obj) {

        File file = new File(OUT_FILE_NAME);
        //System.out.println(file.getName());
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                return obj;
            }
        }

        if ((file.length() >> 20) > MAX_OUTPUT_FILE_SIZE) {
            return obj;
        }

        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
        } catch (IOException e) {
            return obj;
        }

        try {
            bufferedWriter.write(VarName);
            bufferedWriter.write(dump(obj));
            bufferedWriter.write("\n");
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return obj;
    }
    public static double write(String OUT_FILE_NAME, String VarName, double obj) {

        File file = new File(OUT_FILE_NAME);
        //System.out.println(file.getName());
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                return obj;
            }
        }

        if ((file.length() >> 20) > MAX_OUTPUT_FILE_SIZE) {
            return obj;
        }

        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
        } catch (IOException e) {
            return obj;
        }

        try {
            bufferedWriter.write(VarName);
            bufferedWriter.write(dump(obj));
            bufferedWriter.write("\n");
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return obj;
    }
    public static long write(String OUT_FILE_NAME, String VarName, long obj) {

        File file = new File(OUT_FILE_NAME);
        //System.out.println(file.getName());
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                return obj;
            }
        }

        if ((file.length() >> 20) > MAX_OUTPUT_FILE_SIZE) {
            return obj;
        }

        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
        } catch (IOException e) {
            return obj;
        }

        try {
            bufferedWriter.write(VarName);
            bufferedWriter.write(dump(obj));
            bufferedWriter.write("\n");
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return obj;
    }
    public static byte write(String OUT_FILE_NAME, String VarName, byte obj) {

        File file = new File(OUT_FILE_NAME);
        //System.out.println(file.getName());
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                return obj;
            }
        }

        if ((file.length() >> 20) > MAX_OUTPUT_FILE_SIZE) {
            return obj;
        }

        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
        } catch (IOException e) {
            return obj;
        }

        try {
            bufferedWriter.write(VarName);
            bufferedWriter.write(dump(obj));
            bufferedWriter.write("\n");
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return obj;
    }
    public static char write(String OUT_FILE_NAME, String VarName, char obj) {

        File file = new File(OUT_FILE_NAME);
        //System.out.println(file.getName());
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                return obj;
            }
        }

        if ((file.length() >> 20) > MAX_OUTPUT_FILE_SIZE) {
            return obj;
        }

        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
        } catch (IOException e) {
            return obj;
        }

        try {
            bufferedWriter.write(VarName);
            bufferedWriter.write(dump(obj));
            bufferedWriter.write("\n");
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return obj;
    }
    public static boolean write(String OUT_FILE_NAME, String obj) {
        if (obj == null) {
            return false;
        }
        File file = new File(OUT_FILE_NAME);
        //System.out.println(file.getName());
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                return false;
            }
        }

        if ((file.length() >> 20) > MAX_OUTPUT_FILE_SIZE) {
            return false;
        }

        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
        } catch (IOException e) {
            return false;
        }

        try {
            bufferedWriter.write(obj);
            bufferedWriter.write("\n");
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    public static String dump(Object o) {
        return dump(o, MAX_DEPTH, ARRAY_MAX_LENGTH);
    }

    public static String dump(int o) {
        return dump(Integer.valueOf(o), MAX_DEPTH, ARRAY_MAX_LENGTH);
    }

    public static String dump(float o) {
        return dump(Float.valueOf(o), MAX_DEPTH, ARRAY_MAX_LENGTH);
    }

    public static String dump(double o) {
        return dump(Double.valueOf(o), MAX_DEPTH, ARRAY_MAX_LENGTH);
    }

    public static String dump(long o) {
        return dump(Long.valueOf(o), MAX_DEPTH, ARRAY_MAX_LENGTH);
    }

    public static String dump(byte o) {
        return dump(Byte.valueOf(o), MAX_DEPTH, ARRAY_MAX_LENGTH);
    }

    public static String dump(char o) {
        return dump(Character.valueOf(o), MAX_DEPTH, ARRAY_MAX_LENGTH);
    }

    public static String dump(boolean o) {
        return dump(Boolean.valueOf(o), MAX_DEPTH, ARRAY_MAX_LENGTH);
    }

    public static String dump(Object o, int maxDepth, int maxArrayElements) {
        DumpContext ctx = Dumper.getInstance().new DumpContext();
        ctx.maxDepth = maxDepth;
        ctx.maxArrayElements = maxArrayElements;

        Object object = dump(o, ctx);
        if(object == null){
            return "{null}";
        }
        String allFields = object.toString();
        // if(removeNewLine){
        // allFields = allFields.replace("\n", "");
        // }

        return allFields;
    }

    protected static Object dump(Object o, DumpContext ctx) {
        if (o == null) {
            if (ctx.callCount == 0) {
                return "{null}";
            } else {
                return null;
            }
        }

        ctx.callCount++;
        // StringBuffer tabs = new StringBuffer();
        // tabs.append("@"+ctx.callCount+"@");
        // StringBuffer buffer = new StringBuffer();
        Class oClass = o.getClass();

        Object returnObject = null;

        if (oClass.isArray()) {
            // buffer.append("[\n");
            int realCount = Array.getLength(o);
            int rowCount = ctx.maxArrayElements == 0 ? realCount : Math.min(ctx.maxArrayElements, Array.getLength(o));
            List elemets = new ArrayList();
            for (int i = 0; i < rowCount; i++) {
                try {
                    Object value = Array.get(o, i);
                    // buffer.append(dumpValue(value, ctx, tabs));
                    elemets.add(dumpValue(value, ctx));
                } catch (Exception e) {
                    // buffer.append(e.getMessage());
                }
                // buffer.append("\n");
            }
            // buffer.append("]");

            try {
                Class javaClass = Class.forName("net.sf.json.JSONArray", true, classLoader);
                Method method = javaClass.getMethod("fromObject", new Class[] { Object.class });
                method.setAccessible(true);
                Object tmpObject = method.invoke(null, new Object[]{ elemets });

                Class wrapperClass = Class.forName("net.sf.json.JSONObject", true, classLoader);
                returnObject = wrapperClass.newInstance();
                method = wrapperClass.getMethod("accumulate", new Class[] { String.class, Object.class });
                method.setAccessible(true);

                method.invoke(returnObject, new Object[]{"length", Integer.valueOf(realCount)});
                method.invoke(returnObject, new Object[]{"elements", tmpObject});

            } catch (Exception e) {
                e.printStackTrace();
            }

            // JSONArray jsonArray = JSONArray.fromObject(elemets);
            // returnObject = jsonArray;
        } else {
            // buffer.append("{\n");
            // JSONObject jsonObject = new JSONObject();
            Object jsonObject = null;
            Method method = null;
            try {
                Class javaClass = Class.forName("net.sf.json.JSONObject", true, classLoader);
                jsonObject = javaClass.newInstance();
                method = javaClass.getMethod("accumulate", new Class[] { String.class, Object.class });
                method.setAccessible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }

            while (oClass != null) {

                Field[] allFields = oClass.getDeclaredFields();

                for (int i = 0; i < allFields.length; i++) {
                    Field field = allFields[i];
                    int modifiers = field.getModifiers();
                    if (Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers)) {
                        continue;
                    }
                    // print variable name information
                    String fSimpleName = getSimpleNameWithoutArrayQualifier(field.getType());
                    String fName = field.getName();
                    // buffer.append("(" + fName + ":" + fSimpleName + ")");
                    // buffer.append("(" + fName + ")");
                    // buffer.append("(" + fSimpleName + ")");

                    field.setAccessible(true);
                    try {
                        Object value = field.get(o);
                        // jsonObject.accumulate(fName, dumpValue(value, ctx));
                        method.invoke(jsonObject, new Object[] { fName, dumpValue(value, ctx) });
                        // buffer.append(dumpValue(value, ctx, tabs));
                    } catch (Exception e) {
                    }
                    // buffer.append("\n");

                }
                oClass = oClass.getSuperclass();
            }
            // buffer.append("}");
            returnObject = jsonObject;
        }
        ctx.callCount--;
        // return buffer.toString();
        return returnObject;

    }

    protected static Object dumpValue(Object value, DumpContext ctx) {
        // String nullValue = tabs.toString() + "<null>";
        String nullValue = "<null>";
        if (value == null) {
            return null;
        }
        if (value.getClass().isPrimitive() || value.getClass() == java.lang.Short.class
                || value.getClass() == java.lang.Long.class || value.getClass() == java.lang.String.class
                || value.getClass() == java.lang.Integer.class || value.getClass() == java.lang.Float.class
                || value.getClass() == java.lang.Byte.class || value.getClass() == java.lang.Character.class
                || value.getClass() == java.lang.Double.class || value.getClass() == java.lang.Boolean.class
                || value.getClass() == java.util.Date.class || value.getClass().isEnum()) {

            // return tabs.toString() + value.toString();
            return value;

        } else {
            // Integer visitedIndex = ctx.visited.get(value);
            // if (visitedIndex == null) {
            if (!ctx.visited.contains(value)) {
                // ctx.visited.put(value, ctx.callCount);
                ctx.visited.add(value);
                if (ctx.maxDepth == 0 || ctx.callCount < ctx.maxDepth) {
                    return dump(value, ctx);
                } else {
                    // return "<Reached max recursion depth>";
                    return nullValue;
                }
            } else {
                // return "<Previously visited>";
                return nullValue;
            }
        }
    }

    private static String getSimpleNameWithoutArrayQualifier(Class clazz) {
        String simpleName = clazz.getSimpleName();
        int indexOfBracket = simpleName.indexOf('[');
        if (indexOfBracket != -1)
            return simpleName.substring(0, indexOfBracket);
        return simpleName;
    }

    private static void init() {
        String[] libs = { "commons-beanutils-1.8.0.jar", "commons-collections-3.2.1.jar", "commons-lang-2.5.jar",
                "commons-logging-1.1.1.jar", "ezmorph-1.0.6.jar", "json-lib-2.4-jdk15.jar" };

        List files = new ArrayList();
        for (int i = 0; i < libs.length; i++) {
            files.add(new File(LIB_FILE_PATH + "/" + libs[i]));
        }
        URL[] loadpath = new URL[files.size()];
        int i = 0;
        for (; i < files.size(); i++) {
            try {
                loadpath[i] = ((File) files.get(i)).toURI().toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        classLoader = new URLClassLoader(loadpath);
    }
}


