package com.ilmusi.stttranslator.voice;

import com.ilmusi.stttranslator.STTTranslator;
import com.ilmusi.stttranslator.config.ModConfig;
import com.ilmusi.stttranslator.network.SttSubmitPayload;
import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.ClientSoundEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.neoforged.neoforge.network.PacketDistributor;

@ForgeVoicechatPlugin
public class STTVoicechatPlugin implements VoicechatPlugin {
   private static final String FLOW = "[VOICEFLOW]";
   private static final long SILENCE_TIMEOUT = 1000L;
   private static final double SILENCE_RMS_THRESHOLD = 220.0D;
   private static final int SILENT_PACKET_STREAK_TO_FLUSH = 50;
   private static final String[] JUNK = new String[]{"eh", "uh", "um", "ah", "oh", "hm", "hmm", "mmm", "mm"};
   private static final SpeechRecognizer CLIENT_RECOGNIZER = new SpeechRecognizer();
   private static final ScheduledExecutorService SILENCE_FLUSH_EXECUTOR = Executors.newSingleThreadScheduledExecutor((r) -> {
      Thread t = new Thread(r, "qlobal-stt-silence-flush");
      t.setDaemon(true);
      return t;
   });
   private static final Object CLIENT_LOCK = new Object();
   private static final StringBuilder CLIENT_CHUNK = new StringBuilder();
   private static String clientSourceLanguage = "";
   private static String clientTargetLanguage = "";
   private static String clientLanguage = "";
   private static String lastRecognizerText = "";
   private static String lastSubmittedFullText = "";
   private static long lastSpeechTime = 0L;
   private static int silentPacketStreak = 0;
   private static ScheduledFuture<?> pendingSilenceFlush;
   private static long pendingSilenceGeneration = 0L;

   @Override
   public String getPluginId() {
      return STTTranslator.MOD_ID;
   }

   @Override
   public void initialize(VoicechatApi api) {
      STTTranslator.LOGGER.info("Simple Voice Chat bridge initialized (client STT mode)");
   }

   @Override
   public void registerEvents(EventRegistration registration) {
      registration.registerEvent(ClientSoundEvent.class, this::onClientSound);
   }

   private void onClientSound(ClientSoundEvent event) {
      STTTranslator instance = STTTranslator.getInstance();
      if (instance == null) {
         return;
      }

      ModConfig config = instance.getConfig();
      String sourceLang = config.getSourceLanguage();
      String targetLang = config.getTargetLanguage();

      if (!sourceLang.equals(clientSourceLanguage) || !targetLang.equals(clientTargetLanguage)) {
         resetClientSpeechState(true);
         clientSourceLanguage = sourceLang;
         clientTargetLanguage = targetLang;
      }

      short[] rawAudio = event.getRawAudio();
      if (rawAudio == null || rawAudio.length == 0) {
         scheduleSilenceFlush(sourceLang, targetLang, "client_packet_end");
         return;
      }

      if (!CLIENT_RECOGNIZER.isInitialized() || !sourceLang.equals(clientLanguage)) {
         if (!ModelDownloader.isModelDownloaded(sourceLang) || !CLIENT_RECOGNIZER.initialize(sourceLang)) {
            STTTranslator.LOGGER.debug("{} Client recognizer not ready for lang={}", FLOW, sourceLang);
            return;
         }
         clientLanguage = sourceLang;
         lastRecognizerText = "";
      }

      byte[] pcm16k = downsamplePcm48kTo16k(rawAudio);
      if (pcm16k.length == 0) {
         return;
      }

      String sttText = CLIENT_RECOGNIZER.processAudio(pcm16k, pcm16k.length);
      long now = System.currentTimeMillis();
      boolean silentPacket = isMostlySilent(rawAudio);

      synchronized(CLIENT_LOCK) {
         if (sttText != null && !sttText.isBlank() && !isJunk(sttText)) {
            String recognized = sttText.trim();
            appendRecognizerText(recognized);
            silentPacketStreak = 0;
            lastSpeechTime = now;
            STTTranslator.LOGGER.debug("{} CLIENT_STT_FRAGMENT text='{}'", FLOW, recognized);
         } else {
            if (silentPacket) {
               silentPacketStreak++;
            } else {
               silentPacketStreak = 0;
            }

            if (CLIENT_CHUNK.length() > 0) {
               lastSpeechTime = now;
            }
         }

         scheduleSilenceFlushLocked(sourceLang, targetLang, "audio_packet");
      }
   }

   private static void scheduleSilenceFlush(String sourceLang, String targetLang, String reason) {
      synchronized(CLIENT_LOCK) {
         scheduleSilenceFlushLocked(sourceLang, targetLang, reason);
      }
   }

   private static void scheduleSilenceFlushLocked(String sourceLang, String targetLang, String reason) {
      if (pendingSilenceFlush != null) {
         pendingSilenceFlush.cancel(false);
      }

      long generation = ++pendingSilenceGeneration;
      pendingSilenceFlush = SILENCE_FLUSH_EXECUTOR.schedule(() -> flushAfterSilence(generation, sourceLang, targetLang, reason), SILENCE_TIMEOUT, TimeUnit.MILLISECONDS);
   }

   private static void flushAfterSilence(long generation, String sourceLang, String targetLang, String reason) {
      String text;
      String finalText = null;

      synchronized(CLIENT_LOCK) {
         if (generation != pendingSilenceGeneration) {
            return;
         }

         pendingSilenceFlush = null;
         finalText = CLIENT_RECOGNIZER.finalizeAudio();
         if (finalText != null && !finalText.isBlank() && !isJunk(finalText)) {
            appendRecognizerText(finalText.trim());
            STTTranslator.LOGGER.debug("{} CLIENT_STT_FINAL text='{}'", FLOW, finalText.trim());
         }

         text = CLIENT_CHUNK.toString().trim();
         CLIENT_CHUNK.setLength(0);
         lastRecognizerText = "";
         silentPacketStreak = 0;
      }

      if (text.isEmpty() || text.length() < 3 || isJunk(text)) {
         return;
      }

      STTTranslator instance = STTTranslator.getInstance();
      if (instance != null) {
         ModConfig config = instance.getConfig();
         String currentSource = config.getSourceLanguage();
         String currentTarget = config.getTargetLanguage();
         if (!sourceLang.equals(currentSource) || !targetLang.equals(currentTarget)) {
            STTTranslator.LOGGER.debug("{} Dropping stale flush old={}=>{} current={}=>{}", FLOW, sourceLang, targetLang, currentSource, currentTarget);
            return;
         }
      }

      String submitText = text;
      if (!lastSubmittedFullText.isEmpty()) {
         if (text.equals(lastSubmittedFullText)) {
            return;
         }

         if (text.startsWith(lastSubmittedFullText)) {
            String delta = text.substring(lastSubmittedFullText.length()).trim();
            if (delta.length() < 3 || isJunk(delta)) {
               return;
            }

            submitText = delta;
         }
      }

      lastSubmittedFullText = text;

      STTTranslator.LOGGER.info("{} CLIENT_SEND reason={} from={} to={} stt='{}'", FLOW, reason, sourceLang, targetLang, submitText);

      try {
         PacketDistributor.sendToServer(new SttSubmitPayload(submitText, sourceLang, targetLang, System.currentTimeMillis()));
      } catch (Exception e) {
         STTTranslator.LOGGER.debug("{} Failed to send STT payload to server", FLOW, e);
      }
   }

   public static void resetClientSpeechState(boolean clearSubmittedText) {
      synchronized(CLIENT_LOCK) {
         if (pendingSilenceFlush != null) {
            pendingSilenceFlush.cancel(false);
            pendingSilenceFlush = null;
         }

         pendingSilenceGeneration++;
         CLIENT_CHUNK.setLength(0);
         lastRecognizerText = "";
         silentPacketStreak = 0;
         lastSpeechTime = 0L;
         if (clearSubmittedText) {
            lastSubmittedFullText = "";
         }
      }
   }

   private static void appendRecognizerText(String recognized) {
      if (recognized == null || recognized.isBlank() || isJunk(recognized)) {
         return;
      }

      if (!recognized.equals(lastRecognizerText)) {
         if (!lastRecognizerText.isEmpty() && recognized.startsWith(lastRecognizerText)) {
            String delta = recognized.substring(lastRecognizerText.length()).trim();
            if (!delta.isEmpty()) {
               if (CLIENT_CHUNK.length() > 0) {
                  CLIENT_CHUNK.append(" ");
               }

               CLIENT_CHUNK.append(delta);
            }
         } else {
            if (CLIENT_CHUNK.length() > 0) {
               CLIENT_CHUNK.append(" ");
            }

            CLIENT_CHUNK.append(recognized);
         }

         lastRecognizerText = recognized;
      }
   }

   private static boolean isJunk(String text) {
      if (text == null || text.isBlank() || text.length() < 3) {
         return true;
      }
      String normalized = text.toLowerCase().trim();
      for (String junk : JUNK) {
         if (normalized.equals(junk)) {
            return true;
         }
      }
      return false;
   }

   private static byte[] downsamplePcm48kTo16k(short[] pcm48k) {
      if (pcm48k == null || pcm48k.length == 0) {
         return new byte[0];
      }

      int targetSamples = pcm48k.length / 3;
      if (targetSamples <= 0) {
         return new byte[0];
      }

      ByteBuffer buffer = ByteBuffer.allocate(targetSamples * 2).order(ByteOrder.LITTLE_ENDIAN);
      for (int i = 0; i < targetSamples; i++) {
         buffer.putShort(pcm48k[i * 3]);
      }
      return buffer.array();
   }

   private static boolean isMostlySilent(short[] pcm) {
      if (pcm == null || pcm.length == 0) {
         return true;
      }

      double sumSquares = 0.0D;
      for (short sample : pcm) {
         sumSquares += (double) sample * (double) sample;
      }

      double rms = Math.sqrt(sumSquares / (double) pcm.length);
      return rms < SILENCE_RMS_THRESHOLD;
   }
}


