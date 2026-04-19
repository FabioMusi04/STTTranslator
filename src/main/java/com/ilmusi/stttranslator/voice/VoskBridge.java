package com.ilmusi.stttranslator.voice;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ilmusi.stttranslator.STTTranslator;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import net.neoforged.fml.loading.FMLPaths;

public class VoskBridge {
   private static final String VOSK_JAR_URL = "https://repo1.maven.org/maven2/com/alphacephei/vosk/0.3.45/vosk-0.3.45.jar";
   private static final String JNA_JAR_URL = "https://repo1.maven.org/maven2/net/java/dev/jna/jna/5.14.0/jna-5.14.0.jar";
   private static final Gson GSON = new Gson();
   private ClassLoader voskClassLoader;
   private Object model;
   private Object recognizer;
   private Method acceptWaveFormMethod;
   private Method getResultMethod;
   private Method getPartialResultMethod;
   private Method getFinalResultMethod;
   private Method recognizerCloseMethod;
   private Method modelCloseMethod;
   private boolean initialized = false;

   public boolean initialize(String modelPath) {
      try {
         Path libsDir = getLibsDir();
         Files.createDirectories(libsDir);
         Path voskJar = libsDir.resolve("vosk-0.3.45.jar");
         Path jnaJar = libsDir.resolve("jna-5.14.0.jar");
         if (!Files.exists(voskJar, new LinkOption[0])) {
            downloadFile(VOSK_JAR_URL, voskJar);
         }

         if (!Files.exists(jnaJar, new LinkOption[0])) {
            downloadFile(JNA_JAR_URL, jnaJar);
         }

         URL[] urls = new URL[]{voskJar.toUri().toURL(), jnaJar.toUri().toURL()};
         this.voskClassLoader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
         Class<?> libVoskClass = this.voskClassLoader.loadClass("org.vosk.LibVosk");
         Class<?> logLevelClass = this.voskClassLoader.loadClass("org.vosk.LogLevel");
         Object warningsLevel = logLevelClass.getField("WARNINGS").get((Object)null);
         libVoskClass.getMethod("setLogLevel", logLevelClass).invoke((Object)null, warningsLevel);
         Class<?> modelClass = this.voskClassLoader.loadClass("org.vosk.Model");
         this.model = modelClass.getConstructor(String.class).newInstance(modelPath);
         Class<?> recClass = this.voskClassLoader.loadClass("org.vosk.Recognizer");
         this.recognizer = recClass.getConstructor(modelClass, Float.TYPE).newInstance(this.model, 16000.0F);

         try {
            recClass.getMethod("setWords", Boolean.TYPE).invoke(this.recognizer, true);
         } catch (Exception var14) {
         }

         try {
            recClass.getMethod("setPartialWords", Boolean.TYPE).invoke(this.recognizer, true);
         } catch (Exception var13) {
         }

         try {
            recClass.getMethod("setMaxAlternatives", Integer.TYPE).invoke(this.recognizer, 3);
         } catch (Exception var12) {
         }

         this.acceptWaveFormMethod = recClass.getMethod("acceptWaveForm", byte[].class, Integer.TYPE);
         this.getResultMethod = recClass.getMethod("getResult");
         this.getPartialResultMethod = recClass.getMethod("getPartialResult");
         this.getFinalResultMethod = recClass.getMethod("getFinalResult");
         this.recognizerCloseMethod = recClass.getMethod("close");
         this.modelCloseMethod = modelClass.getMethod("close");
         this.initialized = true;
         return true;
      } catch (Exception var15) {
         STTTranslator.LOGGER.error("VoskBridge init failed", var15);
         return false;
      }
   }

   public String processAudio(byte[] data, int length) {
      if (!this.initialized) {
         return null;
      } else {
         try {
            boolean complete = (Boolean)this.acceptWaveFormMethod.invoke(this.recognizer, data, length);
            if (complete) {
               return this.extractBestText((String)this.getResultMethod.invoke(this.recognizer));
            }

            return this.extractPartialText((String)this.getPartialResultMethod.invoke(this.recognizer));
         } catch (Exception var4) {
            return null;
         }
      }
   }

   public String finalizeAudio() {
      if (!this.initialized) {
         return null;
      }

      try {
         return this.extractBestText((String)this.getFinalResultMethod.invoke(this.recognizer));
      } catch (Exception var2) {
         return null;
      }
   }

   private String extractBestText(String json) {
      try {
         JsonObject obj = (JsonObject)GSON.fromJson(json, JsonObject.class);
         if (obj == null) {
            return null;
         }

         if (obj.has("alternatives")) {
            JsonArray alts = obj.getAsJsonArray("alternatives");
            if (alts != null && !alts.isEmpty()) {
               JsonObject best = alts.get(0).getAsJsonObject();
               if (best.has("text")) {
                  String t = best.get("text").getAsString().trim();
                  return t.isEmpty() ? null : t;
               }
            }
         }

         if (obj.has("text")) {
            String t = obj.get("text").getAsString().trim();
            return t.isEmpty() ? null : t;
         }
      } catch (Exception var6) {
      }

      return null;
   }

   private String extractPartialText(String json) {
      try {
         JsonObject obj = (JsonObject)GSON.fromJson(json, JsonObject.class);
         if (obj != null && obj.has("partial")) {
            String t = obj.get("partial").getAsString().trim();
            return t.isEmpty() ? null : t;
         }
      } catch (Exception var4) {
      }

      return null;
   }

   public boolean isInitialized() {
      return this.initialized;
   }

   public void close() {
      try {
         if (this.recognizer != null) {
            this.recognizerCloseMethod.invoke(this.recognizer);
         }

         if (this.model != null) {
            this.modelCloseMethod.invoke(this.model);
         }
      } catch (Exception var2) {
      }

      this.initialized = false;
      this.model = null;
      this.recognizer = null;
   }

   private static Path getLibsDir() {
      return FMLPaths.GAMEDIR.get().resolve("stt-translator").resolve("libs");
   }

   public static boolean areLibsDownloaded() {
      Path d = getLibsDir();
      return Files.exists(d.resolve("vosk-0.3.45.jar"), new LinkOption[0]) && Files.exists(d.resolve("jna-5.14.0.jar"), new LinkOption[0]);
   }

   private static void downloadFile(String url, Path target) throws Exception {
      HttpURLConnection c = (HttpURLConnection)URI.create(url).toURL().openConnection();
      c.setRequestProperty("User-Agent", "STTTranslator/1.0");
      InputStream in = c.getInputStream();

      try {
         FileOutputStream out = new FileOutputStream(target.toFile());

         try {
            byte[] buf = new byte[8192];

            int r;
            while((r = in.read(buf)) != -1) {
               out.write(buf, 0, r);
            }
         } catch (Throwable var9) {
            try {
               out.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }

            throw var9;
         }

         out.close();
      } catch (Throwable var10) {
         if (in != null) {
            try {
               in.close();
            } catch (Throwable var7) {
               var10.addSuppressed(var7);
            }
         }

         throw var10;
      }

      if (in != null) {
         in.close();
      }

   }
}


