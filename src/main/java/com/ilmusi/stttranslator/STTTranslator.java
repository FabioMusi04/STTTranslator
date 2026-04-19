package com.ilmusi.stttranslator;

import com.ilmusi.stttranslator.chat.ChatTranslationHandler;
import com.ilmusi.stttranslator.config.ModConfig;
import com.ilmusi.stttranslator.gui.MenuButtonHandler;
import com.ilmusi.stttranslator.gui.TranslatorKeyBind;
import com.ilmusi.stttranslator.gui.WelcomeOverlay;
import com.ilmusi.stttranslator.network.SttSubmitPayload;
import com.ilmusi.stttranslator.network.TranslationPayload;
import com.ilmusi.stttranslator.render.SpeechBubbleRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("stttranslator")
public class STTTranslator {
   public static final String MOD_ID = "stttranslator";
   public static final Logger LOGGER = LoggerFactory.getLogger("STTTranslator");
   private static STTTranslator instance;
   private final ModConfig config;

   public STTTranslator(IEventBus modEventBus, ModContainer modContainer) {
      instance = this;
      LOGGER.info("===========================================");
      LOGGER.info("  STTTranslator v1.4 by IlMusi - Loading...");
      LOGGER.info("===========================================");
      this.config = new ModConfig();
      this.config.load();
      modEventBus.addListener(this::onCommonSetup);
      modEventBus.addListener(this::onRegisterPayloads);
      NeoForge.EVENT_BUS.register(new ChatTranslationHandler(this.config));
      if (FMLEnvironment.dist.isClient()) {
         modEventBus.addListener(this::onClientSetup);
         modEventBus.addListener(this::onRegisterKeyMappings);
         this.registerClientEvents();
      }

      LOGGER.info("Languages: {} -> {}", this.config.getSourceLanguage(), this.config.getTargetLanguage());
   }

   private void registerClientEvents() {
      NeoForge.EVENT_BUS.register(new MenuButtonHandler());
      NeoForge.EVENT_BUS.register(new TranslatorKeyBind());
      NeoForge.EVENT_BUS.register(new WelcomeOverlay());
   }

   private void onCommonSetup(FMLCommonSetupEvent event) {
      LOGGER.info("STTTranslator - Common setup complete");
   }

   private void onClientSetup(FMLClientSetupEvent event) {
      NeoForge.EVENT_BUS.register(new SpeechBubbleRenderer());
      SpeechBubbleRenderer.setPanelPosition(this.config.getPanelPosition());
      LOGGER.info("STTTranslator - Client ready!");
   }

   private void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
      PayloadRegistrar registrar = event.registrar("stttranslator").versioned("1.0");
      registrar.playBidirectional(TranslationPayload.TYPE, TranslationPayload.CODEC, TranslationPayload::handle);
      registrar.playToServer(SttSubmitPayload.TYPE, SttSubmitPayload.CODEC, SttSubmitPayload::handle);
   }

   private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
      TranslatorKeyBind.register(event);
   }

   public static STTTranslator getInstance() {
      return instance;
   }

   public ModConfig getConfig() {
      return this.config;
   }
}


