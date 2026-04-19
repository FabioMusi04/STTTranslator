package com.ilmusi.stttranslator.translation;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Version;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GoogleTranslateService {
   private static final String BASE_URL = "https://translate.googleapis.com/translate_a/single";
   private static final HttpClient HTTP_CLIENT;
   private static final Gson GSON;
   private static final ExecutorService EXECUTOR;
   private static final Map<String, String> TRANSLATION_CACHE;
   private static final int MAX_CACHE_SIZE = 2000;
   private static boolean warmedUp;

   public static void warmUp() {
      if (!warmedUp) {
         EXECUTOR.submit(() -> {
            try {
               HttpRequest req = HttpRequest.newBuilder().uri(URI.create("https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=es&dt=t&ie=UTF-8&oe=UTF-8&q=hello")).header("User-Agent", "Mozilla/5.0").GET().timeout(Duration.ofSeconds(4L)).build();
               HTTP_CLIENT.send(req, BodyHandlers.ofByteArray());
               warmedUp = true;
            } catch (Exception var1) {
            }

         });
      }
   }

   public static CompletableFuture<String> translateAsync(String text, String fromLang, String toLang) {
      if (text != null && !text.isBlank()) {
         if (fromLang.equals(toLang)) {
            return CompletableFuture.completedFuture(text);
         } else {
            String cacheKey = fromLang + ">" + toLang + ":" + text.toLowerCase().trim();
            String cached = (String)TRANSLATION_CACHE.get(cacheKey);
            return cached != null ? CompletableFuture.completedFuture(cached) : CompletableFuture.supplyAsync(() -> {
               try {
                  return doTranslate(text, fromLang, toLang, cacheKey);
               } catch (Exception var5) {
                  return null;
               }
            }, EXECUTOR);
         }
      } else {
         return CompletableFuture.completedFuture("");
      }
   }

   private static String doTranslate(String text, String fromLang, String toLang, String cacheKey) throws Exception {
      String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
      String url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=" + fromLang + "&tl=" + toLang + "&dt=t&ie=UTF-8&oe=UTF-8&q=" + encoded;
      HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36").header("Accept-Charset", "UTF-8").header("Connection", "keep-alive").GET().timeout(Duration.ofSeconds(4L)).build();
      HttpResponse<byte[]> response = HTTP_CLIENT.send(request, BodyHandlers.ofByteArray());
      if (response.statusCode() != 200) {
         return null;
      } else {
         String body = new String((byte[])response.body(), StandardCharsets.UTF_8);
         JsonArray root = (JsonArray)GSON.fromJson(body, JsonArray.class);
         JsonArray sentences = root.get(0).getAsJsonArray();
         StringBuilder result = new StringBuilder();
         Iterator var12 = sentences.iterator();

         while(var12.hasNext()) {
            JsonElement sentence = (JsonElement)var12.next();
            JsonArray parts = sentence.getAsJsonArray();
            if (parts.size() > 0 && !parts.get(0).isJsonNull()) {
               result.append(parts.get(0).getAsString());
            }
         }

         String translated = result.toString().replace("+", " ").trim();
         if (translated.isEmpty()) {
            return null;
         } else {
            if (TRANSLATION_CACHE.size() >= 2000) {
               Iterator<String> keys = TRANSLATION_CACHE.keySet().iterator();

               for(int i = 0; i < 500 && keys.hasNext(); ++i) {
                  keys.next();
                  keys.remove();
               }
            }

            TRANSLATION_CACHE.put(cacheKey, translated);
            return translated;
         }
      }
   }

   public static void clearCache() {
      TRANSLATION_CACHE.clear();
   }

   static {
      HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4L)).version(Version.HTTP_2).build();
      GSON = new Gson();
      EXECUTOR = Executors.newFixedThreadPool(8);
      TRANSLATION_CACHE = new ConcurrentHashMap();
      warmedUp = false;
   }
}


