package com.ilmusi.stttranslator.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent.Init.Post;

public class MenuButtonHandler {
   private static final ResourceLocation LOGO = ResourceLocation.fromNamespaceAndPath("stttranslator", "textures/logo.png");

   @SubscribeEvent
   public void onScreenInit(Post event) {
      int sw;
      int sh;
      if (event.getScreen() instanceof PauseScreen) {
         sw = 4;
         sh = 4;
         Button btn = Button.builder(Component.literal("\u00A7aSTTTranslator"), (b) -> {
            Minecraft.getInstance().setScreen(new TranslatorConfigScreen(event.getScreen()));
         }).pos(sw, sh).size(100, 18).build();
         event.addListener(btn);
      }

      if (event.getScreen() instanceof TitleScreen) {
         sw = event.getScreen().width;
         sh = event.getScreen().height;
         int x = 4;
         int y = 24;
         Button btn = Button.builder(Component.literal("\u00A7aSTTTranslator"), (b) -> {
            Minecraft.getInstance().setScreen(new TranslatorConfigScreen(event.getScreen()));
         }).pos(x, y).size(100, 18).build();
         event.addListener(btn);
      }

   }

   @SubscribeEvent
   public void onScreenRender(net.neoforged.neoforge.client.event.ScreenEvent.Render.Post event) {
      if (event.getScreen() instanceof TitleScreen) {
         GuiGraphics g = event.getGuiGraphics();
         int logoX = 4;
         int logoY = 4;
         g.fill(logoX, logoY, logoX + 100, logoY + 18, -2013265920);
         g.fill(logoX, logoY, logoX + 100, logoY + 1, -12264124);

         try {
            g.blit(LOGO, logoX + 1, logoY + 1, 0.0F, 0.0F, 16, 16, 64, 64);
         } catch (Exception var7) {
         }

         g.drawString(Minecraft.getInstance().font, "\u00A7a\u00A7lSTTTranslator", logoX + 19, logoY + 2, 4513092, false);
         g.drawString(Minecraft.getInstance().font, "\u00A78by IlMusi", logoX + 19, logoY + 10, 5592405, false);
      }

   }
}


