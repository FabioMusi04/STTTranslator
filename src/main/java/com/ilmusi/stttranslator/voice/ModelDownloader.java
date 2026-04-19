package com.ilmusi.stttranslator.voice;

import com.ilmusi.stttranslator.STTTranslator;
import com.ilmusi.stttranslator.config.ModConfig;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.neoforged.fml.loading.FMLPaths;

public class ModelDownloader {
   private static final Map<String, String> SMALL_MODELS = new HashMap<>();
   private static final Map<String, String> LARGE_MODELS = new HashMap<>();

   private static Path getModelsDir() {
      return FMLPaths.GAMEDIR.get().resolve("stt-translator").resolve("models");
   }

   private static Map<String, String> getModelMap() {
      ModConfig config = STTTranslator.getInstance().getConfig();
      return config.isUseLargeModel() ? LARGE_MODELS : SMALL_MODELS;
   }

   public static Path getModelPath(String langCode) {
      Path modelsDir = getModelsDir();
      String largeName = (String)LARGE_MODELS.get(langCode);
      if (largeName != null) {
         Path p = modelsDir.resolve(largeName.replace(".zip", ""));
         if (Files.exists(p, new LinkOption[0]) && Files.isDirectory(p, new LinkOption[0])) {
            return p;
         }
      }

      String smallName = (String)SMALL_MODELS.get(langCode);
      if (smallName != null) {
         Path p = modelsDir.resolve(smallName.replace(".zip", ""));
         if (Files.exists(p, new LinkOption[0]) && Files.isDirectory(p, new LinkOption[0])) {
            return p;
         }
      }

      return null;
   }

   public static boolean isModelDownloaded(String langCode) {
      return getModelPath(langCode) != null;
   }

   public static boolean hasModelAvailable(String langCode) {
      return SMALL_MODELS.containsKey(langCode) || LARGE_MODELS.containsKey(langCode);
   }

   public static String getModelSize(String langCode) {
      ModConfig config = STTTranslator.getInstance().getConfig();
      return config.isUseLargeModel() && LARGE_MODELS.containsKey(langCode) ? "~1GB HD" : "~50MB";
   }

   public static CompletableFuture<Path> downloadModelAsync(String langCode, ModelDownloader.DownloadProgressListener listener) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            return downloadModel(langCode, listener);
         } catch (Exception var3) {
            throw new RuntimeException(var3.getMessage(), var3);
         }
      });
   }

   private static Path downloadModel(String langCode, ModelDownloader.DownloadProgressListener listener) throws Exception {
      Map<String, String> models = getModelMap();
      String modelZip = (String)models.get(langCode);
      if (modelZip == null) {
         modelZip = (String)SMALL_MODELS.get(langCode);
      }

      if (modelZip == null) {
         throw new RuntimeException("No model for: " + langCode);
      } else {
         Path modelsDir = getModelsDir();
         Files.createDirectories(modelsDir);
         String url = "https://alphacephei.com/vosk/models/" + modelZip;
         Path zipFile = modelsDir.resolve(modelZip);
         if (listener != null) {
            listener.onProgress("Connecting...", 0);
         }

         HttpURLConnection conn = (HttpURLConnection)URI.create(url).toURL().openConnection();
         conn.setRequestProperty("User-Agent", "STTTranslator/1.0");
         int totalSize = conn.getContentLength();
         InputStream in = conn.getInputStream();

         try {
            FileOutputStream out = new FileOutputStream(zipFile.toFile());

            try {
               byte[] buf = new byte[16384];
               long dl = 0L;

               int read;
               while((read = in.read(buf)) != -1) {
                  out.write(buf, 0, read);
                  dl += (long)read;
                  if (listener != null && totalSize > 0) {
                     int pct = (int)(dl * 100L / (long)totalSize);
                     listener.onProgress("Downloading: " + dl / 1024L / 1024L + "/" + totalSize / 1024 / 1024 + " MB", pct);
                  }
               }
            } catch (Throwable var22) {
               try {
                  out.close();
               } catch (Throwable var17) {
                  var22.addSuppressed(var17);
               }

               throw var22;
            }

            out.close();
         } catch (Throwable var23) {
            if (in != null) {
               try {
                  in.close();
               } catch (Throwable var16) {
                  var23.addSuppressed(var16);
               }
            }

            throw var23;
         }

         if (in != null) {
            in.close();
         }

         if (listener != null) {
            listener.onProgress("Extracting...", 95);
         }

         ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile.toFile()));

         ZipEntry entry;
         try {
            for(; (entry = zis.getNextEntry()) != null; zis.closeEntry()) {
               Path ep = modelsDir.resolve(entry.getName());
               if (entry.isDirectory()) {
                  Files.createDirectories(ep);
               } else {
                  Files.createDirectories(ep.getParent());
                  FileOutputStream fos = new FileOutputStream(ep.toFile());

                  try {
                     byte[] b = new byte[16384];

                     int l;
                     while((l = zis.read(b)) > 0) {
                        fos.write(b, 0, l);
                     }
                  } catch (Throwable var20) {
                     try {
                        fos.close();
                     } catch (Throwable var19) {
                        var20.addSuppressed(var19);
                     }

                     throw var20;
                  }

                  fos.close();
               }
            }
         } catch (Throwable var21) {
            try {
               zis.close();
            } catch (Throwable var18) {
               var21.addSuppressed(var18);
            }

            throw var21;
         }

         zis.close();
         Files.deleteIfExists(zipFile);
         if (listener != null) {
            listener.onProgress("Done!", 100);
         }

         return getModelPath(langCode);
      }
   }

   static {
      SMALL_MODELS.put("en", "vosk-model-small-en-us-0.15.zip");
      SMALL_MODELS.put("es", "vosk-model-small-es-0.42.zip");
      SMALL_MODELS.put("pt", "vosk-model-small-pt-0.3.zip");
      SMALL_MODELS.put("fr", "vosk-model-small-fr-0.22.zip");
      SMALL_MODELS.put("de", "vosk-model-small-de-0.15.zip");
      SMALL_MODELS.put("ru", "vosk-model-small-ru-0.22.zip");
      SMALL_MODELS.put("it", "vosk-model-small-it-0.22.zip");
      SMALL_MODELS.put("zh", "vosk-model-small-cn-0.22.zip");
      SMALL_MODELS.put("ja", "vosk-model-small-ja-0.22.zip");
      SMALL_MODELS.put("ko", "vosk-model-small-ko-0.22.zip");
      SMALL_MODELS.put("tr", "vosk-model-small-tr-0.3.zip");
      SMALL_MODELS.put("pl", "vosk-model-small-pl-0.22.zip");
      SMALL_MODELS.put("nl", "vosk-model-small-nl-0.22.zip");
      SMALL_MODELS.put("hi", "vosk-model-small-hi-0.22.zip");
      LARGE_MODELS.put("en", "vosk-model-en-us-0.22.zip");
      LARGE_MODELS.put("es", "vosk-model-es-0.42.zip");
      LARGE_MODELS.put("fr", "vosk-model-fr-0.22.zip");
      LARGE_MODELS.put("de", "vosk-model-de-0.21.zip");
      LARGE_MODELS.put("ru", "vosk-model-ru-0.42.zip");
      LARGE_MODELS.put("it", "vosk-model-it-0.22.zip");
      LARGE_MODELS.put("zh", "vosk-model-cn-0.22.zip");
      LARGE_MODELS.put("ja", "vosk-model-ja-0.22.zip");
      LARGE_MODELS.put("ko", "vosk-model-ko-0.22.zip");
   }

   public interface DownloadProgressListener {
      void onProgress(String var1, int var2);
   }
}


