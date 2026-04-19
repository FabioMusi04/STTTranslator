package com.ilmusi.stttranslator.chat;

import com.ilmusi.stttranslator.STTTranslator;
import com.ilmusi.stttranslator.config.ModConfig;
import com.ilmusi.stttranslator.network.TranslationPayload;
import com.ilmusi.stttranslator.translation.TranslationManager;
import java.util.Iterator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class ChatTranslationHandler {
   private final ModConfig config;

   public ChatTranslationHandler(ModConfig config) {
      this.config = config;
   }

   @SubscribeEvent
   public void onServerChat(ServerChatEvent event) {
      ServerPlayer sender = event.getPlayer();
      String message = event.getMessage().getString();
      if (this.config.isTranslateChat()) {
         String sourceLang = this.config.getSourceLanguage();
         String targetLang = this.config.getTargetLanguage();
         String senderName = sender.getName().getString();
         String senderUUID = sender.getUUID().toString();
         TranslationManager.translate(message, sourceLang, targetLang).thenAccept((translated) -> {
            sender.getServer().execute(() -> {
               String providerTag = this.config.getTranslationProvider() == TranslationManager.Provider.GOOGLE ? "GT" : "AI";
               Component translatedComponent = Component.literal("\u00A7a[" + targetLang.toUpperCase() + "/" + providerTag + "] \u00A7f" + senderName + "\u00A7a: " + translated);
               Iterator var10 = sender.getServer().getPlayerList().getPlayers().iterator();

               while(var10.hasNext()) {
                  ServerPlayer player = (ServerPlayer)var10.next();
                  player.sendSystemMessage(translatedComponent);

                  try {
                     PacketDistributor.sendToPlayer(player, new TranslationPayload(senderUUID, senderName, message, translated, sourceLang, targetLang));
                  } catch (Exception var13) {
                     STTTranslator.LOGGER.debug("Could not send bubble to {}", player.getName().getString());
                  }
               }

               STTTranslator.LOGGER.info("[{}] {}: {} -> {}", new Object[]{providerTag, senderName, message, translated});
            });
         }).exceptionally((throwable) -> {
            STTTranslator.LOGGER.error("Translation failed", throwable);
            return null;
         });
      }
   }
}


