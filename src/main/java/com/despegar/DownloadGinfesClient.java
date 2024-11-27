package com.despegar;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class DownloadGinfesClient {

    public boolean postToGinfesAndSavePdf(String nfsValue) {
        try {
            URL url = new URL("https://visualizar.ginfes.com.br/report/exportacao");

            // AQUI TAMBVEM ACONTECE REDIRECT
            while (true) {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setInstanceFollowRedirects(false); // DESATIVA O REDIRECT AUTOMATICO
                connection.setDoOutput(true);

                //HEADERS QUE SAO PASSADOS NO POSTMAN
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
                connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

                // MONTAR O BODY DA REQUISICAO
                String body = "nfs=" + URLEncoder.encode(nfsValue, "UTF-8")
                        + "&nomeRelatorio=" + URLEncoder.encode("nfe_ginfes", "UTF-8")
                        + "&imprime=" + URLEncoder.encode("0", "UTF-8")
                        + "&tipo=" + URLEncoder.encode("pdf", "UTF-8")
                        + "&path=" + URLEncoder.encode("http://visualizar.ginfes.com.br/report/consultarNota", "UTF-8");


                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = body.getBytes("UTF-8");
                    os.write(input, 0, input.length);
                }

                int statusCode = connection.getResponseCode();

                // LIDAR COM REDIRECIONAMENTO
                if (statusCode == HttpURLConnection.HTTP_MOVED_TEMP || statusCode == HttpURLConnection.HTTP_MOVED_PERM) {
                    String redirectUrl = connection.getHeaderField("Location");
                    if (redirectUrl == null) {
                        System.err.println("Erro no redirecionamento GINFES client ");
                        return false;
                    }
                    System.out.println("Redirecionando para: " + redirectUrl);
                    url = new URL(redirectUrl); // Atualiza a URL para seguir o redirecionamento
                    connection.disconnect();
                    continue; // Faz a nova chamada com a URL redirecionada
                }

                if (statusCode == HttpURLConnection.HTTP_OK) {
                    // Pasta onde o PDF será salvo
                    File directory = new File("arquivos");
                    if (!directory.exists()) {
                        boolean created = directory.mkdir();
                        if (!created) {
                            System.err.println("Erro ao criar a pasta");
                            return false;
                        }
                    }

                    File pdfFile = new File(directory, "nota_fiscal.pdf");

                    // Salva o arquivo PDF
                    try (InputStream is = connection.getInputStream();
                         FileOutputStream fos = new FileOutputStream(pdfFile)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }

                    System.out.println("Arquivo PDF salvo com sucesso em: " + pdfFile.getAbsolutePath());
                    connection.disconnect();
                    return true;
                }
                    // ERROR AREA
                    InputStream errorStream = connection.getErrorStream();
                    if (errorStream != null) {
                        BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
                        StringBuilder errorResponse = new StringBuilder();
                        String line;
                        while ((line = errorReader.readLine()) != null) {
                            errorResponse.append(line);
                        }
                        System.err.println("Erro na requisição POST: HTTP " + statusCode);
                        System.err.println("Resposta do erro: " + errorResponse.toString());
                    } else {
                        System.err.println("Erro na requisição POST: HTTP " + statusCode);
                    }
                    connection.disconnect();
                    return false;
                }

        } catch (Exception e) {
            System.err.println("Erro ao realizar a requisição: " + e.getMessage());
            return false;
        }
    }
}