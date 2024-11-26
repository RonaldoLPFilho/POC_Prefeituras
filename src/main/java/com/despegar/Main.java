package com.despegar;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws MalformedURLException {

        Scanner scanner = new Scanner(System.in);

        System.out.println("----------------------- BEM-VINDO -----------------------");
        System.out.println();
        System.out.print("Insira o link: ");

        String link = scanner.nextLine();

        URL url = new URL(link);

        String teste = parseUrl(url);

        System.out.println(teste);

    }

    public static String parseUrl(URL url){
       DownloadDocumentService downloadDocumentService = new DownloadDocumentService();

        if (url.getHost().contains("ginfes") || url.getHost().contains("nfse.isssbc") )
            return downloadDocumentService.findCorrectHTML(url);

        if(url.getHost().contains("prefeitura.sp") || url.getHost().contains("rio.gov"))
            return "URL não é do ginfes";


        return "Link não identificado";
    }

}
