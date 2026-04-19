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
               return isUsableTranslation(text, r) ? CompletableFuture.completedFuture(r) : fallbackFree(text, fromLang, toLang);
            });
         }
      }

      if (provider == TranslationManager.Provider.DEEPL) {
         apiKey = config.getDeeplApiKey();
         if (apiKey != null && !apiKey.isBlank()) {
            return DeepLTranslateService.translateAsync(text, fromLang, toLang, apiKey).thenCompose((r) -> {
               return isUsableTranslation(text, r) ? CompletableFuture.completedFuture(r) : fallbackFree(text, fromLang, toLang);
            });
         }
      }

      return provider == TranslationManager.Provider.LIBRE ? LibreTranslateService.translateAsync(text, fromLang, toLang).thenCompose((r) -> {
         return isUsableTranslation(text, r) ? CompletableFuture.completedFuture(r) : GoogleTranslateService.translateAsync(text, fromLang, toLang);
      }) : GoogleTranslateService.translateAsync(text, fromLang, toLang).thenCompose((r) -> {
         return isUsableTranslation(text, r) ? CompletableFuture.completedFuture(r) : LibreTranslateService.translateAsync(text, fromLang, toLang);
      });
   }

   private static CompletableFuture<String> fallbackFree(String text, String fromLang, String toLang) {
      return LibreTranslateService.translateAsync(text, fromLang, toLang).thenCompose((r) -> {
         return isUsableTranslation(text, r) ? CompletableFuture.completedFuture(r) : GoogleTranslateService.translateAsync(text, fromLang, toLang);
      });
   }

   private static boolean isUsableTranslation(String original, String translated) {
      if (translated == null || translated.isBlank()) {
         return false;
      }

      String src = normalizeText(original);
      String out = normalizeText(translated);
      if (out.isEmpty() || out.equals(src)) {
         return false;
      }

      if (src.length() >= 8 && out.contains(src)) {
         return false;
      }

      int overlap = longestCommonSubstring(src, out);
      int minLen = Math.min(src.length(), out.length());
      return minLen <= 0 || (double)overlap / (double)minLen <= 0.8D;
   }

   private static String normalizeText(String text) {
      return text == null ? "" : text.toLowerCase().replaceAll("\\s+", " ").trim();
   }

   private static int longestCommonSubstring(String a, String b) {
      if (a.isEmpty() || b.isEmpty()) {
         return 0;
      }

      int[] prev = new int[b.length() + 1];
      int[] curr = new int[b.length() + 1];
      int best = 0;

      for(int i = 1; i <= a.length(); ++i) {
         for(int j = 1; j <= b.length(); ++j) {
            if (a.charAt(i - 1) == b.charAt(j - 1)) {
               curr[j] = prev[j - 1] + 1;
               if (curr[j] > best) {
                  best = curr[j];
               }
            } else {
               curr[j] = 0;
            }
         }

         int[] swap = prev;
         prev = curr;
         curr = swap;
      }

      return best;
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


