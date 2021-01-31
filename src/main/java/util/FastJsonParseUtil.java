package util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Map.Entry;
import java.util.Objects;


public class FastJsonParseUtil {
    /**
     * 把拍平后的json进行格式化处理，输出标准的json格式
     *
     * @param uglyJSONString
     * @return
     */
    public static JSONObject jsonFormatter(String uglyJSONString, String rootKey) {

        JSONObject result = new JSONObject();
        parseJson2Map(result, JSON.parseObject(uglyJSONString), rootKey);
        //System.out.println(uglyJSONString);
        // return result.toString();
        return result;
    }

    public static void parseJson2Map(JSONObject resultObject, JSONObject jsonObject, String parentKey) {
        for (Entry<String, Object> object : jsonObject.entrySet()) {
            String key = object.getKey();
            Object value = object.getValue();
            String fullkey = (null == parentKey || parentKey.trim().equals("")) ? key : parentKey.trim() + "::" + key;
            if (Objects.isNull(value)) {
                resultObject.put(fullkey, null);
            } else if (value instanceof JSONObject) {
                parseJson2Map(resultObject, (JSONObject) value, fullkey);
            } else if (value instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) value;
                int i = 0;
                for (Object o : jsonArray) {
                    if (o instanceof JSONObject) {
                        JSONObject jsonObject1 = (JSONObject) o;
                        parseJson2Map(resultObject, jsonObject1, fullkey + "::" + i);
                    } else {
                        resultObject.put(fullkey + "::" + i, o);
                    }
                    ++i;
                }
            } else {
                resultObject.put(fullkey, value);
            }
        }
    }

    public static boolean isNested(Object jsonObj) {

        return jsonObj.toString().contains("{");
    }

    public static void main(String[] args) {
        String json = "{\"code\":200, \"message\":\"ok\", \"data\":\"{\\\"id\\\":131,\\\"appId\\\":6,\\\"versionCode\\\":6014000}\"}";
        String test = "{\"domain\":\"Time\",\"range\":\"Value\",\"timePeriodClass\":{\"cachedConstructor\":null,\"newInstanceCallerCache\":null,\"name\":null,\"allPermDomain\":null,\"smHelper\":null,\"useCaches\":true,\"reflectionData\":{\"clock\":1570435336585,\"timestamp\":1570435336585,\"referent\":\"<null>\",\"queue\":\"<null>\",\"next\":null,\"discovered\":null,\"lock\":\"<null>\",\"pending\":null},\"classRedefinedCount\":0,\"genericInfo\":null,\"reflectionFactory\":{\"initted\":true,\"reflectionFactoryAccessPerm\":\"<null>\",\"soleInstance\":\"<null>\",\"langReflectAccess\":\"<null>\",\"noInflation\":false,\"inflationThreshold\":15},\"initted\":true,\"enumConstants\":null,\"enumConstantDirectory\":null,\"annotations\":null,\"declaredAnnotations\":null,\"lastAnnotationsRedefinedCount\":0,\"annotationType\":null,\"classValueMap\":null},\"data\":{\"elementData\":{\"length\":10,\"elements\":[\"<null>\",null,null,null,null]},\"size\":1,\"modCount\":1},\"maximumItemCount\":2147483647,\"maximumItemAge\":9223372036854775807,\"minY\":102000,\"maxY\":102000,\"class$java$lang$Class\":null,\"class$java$util$Date\":null,\"class$java$util$TimeZone\":null,\"class$org$jfree$data$time$RegularTimePeriod\":null,\"key\":\"Series A\",\"description\":null,\"listeners\":{\"listenerList\":{\"length\":0,\"elements\":[]}},\"propertyChangeSupport\":{\"map\":{\"map\":null},\"source\":{\"domain\":\"Time\",\"range\":\"Value\",\"timePeriodClass\":\"<null>\",\"data\":\"<null>\",\"maximumItemCount\":2147483647,\"maximumItemAge\":9223372036854775807,\"minY\":102000,\"maxY\":102000,\"class$java$lang$Class\":null,\"class$java$util$Date\":null,\"class$java$util$TimeZone\":null,\"class$org$jfree$data$time$RegularTimePeriod\":null,\"key\":\"Series A\",\"description\":null,\"listeners\":\"<null>\",\"propertyChangeSupport\":\"<null>\",\"notify\":true,\"class$org$jfree$data$event$SeriesChangeListener\":null}},\"notify\":true,\"class$org$jfree$data$event$SeriesChangeListener\":null}";
        String array = "{'name':'111','child':[{'child':[{'name':'333'}]},{'name':'2221'},{'name':'22233'}]}";
        String test1 = "{\"code\":200,\"message\":\"ok\",\"data\":null}";
        System.out.println(jsonFormatter(test.replaceAll("\\\"<null>\\\"", "null"), null));
    }

}