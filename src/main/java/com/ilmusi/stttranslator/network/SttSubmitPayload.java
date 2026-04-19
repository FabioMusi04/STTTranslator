package com.ilmusi.stttranslator.network;

import com.ilmusi.stttranslator.STTTranslator;
import com.ilmusi.stttranslator.translation.TranslationManager;
import io.netty.buffer.ByteBuf;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SttSubmitPayload(String originalText, String fromLang, String toLang, long clientSentAtMs) implements CustomPacketPayload {
   public static final Type<SttSubmitPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("stttranslator", "stt_submit"));
   public static final StreamCodec<ByteBuf, SttSubmitPayload> CODEC = StreamCodec.composite(
         ByteBufCodecs.STRING_UTF8, SttSubmitPayload::originalText,
         ByteBufCodecs.STRING_UTF8, SttSubmitPayload::fromLang,
         ByteBufCodecs.STRING_UTF8, SttSubmitPayload::toLang,
      ByteBufCodecs.VAR_LONG, SttSubmitPayload::clientSentAtMs,
         SttSubmitPayload::new
   );
   private static final String FLOW = "[VOICEFLOW]";
   private static final long SUBMIT_COOLDOWN_MS = 250L;
   private static final long DUPLICATE_WINDOW_MS = 6000L;
   private static final String[] JUNK = new String[]{"eh", "uh", "um", "ah", "oh", "hm", "hmm", "mmm", "mm"};
   private static final Map<UUID, Long> LAST_SUBMIT_AT = new ConcurrentHashMap<>();
   private static final ConcurrentMap<UUID, LastSubmission> LAST_SUBMISSION = new ConcurrentHashMap<>();

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(SttSubmitPayload payload, IPayloadContext context) {
      if (!context.flow().isServerbound()) {
         return;
      }

      context.enqueueWork(() -> {
         try {
            ServerPlayer sender = (ServerPlayer) context.player();
            if (sender == null) {
               return;
            }

            String text = payload.originalText() == null ? "" : payload.originalText().trim();
            String fromLang = payload.fromLang() == null ? "" : payload.fromLang().trim();
            String toLang = payload.toLang() == null ? "" : payload.toLang().trim();
            if (isJunk(text) || fromLang.isEmpty() || toLang.isEmpty()) {
               return;
            }

            UUID senderUuid = sender.getUUID();
            long now = System.currentTimeMillis();
            Long last = LAST_SUBMIT_AT.get(senderUuid);
            if (last != null && now - last < SUBMIT_COOLDOWN_MS) {
               return;
            }
            LAST_SUBMIT_AT.put(senderUuid, now);

            LastSubmission previous = LAST_SUBMISSION.get(senderUuid);
            if (previous != null && now - previous.timestampMs < DUPLICATE_WINDOW_MS) {
               if (text.equals(previous.text) || text.startsWith(previous.text)) {
                  return;
               }
            }
            LAST_SUBMISSION.put(senderUuid, new LastSubmission(text, now));

            long translateStartNs = System.nanoTime();
            String senderName = sender.getName().getString();
            long queueLagMs = payload.clientSentAtMs() > 0L ? Math.max(0L, now - payload.clientSentAtMs()) : -1L;
            STTTranslator.LOGGER.info("{} SERVER_RECV player={} from={} to={} queueLagMs={} stt='{}'", FLOW, senderName, fromLang, toLang, queueLagMs, text);

            TranslationManager.translate(text, fromLang, toLang).thenAccept(translated -> {
               MinecraftServer server = sender.getServer();
               if (server == null) {
                  return;
               }

               String translatedText = translated == null || translated.isBlank() ? text : translated;
               long tookMs = (System.nanoTime() - translateStartNs) / 1_000_000L;
               STTTranslator.LOGGER.info("{} SERVER_TRANSLATED player={} tookMs={} translated='{}'", FLOW, senderName, tookMs, translatedText);

               server.execute(() -> {
                  TranslationPayload out = new TranslationPayload(senderUuid.toString(), senderName, text, translatedText, fromLang, toLang);
                  for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                     try {
                        PacketDistributor.sendToPlayer(player, out);
                     } catch (Exception ignored) {
                     }
                  }
               });
            }).exceptionally(err -> {
               STTTranslator.LOGGER.debug("Voice translation failed (server submit)", err);
               return null;
            });
         } catch (Exception e) {
            STTTranslator.LOGGER.error("Error handling STT submit payload", e);
         }
      });
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

   private static class LastSubmission {
      private final String text;
      private final long timestampMs;

      private LastSubmission(String text, long timestampMs) {
         this.text = text;
         this.timestampMs = timestampMs;
      }
   }
}


