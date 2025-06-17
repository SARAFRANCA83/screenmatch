package br.com.sfranca.screenmatch.service;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.TranslateTextRequest;
import software.amazon.awssdk.services.translate.model.TranslateTextResponse;

public class AwsTradutor {

    public static String traduzir(String textoIngles) {
        Region region = Region.US_EAST_1;

        try (TranslateClient translateClient = TranslateClient.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()) {

            TranslateTextRequest request = TranslateTextRequest.builder()
                    .sourceLanguageCode("en")
                    .targetLanguageCode("pt")
                    .text(textoIngles)
                    .build();

            TranslateTextResponse response = translateClient.translateText(request);
            return response.translatedText();
        }
    }
}


