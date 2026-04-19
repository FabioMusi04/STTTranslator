package com.ilmusi.stttranslator.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ilmusi.stttranslator.STTTranslator;
import com.ilmusi.stttranslator.translation.TranslationManager;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import net.neoforged.fml.loading.FMLPaths;

public class ModConfig {
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private static final String CONFIG_FILE = "stt-translator.json";
   private String translationProvider = "LIBRE";
   private String claudeApiKey = "";
   private String deeplApiKey = "";
   private String sourceLanguage = "es";
   private String targetLanguage = "en";
   private String bubbleSourceLanguage = "es";
   private String bubbleTargetLanguage = "en";
   private boolean translateChat = true;
   private boolean showBubbles = true;
   private boolean showOriginal = true;
   private boolean useLargeModel = true;
   private boolean darkBubbles = true;
   private boolean darkPanel = true;
   private boolean darkMenu = true;
   private int bubbleDurationTicks = 200;
   private float bubbleScale = 1.0F;
   private int bubbleMaxWidth = 200;
   private float bubbleOpacity = 0.85F;
   private int panelPosition = 0;
   public static final List<ModConfig.LanguageEntry> SUPPORTED_LANGUAGES = Arrays.asList(new ModConfig.LanguageEntry("en", "English", "\ud83c\uddfa\ud83c\uddf8"), new ModConfig.LanguageEntry("es", "Espa\u00F1ol", "\ud83c\uddea\ud83c\uddf8"), new ModConfig.LanguageEntry("pt", "Portugu\u00EAs", "\ud83c\udde7\ud83c\uddf7"), new ModConfig.LanguageEntry("fr", "Fran\u00E7ais", "\ud83c\uddeb\ud83c\uddf7"), new ModConfig.LanguageEntry("de", "Deutsch", "\ud83c\udde9\ud83c\uddea"), new ModConfig.LanguageEntry("ko", "\uD55C\uAD6D\uC5B4", "\ud83c\uddf0\ud83c\uddf7"), new ModConfig.LanguageEntry("ja", "\u65E5\u672C\u8A9E", "\ud83c\uddef\ud83c\uddf5"), new ModConfig.LanguageEntry("zh", "\u4E2D\u6587", "\ud83c\udde8\ud83c\uddf3"), new ModConfig.LanguageEntry("it", "Italiano", "\ud83c\uddee\ud83c\uddf9"), new ModConfig.LanguageEntry("ru", "\u0420\u0443\u0441\u0441\u043A\u0438\u0439", "\ud83c\uddf7\ud83c\uddfa"), new ModConfig.LanguageEntry("ar", "\u0627\u0644\u0639\u0631\u0628\u064A\u0629", "\ud83c\uddf8\ud83c\udde6"), new ModConfig.LanguageEntry("hi", "\u0939\u093F\u0928\u094D\u0926\u0940", "\ud83c\uddee\ud83c\uddf3"), new ModConfig.LanguageEntry("th", "\u0E44\u0E17\u0E22", "\ud83c\uddf9\ud83c\udded"), new ModConfig.LanguageEntry("tr", "T\u00FCrk\u00E7e", "\ud83c\uddf9\ud83c\uddf7"), new ModConfig.LanguageEntry("pl", "Polski", "\ud83c\uddf5\ud83c\uddf1"), new ModConfig.LanguageEntry("nl", "Nederlands", "\ud83c\uddf3\ud83c\uddf1"));

   public void load() {
      Path configPath = FMLPaths.CONFIGDIR.get().resolve("stt-translator.json");
      if (Files.exists(configPath, new LinkOption[0])) {
         try {
            FileReader reader = new FileReader(configPath.toFile());

            try {
               ModConfig loaded = (ModConfig)GSON.fromJson(reader, ModConfig.class);
               if (loaded != null) {
                  this.translationProvider = loaded.translationProvider;
                  this.claudeApiKey = loaded.claudeApiKey;
                  this.deeplApiKey = loaded.deeplApiKey;
                  this.sourceLanguage = loaded.sourceLanguage;
                  this.targetLanguage = loaded.targetLanguage;
                  this.bubbleSourceLanguage = loaded.bubbleSourceLanguage;
                  this.bubbleTargetLanguage = loaded.bubbleTargetLanguage;
                  this.translateChat = loaded.translateChat;
                  this.showBubbles = loaded.showBubbles;
                  this.showOriginal = loaded.showOriginal;
                  this.useLargeModel = loaded.useLargeModel;
                  this.darkBubbles = loaded.darkBubbles;
                  this.darkPanel = loaded.darkPanel;
                  this.darkMenu = loaded.darkMenu;
                  this.bubbleDurationTicks = loaded.bubbleDurationTicks;
                  this.bubbleScale = loaded.bubbleScale;
                  this.bubbleMaxWidth = loaded.bubbleMaxWidth;
                  this.bubbleOpacity = loaded.bubbleOpacity;
                  this.panelPosition = loaded.panelPosition;
               }
            } catch (Throwable var6) {
               try {
                  reader.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }

               throw var6;
            }

            reader.close();
         } catch (Exception var7) {
            this.save();
         }
      } else {
         this.save();
      }

   }

   public void save() {
      Path configPath = FMLPaths.CONFIGDIR.get().resolve("stt-translator.json");

      try {
         FileWriter writer = new FileWriter(configPath.toFile());

         try {
            GSON.toJson(this, writer);
         } catch (Throwable var6) {
            try {
               writer.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }

            throw var6;
         }

         writer.close();
      } catch (Exception var7) {
         STTTranslator.LOGGER.error("Failed to save config", var7);
      }

   }

   public TranslationManager.Provider getTranslationProvider() {
      try {
         return TranslationManager.Provider.valueOf(this.translationProvider);
      } catch (Exception var2) {
         return TranslationManager.Provider.LIBRE;
      }
   }

   public void setTranslationProvider(TranslationManager.Provider p) {
      this.translationProvider = p.name();
      this.save();
   }

   public String getClaudeApiKey() {
      return this.claudeApiKey;
   }

   public void setClaudeApiKey(String k) {
      this.claudeApiKey = k;
      this.save();
   }

   public String getDeeplApiKey() {
      return this.deeplApiKey;
   }

   public void setDeeplApiKey(String k) {
      this.deeplApiKey = k;
      this.save();
   }

   public String getSourceLanguage() {
      return this.sourceLanguage;
   }

   public void setSourceLanguage(String l) {
      this.sourceLanguage = l;
      this.save();
   }

   public String getTargetLanguage() {
      return this.targetLanguage;
   }

   public void setTargetLanguage(String l) {
      this.targetLanguage = l;
      this.save();
   }

   public String getBubbleSourceLanguage() {
      return this.bubbleSourceLanguage;
   }

   public void setBubbleSourceLanguage(String l) {
      this.bubbleSourceLanguage = l;
      this.save();
   }

   public String getBubbleTargetLanguage() {
      return this.bubbleTargetLanguage;
   }

   public void setBubbleTargetLanguage(String l) {
      this.bubbleTargetLanguage = l;
      this.save();
   }

   public boolean isTranslateChat() {
      return this.translateChat;
   }

   public void setTranslateChat(boolean v) {
      this.translateChat = v;
      this.save();
   }

   public boolean isShowBubbles() {
      return this.showBubbles;
   }

   public void setShowBubbles(boolean v) {
      this.showBubbles = v;
      this.save();
   }

   public boolean isShowOriginal() {
      return this.showOriginal;
   }

   public void setShowOriginal(boolean v) {
      this.showOriginal = v;
      this.save();
   }

   public boolean isUseLargeModel() {
      return this.useLargeModel;
   }

   public void setUseLargeModel(boolean v) {
      this.useLargeModel = v;
      this.save();
   }

   public boolean isDarkBubbles() {
      return this.darkBubbles;
   }

   public void setDarkBubbles(boolean v) {
      this.darkBubbles = v;
      this.save();
   }

   public boolean isDarkPanel() {
      return this.darkPanel;
   }

   public void setDarkPanel(boolean v) {
      this.darkPanel = v;
      this.save();
   }

   public boolean isDarkMenu() {
      return this.darkMenu;
   }

   public void setDarkMenu(boolean v) {
      this.darkMenu = v;
      this.save();
   }

   public int getBubbleDurationTicks() {
      return this.bubbleDurationTicks;
   }

   public void setBubbleDurationTicks(int v) {
      this.bubbleDurationTicks = v;
      this.save();
   }

   public float getBubbleScale() {
      return this.bubbleScale;
   }

   public void setBubbleScale(float v) {
      this.bubbleScale = v;
      this.save();
   }

   public int getBubbleMaxWidth() {
      return this.bubbleMaxWidth;
   }

   public float getBubbleOpacity() {
      return this.bubbleOpacity;
   }

   public int getPanelPosition() {
      return this.panelPosition;
   }

   public void setPanelPosition(int v) {
      this.panelPosition = v;
      this.save();
   }

   public static String getLanguageName(String code) {
      return (String)SUPPORTED_LANGUAGES.stream().filter((l) -> {
         return l.code.equals(code);
      }).findFirst().map((l) -> {
         return l.name;
      }).orElse(code);
   }

   public static class LanguageEntry {
      public final String code;
      public final String name;
      public final String flag;

      public LanguageEntry(String code, String name, String flag) {
         this.code = code;
         this.name = name;
         this.flag = flag;
      }
   }
}


