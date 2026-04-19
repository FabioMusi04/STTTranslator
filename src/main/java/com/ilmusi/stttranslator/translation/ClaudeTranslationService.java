package com.ilmusi.stttranslator.translation;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ilmusi.stttranslator.STTTranslator;
import com.ilmusi.stttranslator.config.ModConfig;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClaudeTranslationService {
   private static final String API_URL = "https://api.anthropic.com/v1/messages";
   private static final String MODEL = "claude-sonnet-4-20250514";
   private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L)).build();
   private static final Gson GSON = new Gson();
   private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);
   private static final Map<String, String> TRANSLATION_CACHE = new ConcurrentHashMap();
   private static final int MAX_CACHE_SIZE = 500;

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
                  STTTranslator.LOGGER.error("Translation failed for '{}': {}", text, var6.getMessage());
                  return "[!] " + text;
               }
            }, EXECUTOR);
         }
      } else {
         return CompletableFuture.completedFuture("");
      }
   }

   private static String doTranslate(String text, String fromLang, String toLang, String apiKey, String cacheKey) throws Exception {
      String fromName = ModConfig.getLanguageName(fromLang);
      String toName = ModConfig.getLanguageName(toLang);
      String prompt = String.format("You are a real-time game chat translator. Translate the following message from %s to %s. Rules: Return ONLY the translated text. No quotes, no explanation, no extra text. Keep player names, item names, and Minecraft terms unchanged. Keep the tone casual and natural, like gamers talking.\n\nMessage: %s", fromName, toName, text);
      JsonObject requestBody = new JsonObject();
      requestBody.addProperty("model", "claude-sonnet-4-20250514");
      requestBody.addProperty("max_tokens", 300);
      JsonArray messages = new JsonArray();
      JsonObject message = new JsonObject();
      message.addProperty("role", "user");
      message.addProperty("content", prompt);
      messages.add(message);
      requestBody.add("messages", messages);
      HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api.anthropic.com/v1/messages")).header("Content-Type", "application/json").header("x-api-key", apiKey).header("anthropic-version", "2023-06-01").POST(BodyPublishers.ofString(GSON.toJson(requestBody))).timeout(Duration.ofSeconds(15L)).build();
      HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
      if (response.statusCode() != 200) {
         STTTranslator.LOGGER.warn("Claude API returned status {}: {}", response.statusCode(), response.body());
         if (response.statusCode() == 401) {
            return "[API Key Invalid] " + text;
         } else {
            int var10000 = response.statusCode();
            return "[Error " + var10000 + "] " + text;
         }
      } else {
         JsonObject responseJson = (JsonObject)GSON.fromJson((String)response.body(), JsonObject.class);
         JsonArray content = responseJson.getAsJsonArray("content");
         if (content != null && !content.isEmpty()) {
            String translated = content.get(0).getAsJsonObject().get("text").getAsString().trim();
            if (TRANSLATION_CACHE.size() >= 500) {
               TRANSLATION_CACHE.clear();
            }

            TRANSLATION_CACHE.put(cacheKey, translated);
            return translated;
         } else {
            return text;
         }
      }
   }

   public static void clearCache() {
      TRANSLATION_CACHE.clear();
   }
}


