package com.despegar.utils;

import com.despegar.DownloadGinfesClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtils {

    public static String findCorrectHtmlFromUrl(URL url) {
        URL currentUrl = url;
        String htmlContent = "";

        try {
            //LOOPING PARA LIDAR COM OS MULTIPLOS POSSÍVEIS REDIRECTS
            while (true) {
                HttpURLConnection connection = (HttpURLConnection) currentUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setInstanceFollowRedirects(false);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int statusCode = connection.getResponseCode();

                if (statusCode == 302 || statusCode == 301) {
                    String redirectUrl = connection.getHeaderField("Location");
                    System.out.println("Redirecionando para: " + redirectUrl);
                    currentUrl = new URL(redirectUrl);


                } else if (statusCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = in.readLine()) != null) {
                        response.append(line).append("\n");
                    }
                    in.close();
                    connection.disconnect();

                    String locationPattern = "location\\s*=\\s*['\"](.*?)['\"]";
                    Pattern pattern = Pattern.compile(locationPattern);
                    Matcher matcher = pattern.matcher(response.toString());

                    //CAPTURAR REDIRECT JAVASCRIPT, RE-EXECUTAR COM NOVA URL
                    if (matcher.find()) {
                        String newLocation = matcher.group(1);
                        System.out.println("Novo URL encontrado no JavaScript: " + newLocation);
                        currentUrl = new URL(newLocation);

                    } else {
                        // Não encontrou redirecionamento, HTML FINAL

                        htmlContent = response.toString();
                        return htmlContent;
                    }
                } else {
                    connection.disconnect();
                    return "Erro na chamada, status code: " + statusCode;
                }
            }
        } catch (IOException e) {
            return "Erro na chamada: " + e.getMessage();
        }
    }


    public static String nfsExtractorFromHtml(String html) {
        int startIndex = html.indexOf("name=\"nfs\" value=\"");

        if (startIndex == -1) {
            return "Não foi possível encontrar o valor de 'nfs' no HTML.";
        }

        startIndex += "name=\"nfs\" value=\"".length();
        int endIndex = html.indexOf("\"", startIndex);

        if (endIndex == -1) {
            return "Não foi possível encontrar o valor de 'nfs' no HTML.";
        }

        return html.substring(startIndex, endIndex);
    }
}
