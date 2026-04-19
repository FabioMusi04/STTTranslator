package com.ilmusi.stttranslator.translation;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ilmusi.stttranslator.STTTranslator;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
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

public class LibreTranslateService {
   private static final HttpClient HTTP_CLIENT;
   private static final Gson GSON;
   private static final ExecutorService EXECUTOR;
   private static final Map<String, String> TRANSLATION_CACHE;
   private static final int MAX_CACHE_SIZE = 1000;

   public static CompletableFuture<String> translateAsync(String text, String fromLang, String toLang) {
      if (text != null && !text.isBlank()) {
         if (fromLang.equals(toLang)) {
            return CompletableFuture.completedFuture(text);
         } else {
            String cacheKey = fromLang + ">" + toLang + ":" + text.toLowerCase().trim();
            String cached = (String)TRANSLATION_CACHE.get(cacheKey);
            return cached != null ? CompletableFuture.completedFuture(cached) : CompletableFuture.supplyAsync(() -> {
               String result = tryLingva(text, fromLang, toLang);
               if (result != null) {
                  cacheResult(cacheKey, result);
                  return result;
               } else {
                  result = tryMyMemory(text, fromLang, toLang);
                  if (result != null) {
                     cacheResult(cacheKey, result);
                     return result;
                  } else {
                     result = tryGoogleDirect(text, fromLang, toLang);
                     if (result != null) {
                        cacheResult(cacheKey, result);
                        return result;
                     } else {
                        return null;
                     }
                  }
               }
            }, EXECUTOR);
         }
      } else {
         return CompletableFuture.completedFuture("");
      }
   }

   private static String cleanTranslation(String text) {
      return text == null ? null : text.replace("+", " ").trim();
   }

   private static String tryLingva(String text, String fromLang, String toLang) {
      String[] servers = new String[]{"https://lingva.ml/api/v1/", "https://lingva.thedaviddelta.com/api/v1/"};
      String[] var4 = servers;
      int var5 = servers.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         String server = var4[var6];

         try {
            String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = server + fromLang + "/" + toLang + "/" + encoded;
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("User-Agent", "Mozilla/5.0").header("Accept", "application/json; charset=UTF-8").GET().timeout(Duration.ofSeconds(8L)).build();
            HttpResponse<byte[]> response = HTTP_CLIENT.send(request, BodyHandlers.ofByteArray());
            if (response.statusCode() == 200) {
               String body = new String((byte[])response.body(), StandardCharsets.UTF_8);
               JsonObject json = (JsonObject)GSON.fromJson(body, JsonObject.class);
               if (json.has("translation")) {
                  String translated = cleanTranslation(json.get("translation").getAsString());
                  if (!translated.isEmpty() && !translated.equalsIgnoreCase(text)) {
                     return translated;
                  }
               }
            }
         } catch (Exception var15) {
            STTTranslator.LOGGER.debug("Lingva {} failed: {}", server, var15.getMessage());
         }
      }

      return null;
   }

   private static String tryMyMemory(String text, String fromLang, String toLang) {
      try {
         String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
         String url = "https://api.mymemory.translated.net/get?q=" + encoded + "&langpair=" + fromLang + "|" + toLang;
         HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("User-Agent", "Mozilla/5.0").header("Accept", "application/json; charset=UTF-8").GET().timeout(Duration.ofSeconds(8L)).build();
         HttpResponse<byte[]> response = HTTP_CLIENT.send(request, BodyHandlers.ofByteArray());
         if (response.statusCode() == 200) {
            String body = new String((byte[])response.body(), StandardCharsets.UTF_8);
            JsonObject json = (JsonObject)GSON.fromJson(body, JsonObject.class);
            if (json.has("responseData")) {
               JsonObject data = json.getAsJsonObject("responseData");
               if (data.has("translatedText")) {
                  String translated = cleanTranslation(data.get("translatedText").getAsString());
                  if (!translated.isEmpty() && !translated.equalsIgnoreCase(text) && !translated.contains("MYMEMORY WARNING") && !translated.contains("PLEASE SELECT TWO LANGUAGES")) {
                     return translated;
                  }
               }
            }
         }
      } catch (Exception var11) {
         STTTranslator.LOGGER.debug("MyMemory failed: {}", var11.getMessage());
      }

      return null;
   }

   private static String tryGoogleDirect(String text, String fromLang, String toLang) {
      try {
         String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
         String url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=" + fromLang + "&tl=" + toLang + "&dt=t&ie=UTF-8&oe=UTF-8&q=" + encoded;
         HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36").header("Accept-Charset", "UTF-8").GET().timeout(Duration.ofSeconds(6L)).build();
         HttpResponse<byte[]> response = HTTP_CLIENT.send(request, BodyHandlers.ofByteArray());
         if (response.statusCode() == 200) {
            String body = new String((byte[])response.body(), StandardCharsets.UTF_8);
            JsonArray root = (JsonArray)GSON.fromJson(body, JsonArray.class);
            JsonArray sentences = root.get(0).getAsJsonArray();
            StringBuilder result = new StringBuilder();
            Iterator var11 = sentences.iterator();

            while(var11.hasNext()) {
               JsonElement sentence = (JsonElement)var11.next();
               JsonArray parts = sentence.getAsJsonArray();
               if (parts.size() > 0 && !parts.get(0).isJsonNull()) {
                  result.append(parts.get(0).getAsString());
               }
            }

            String translated = cleanTranslation(result.toString());
            if (!translated.isEmpty()) {
               return translated;
            }
         }
      } catch (Exception var14) {
         STTTranslator.LOGGER.debug("Google failed: {}", var14.getMessage());
      }

      return null;
   }

   private static void cacheResult(String key, String value) {
      if (TRANSLATION_CACHE.size() >= 1000) {
         Iterator<String> keys = TRANSLATION_CACHE.keySet().iterator();

         for(int i = 0; i < 250 && keys.hasNext(); ++i) {
            keys.next();
            keys.remove();
         }
      }

      TRANSLATION_CACHE.put(key, value);
   }

   public static void clearCache() {
      TRANSLATION_CACHE.clear();
   }

   static {
      HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8L)).version(Version.HTTP_2).followRedirects(Redirect.NORMAL).build();
      GSON = new Gson();
      EXECUTOR = Executors.newFixedThreadPool(4);
      TRANSLATION_CACHE = new ConcurrentHashMap();
   }
}


