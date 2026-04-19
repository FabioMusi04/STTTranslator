package com.ilmusi.stttranslator.voice;

import com.ilmusi.stttranslator.STTTranslator;
import java.nio.file.Path;

public class SpeechRecognizer {
   private VoskBridge bridge;
   private boolean initialized = false;
   private String currentLanguage = "";

   public boolean initialize(String langCode) {
      try {
         Path modelPath = ModelDownloader.getModelPath(langCode);
         if (modelPath == null) {
            return false;
         } else {
            if (this.initialized && !langCode.equals(this.currentLanguage)) {
               this.close();
            }

            if (this.initialized) {
               return true;
            } else {
               this.bridge = new VoskBridge();
               boolean success = this.bridge.initialize(modelPath.toString());
               if (success) {
                  this.initialized = true;
                  this.currentLanguage = langCode;
               }

               return success;
            }
         }
      } catch (Exception var4) {
         STTTranslator.LOGGER.error("Failed to init speech recognizer", var4);
         return false;
      }
   }

   public String processAudio(byte[] audioData, int length) {
      return this.initialized && this.bridge != null ? this.bridge.processAudio(audioData, length) : null;
   }

   public String finalizeAudio() {
      return this.initialized && this.bridge != null ? this.bridge.finalizeAudio() : null;
   }

   public boolean isInitialized() {
      return this.initialized;
   }

   public String getCurrentLanguage() {
      return this.currentLanguage;
   }

   public void close() {
      if (this.bridge != null) {
         this.bridge.close();
      }

      this.bridge = null;
      this.initialized = false;
      this.currentLanguage = "";
   }
}


