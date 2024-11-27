package com.despegar;


import java.net.URL;
import java.util.Map;
import static com.despegar.utils.HtmlUtils.findCorrectHtmlFromUrl;
import static com.despegar.utils.HtmlUtils.nfsExtractorFromHtml;
import static com.despegar.utils.UrlUtils.extractQueryParamsToMap;

public class DownloadDocumentService {

    public String proccessAndDownloadDocumentByUrl(URL url){
        DownloadDocumentService downloadDocumentService = new DownloadDocumentService();

        if (url.getHost().contains("ginfes") || url.getHost().contains("nfse.isssbc") )
            return downloadDocumentService.downloadDocumentOnGinfes(url);

        if(url.getHost().contains("prefeitura.sp") || url.getHost().contains("rio.gov"))
            return downloadDocumentService.downloadDocumentoAlternativo(url);

        return "Link não identificado";
    }

    public String downloadDocumentOnGinfes(URL url) {
        String html = findCorrectHtmlFromUrl(url);
        String nfs = nfsExtractorFromHtml(html);
        DownloadGinfesClient client = new DownloadGinfesClient();
        try{
            if(client.postToGinfesAndSavePdf(nfs))
                return "Arquivo baixado com sucesso! (Ginfes)";

            return "Falha ao baixar o arquivo (GINFES)";
        }catch (Exception e) {
            return ("(GINFES) Alguma excessão foi lancada, DEBUGGUE: " + e.getMessage());
        }
    }

    public String downloadDocumentoAlternativo(URL url) {
        DownloadAlternativoClient client = new DownloadAlternativoClient();

        Map<String, String> queryParamsByOrinalUrl = extractQueryParamsToMap(url);

        try{
            if(client.postWithCookiesAndSaveGif(queryParamsByOrinalUrl))

                return "Arquivo baixado com sucesso! (Alternativo)";

            return "Falha ao baixar o arquivo (Alternativo)";
        }catch (Exception e) {
            return ("(Alternativo) Alguma excessão foi lancada, DEBUGGUE: " + e.getMessage());
        }

    }




}