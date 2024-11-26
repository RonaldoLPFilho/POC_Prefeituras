package com.despegar.utils;


import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class UrlUtils {

    public static Map<String, String> extractQueryParams(URL url) {
        Map<String, String> queryParams = new HashMap<>();
        String query = url.getQuery();

        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    queryParams.put(keyValue[0], keyValue[1]);
                }
            }
        }

        return queryParams;
    }

}
