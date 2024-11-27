package com.despegar;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class DownloadAlternativoClient {

    private String cookies;

    public boolean postWithCookiesAndSaveGif(Map<String, String> queryParams) {
        try {
            // Base URL
            String baseUrl = "https://notacarioca.rio.gov.br/contribuinte/notaprintimg.aspx";

            // Monta a URL completa com os parametros
            String fullUrl = buildUrlWithParams(baseUrl, queryParams);

            URL url = new URL(fullUrl);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);

            // HEADER
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            // COOKIES para nao dar acesso negado
            if (cookies != null) {
                connection.setRequestProperty("Cookie", cookies);
            }

            Map<String, List<String>> headerFields = connection.getHeaderFields();
            List<String> setCookieHeader = headerFields.get("Set-Cookie");
            if (setCookieHeader != null) {
                StringBuilder cookieBuilder = new StringBuilder();
                for (String cookie : setCookieHeader) {
                    if (cookieBuilder.length() > 0) {
                        cookieBuilder.append("; ");
                    }
                    cookieBuilder.append(cookie.split(";")[0]);
                }
                cookies = cookieBuilder.toString();
            }

            // SALVA ARQUIVO
            int statusCode = connection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                File directory = new File("arquivos");
                if (!directory.exists()) {
                    boolean created = directory.mkdir();
                    if (!created) {
                        throw new IOException("Erro.");
                    }
                }

                // Caminho para o arquivo GIF
                File gifFile = new File(directory, "nota_fiscal_alternativa.gif");


                try (InputStream is = connection.getInputStream();
                     FileOutputStream fos = new FileOutputStream(gifFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }

                System.out.println("Arquivo GIF salvo com sucesso em: " + gifFile.getAbsolutePath());


                File pdfFile = new File(directory, "nota_fiscal_alternativa.pdf");
                boolean conversionSuccess = convertGifToPdf(gifFile.getAbsolutePath(), pdfFile.getAbsolutePath());

                if (conversionSuccess) {
                    System.out.println("Arquivo PDF gerado com sucesso em: " + pdfFile.getAbsolutePath());
                    gifFile.deleteOnExit();
                    connection.disconnect();
                    return true;
                } else {
                    System.err.println("Falha ao converter GIF para PDF.");
                    connection.disconnect();
                    return false;
                }
            } else {
                InputStream errorStream = connection.getErrorStream();
                if (errorStream != null) {
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    System.err.println("Erro na requisição GET: HTTP " + statusCode);
                    System.err.println("Resposta do erro: " + errorResponse.toString());
                } else {
                    System.err.println("Erro na requisição GET: HTTP " + statusCode);
                }
                connection.disconnect();
                return false;
            }
        } catch (Exception e) {
            System.err.println("Erro ao realizar a requisição: " + e.getMessage());
            return false;
        }
    }

    private String buildUrlWithParams(String baseUrl, Map<String, String> queryParams) throws UnsupportedEncodingException {
        StringBuilder urlWithParams = new StringBuilder(baseUrl);
        urlWithParams.append("?");
        for (Map.Entry<String, String> param : queryParams.entrySet()) {
            if (urlWithParams.length() > baseUrl.length() + 1) {
                urlWithParams.append("&");
            }
            urlWithParams.append(param.getKey()).append("=").append(URLEncoder.encode(param.getValue(), "UTF-8"));
        }
        return urlWithParams.toString();
    }

    private boolean convertGifToPdf(String gifFilePath, String pdfFilePath) {
        try {
            File gifFile = new File(gifFilePath);
            if (!gifFile.exists()) {
                System.err.println("Arquivo GIF não encontrado: " + gifFilePath);
                return false;
            }

            PdfWriter writer = new PdfWriter(pdfFilePath);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            ImageData imageData = ImageDataFactory.create(gifFilePath);
            Image image = new Image(imageData);
            document.add(image);

            document.close();
            return true;
        } catch (Exception e) {
            System.err.println("Erro ao converter GIF para PDF: " + e.getMessage());
            return false;
        }
    }
}
