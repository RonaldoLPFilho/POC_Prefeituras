package com.despegar;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws MalformedURLException {

        DownloadDocumentService service = new DownloadDocumentService();
        Scanner scanner = new Scanner(System.in);

        System.out.println("----------------------- BEM-VINDO -----------------------");
        System.out.println();
        System.out.print("Insira o link: ");

        String link = scanner.nextLine();

        URL url = new URL(link);

        String teste = service.proccessAndDownloadDocumentByUrl(url);

        System.out.println(teste);

    }



}
