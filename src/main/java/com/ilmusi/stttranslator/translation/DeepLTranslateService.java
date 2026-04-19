package com.ilmusi.stttranslator.translation;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ilmusi.stttranslator.STTTranslator;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeepLTranslateService {
   private static final String API_URL = "https://api-free.deepl.com/v2/translate";
   private static final HttpClient HTTP_CLIENT;
   private static final Gson GSON;
   private static final ExecutorService EXECUTOR;
   private static final Map<String, String> TRANSLATION_CACHE;
   private static final int MAX_CACHE_SIZE = 1000;
   private static final Map<String, String> DEEPL_LANG_MAP;

   public static CompletableFuture<String> translateAsync(String text, String fromLang, String toLang, String apiKey) {
      if (text != null && !text.isBlank()) {
         if (fromLang.equals(toLang)) {
            return CompletableFuture.completedFuture(text);
         } else {
            String cacheKey = fromLang + ">" + toLang + ":" + text.toLowerCase().trim();
            String cached = (String)TRANSLATION_CACHE.get(cacheKey);
            return cached != null ? CompletableFuture.completedFuture(cached) : CompletableFuture.supplyAsync(() -> {
               try {
                  return doTranslate(text, fromLang, toLang, apiKey, cacheKey);
               } catch (Exception var6) {
                  STTTranslator.LOGGER.error("DeepL failed: {}", var6.getMessage());
                  return "[!] " + text;
               }
            }, EXECUTOR);
         }
      } else {
         return CompletableFuture.completedFuture("");
      }
   }

   private static String doTranslate(String text, String fromLang, String toLang, String apiKey, String cacheKey) throws Exception {
      String sourceLang = (String)DEEPL_LANG_MAP.getOrDefault(fromLang, fromLang.toUpperCase());
      String targetLang = (String)DEEPL_LANG_MAP.getOrDefault(toLang, toLang.toUpperCase());
      String body = "text=" + URLEncoder.encode(text, StandardCharsets.UTF_8) + "&source_lang=" + sourceLang + "&target_lang=" + targetLang;
      HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api-free.deepl.com/v2/translate")).header("Authorization", "DeepL-Auth-Key " + apiKey).header("Content-Type", "application/x-www-form-urlencoded").header("Connection", "keep-alive").POST(BodyPublishers.ofString(body)).timeout(Duration.ofSeconds(6L)).build();
      HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
      if (response.statusCode() == 403) {
         return "[DeepL Key Invalid] " + text;
      } else if (response.statusCode() == 456) {
         return "[DeepL Quota Full] " + text;
      } else if (response.statusCode() != 200) {
         int var10000 = response.statusCode();
         return "[Error " + var10000 + "] " + text;
      } else {
         JsonObject json = (JsonObject)GSON.fromJson((String)response.body(), JsonObject.class);
         JsonArray translations = json.getAsJsonArray("translations");
         if (translations != null && !translations.isEmpty()) {
            String translated = translations.get(0).getAsJsonObject().get("text").getAsString().trim();
            if (TRANSLATION_CACHE.size() >= 1000) {
               Iterator<String> keys = TRANSLATION_CACHE.keySet().iterator();

               for(int i = 0; i < 250 && keys.hasNext(); ++i) {
                  keys.next();
                  keys.remove();
               }
            }

            TRANSLATION_CACHE.put(cacheKey, translated);
            return translated;
         } else {
            return text;
         }
      }
   }

   public static boolean isLanguageSupported(String langCode) {
      return DEEPL_LANG_MAP.containsKey(langCode);
   }

   public static void clearCache() {
      TRANSLATION_CACHE.clear();
   }

   static {
      HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5L)).version(Version.HTTP_2).build();
      GSON = new Gson();
      EXECUTOR = Executors.newFixedThreadPool(6);
      TRANSLATION_CACHE = new ConcurrentHashMap();
      DEEPL_LANG_MAP = Map.ofEntries(Map.entry("en", "EN"), Map.entry("es", "ES"), Map.entry("pt", "PT-BR"), Map.entry("fr", "FR"), Map.entry("de", "DE"), Map.entry("it", "IT"), Map.entry("ru", "RU"), Map.entry("zh", "ZH"), Map.entry("ja", "JA"), Map.entry("ko", "KO"), Map.entry("pl", "PL"), Map.entry("nl", "NL"), Map.entry("tr", "TR"));
   }
}


