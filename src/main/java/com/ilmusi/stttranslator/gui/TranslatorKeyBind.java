package com.ilmusi.stttranslator.gui;

import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;

public class TranslatorKeyBind {
   public static final KeyMapping OPEN_TRANSLATOR;

   public static void register(RegisterKeyMappingsEvent event) {
      event.register(OPEN_TRANSLATOR);
   }

   @SubscribeEvent
   public void onClientTick(Post event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         while(OPEN_TRANSLATOR.consumeClick()) {
            if (mc.screen == null) {
               mc.setScreen(new TranslatorConfigScreen((Screen)null));
            }
         }

      }
   }

   static {
      OPEN_TRANSLATOR = new KeyMapping("key.stttranslator.open_menu", Type.KEYSYM, 89, "key.categories.stttranslator");
   }
}


