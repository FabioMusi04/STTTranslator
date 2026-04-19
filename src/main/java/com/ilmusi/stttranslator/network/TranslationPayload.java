package com.ilmusi.stttranslator.network;

import com.ilmusi.stttranslator.STTTranslator;
import com.ilmusi.stttranslator.render.SpeechBubbleRenderer;
import io.netty.buffer.ByteBuf;
import java.util.Iterator;
import java.util.UUID;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TranslationPayload(String senderUUID, String senderName, String originalText, String translatedText, String fromLang, String toLang) implements CustomPacketPayload {
   public static final Type<TranslationPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("stttranslator", "translation"));
   public static final StreamCodec<ByteBuf, TranslationPayload> CODEC;

   public TranslationPayload(String senderUUID, String senderName, String originalText, String translatedText, String fromLang, String toLang) {
      this.senderUUID = senderUUID;
      this.senderName = senderName;
      this.originalText = originalText;
      this.translatedText = translatedText;
      this.fromLang = fromLang;
      this.toLang = toLang;
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(TranslationPayload payload, IPayloadContext context) {
      context.enqueueWork(() -> {
         try {
            if (context.flow().isServerbound()) {
               ServerPlayer sender = (ServerPlayer)context.player();
               if (sender == null || sender.getServer() == null) {
                  return;
               }

               Iterator var3 = sender.getServer().getPlayerList().getPlayers().iterator();

               while(var3.hasNext()) {
                  ServerPlayer player = (ServerPlayer)var3.next();

                  try {
                     PacketDistributor.sendToPlayer(player, payload);
                  } catch (Exception var6) {
                  }
               }
            } else {
               UUID uuid = UUID.fromString(payload.senderUUID());
               SpeechBubbleRenderer.addBubble(uuid, payload.senderName(), payload.originalText(), payload.translatedText(), payload.fromLang(), payload.toLang());
            }
         } catch (Exception var7) {
            STTTranslator.LOGGER.error("Error handling translation", var7);
         }

      });
   }

   public String senderUUID() {
      return this.senderUUID;
   }

   public String senderName() {
      return this.senderName;
   }

   public String originalText() {
      return this.originalText;
   }

   public String translatedText() {
      return this.translatedText;
   }

   public String fromLang() {
      return this.fromLang;
   }

   public String toLang() {
      return this.toLang;
   }

   static {
      CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, TranslationPayload::senderUUID, ByteBufCodecs.STRING_UTF8, TranslationPayload::senderName, ByteBufCodecs.STRING_UTF8, TranslationPayload::originalText, ByteBufCodecs.STRING_UTF8, TranslationPayload::translatedText, ByteBufCodecs.STRING_UTF8, TranslationPayload::fromLang, ByteBufCodecs.STRING_UTF8, TranslationPayload::toLang, TranslationPayload::new);
   }
}


