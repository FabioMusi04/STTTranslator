package com.ilmusi.stttranslator.translation;

import com.ilmusi.stttranslator.STTTranslator;
import com.ilmusi.stttranslator.config.ModConfig;
import java.util.concurrent.CompletableFuture;

public class TranslationManager {
   public static CompletableFuture<String> translate(String text, String fromLang, String toLang) {
      ModConfig config = STTTranslator.getInstance().getConfig();
      TranslationManager.Provider provider = config.getTranslationProvider();
      String apiKey;
      if (provider == TranslationManager.Provider.CLAUDE) {
         apiKey = config.getClaudeApiKey();
         if (apiKey != null && !apiKey.isBlank() && !apiKey.equals("PUT-YOUR-API-KEY-HERE")) {
            return ClaudeTranslationService.translateAsync(text, fromLang, toLang, apiKey).thenCompose((r) -> {
               return r != null && !r.isEmpty() ? CompletableFuture.completedFuture(r) : fallbackFree(text, fromLang, toLang);
            });
         }
      }

      if (provider == TranslationManager.Provider.DEEPL) {
         apiKey = config.getDeeplApiKey();
         if (apiKey != null && !apiKey.isBlank()) {
            return DeepLTranslateService.translateAsync(text, fromLang, toLang, apiKey).thenCompose((r) -> {
               return r != null && !r.isEmpty() ? CompletableFuture.completedFuture(r) : fallbackFree(text, fromLang, toLang);
            });
         }
      }

      return provider == TranslationManager.Provider.LIBRE ? LibreTranslateService.translateAsync(text, fromLang, toLang).thenCompose((r) -> {
         return r != null && !r.isEmpty() ? CompletableFuture.completedFuture(r) : GoogleTranslateService.translateAsync(text, fromLang, toLang);
      }) : GoogleTranslateService.translateAsync(text, fromLang, toLang).thenCompose((r) -> {
         return r != null && !r.isEmpty() ? CompletableFuture.completedFuture(r) : LibreTranslateService.translateAsync(text, fromLang, toLang);
      });
   }

   private static CompletableFuture<String> fallbackFree(String text, String fromLang, String toLang) {
      return LibreTranslateService.translateAsync(text, fromLang, toLang).thenCompose((r) -> {
         return r != null && !r.isEmpty() ? CompletableFuture.completedFuture(r) : GoogleTranslateService.translateAsync(text, fromLang, toLang);
      });
   }

   public static void clearAllCaches() {
      GoogleTranslateService.clearCache();
      LibreTranslateService.clearCache();
      ClaudeTranslationService.clearCache();
      DeepLTranslateService.clearCache();
   }

   public static enum Provider {
      GOOGLE("Google Translate (Gratis)"),
      LIBRE("LibreTranslate IA (Gratis, mejor)"),
      DEEPL("DeepL (Gratis, API Key)"),
      CLAUDE("Claude AI (Premium)");

      public final String displayName;

      private Provider(String displayName) {
         this.displayName = displayName;
      }
   }
}


