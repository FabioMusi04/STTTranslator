package com.ilmusi.stttranslator.gui;

import com.ilmusi.stttranslator.STTTranslator;
import com.ilmusi.stttranslator.config.ModConfig;
import com.ilmusi.stttranslator.voice.ModelDownloader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent.Post;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;

public class WelcomeOverlay {
   private static long showStartTime = 0L;
   private static boolean shouldShow = false;
   private static final long DISPLAY_DURATION = 5000L;

   @SubscribeEvent
   public void onPlayerLogin(PlayerLoggedInEvent event) {
      triggerWelcome();
   }

   public static void triggerWelcome() {
      showStartTime = System.currentTimeMillis();
      shouldShow = true;
   }

   @SubscribeEvent
   public void onRenderGui(Post event) {
      if (shouldShow) {
         long elapsed = System.currentTimeMillis() - showStartTime;
         if (elapsed > 5000L) {
            shouldShow = false;
         } else {
            float alpha = 1.0F;
            if (elapsed < 500L) {
               alpha = (float)elapsed / 500.0F;
            }

            if (elapsed > 4000L) {
               alpha = (float)(5000L - elapsed) / 1000.0F;
            }

            if (!(alpha <= 0.01F)) {
               int a = (int)(alpha * 255.0F);
               int bgAlpha = (int)(alpha * 200.0F);
               GuiGraphics g = event.getGuiGraphics();
               Font font = Minecraft.getInstance().font;
               int sw = g.guiWidth();
               int sh = g.guiHeight();
               ModConfig config = STTTranslator.getInstance().getConfig();
               int panelW = 200;
               int panelH = 80;
               int px = (sw - panelW) / 2;
               int py = sh / 4;
               g.fill(px, py, px + panelW, py + panelH, bgAlpha << 24 | 855320);
               g.fill(px, py, px + panelW, py + 2, a << 24 | 5635925);
               g.fill(px, py + panelH - 1, px + panelW, py + panelH, a / 2 << 24 | 5635925);
               g.drawCenteredString(font, "\u00A7a\u00A7lSTTTranslator", px + panelW / 2, py + 8, a << 24 | 5635925);
               g.fill(px + 20, py + 20, px + panelW - 20, py + 21, a / 3 << 24 | 16777215);
               String sourceFlag = getFlag(config.getSourceLanguage());
               String sourceName = ModConfig.getLanguageName(config.getSourceLanguage());
               String targetFlag = getFlag(config.getTargetLanguage());
               String targetName = ModConfig.getLanguageName(config.getTargetLanguage());
               String langLine = sourceFlag + " " + sourceName + " \u00A77\u27A1 " + targetFlag + " \u00A7f" + targetName;
               g.drawCenteredString(font, langLine, px + panelW / 2, py + 28, a << 24 | 16777215);
               String var10000;
               switch(config.getTranslationProvider()) {
               case LIBRE:
                  var10000 = "\u00A7aLibreTranslate IA";
                  break;
               case GOOGLE:
                  var10000 = "\u00A7aGoogle Translate";
                  break;
               case DEEPL:
                  var10000 = "\u00A7bDeepL";
                  break;
               case CLAUDE:
                  var10000 = "\u00A7dClaude AI";
                  break;
               default:
                  throw new IllegalStateException("Unexpected provider: " + config.getTranslationProvider());
               }

               String providerName = var10000;
               g.drawCenteredString(font, "\u00A78Translator: " + providerName, px + panelW / 2, py + 44, a << 24 | 8947848);
               String voiceStatus;
               if (ModelDownloader.isModelDownloaded(config.getSourceLanguage())) {
                  voiceStatus = "\u00A7a\u2714 Voice active";
               } else {
                  voiceStatus = "\u00A7c\u2718 No voice model";
               }

               g.drawCenteredString(font, voiceStatus, px + panelW / 2, py + 56, a << 24 | 11184810);
               g.drawCenteredString(font, "\u00A78Press Y to configure", px + panelW / 2, py + 68, a / 2 << 24 | 5592405);
            }
         }
      }
   }

   private static String getFlag(String lang) {
      String var1 = lang.toLowerCase();
      byte var2 = -1;
      switch(var1.hashCode()) {
      case 3121:
         if (var1.equals("ar")) {
            var2 = 10;
         }
         break;
      case 3201:
         if (var1.equals("de")) {
            var2 = 4;
         }
         break;
      case 3241:
         if (var1.equals("en")) {
            var2 = 0;
         }
         break;
      case 3246:
         if (var1.equals("es")) {
            var2 = 1;
         }
         break;
      case 3276:
         if (var1.equals("fr")) {
            var2 = 3;
         }
         break;
      case 3329:
         if (var1.equals("hi")) {
            var2 = 11;
         }
         break;
      case 3371:
         if (var1.equals("it")) {
            var2 = 8;
         }
         break;
      case 3383:
         if (var1.equals("ja")) {
            var2 = 6;
         }
         break;
      case 3428:
         if (var1.equals("ko")) {
            var2 = 5;
         }
         break;
      case 3518:
         if (var1.equals("nl")) {
            var2 = 15;
         }
         break;
      case 3580:
         if (var1.equals("pl")) {
            var2 = 14;
         }
         break;
      case 3588:
         if (var1.equals("pt")) {
            var2 = 2;
         }
         break;
      case 3651:
         if (var1.equals("ru")) {
            var2 = 9;
         }
         break;
      case 3700:
         if (var1.equals("th")) {
            var2 = 12;
         }
         break;
      case 3710:
         if (var1.equals("tr")) {
            var2 = 13;
         }
         break;
      case 3886:
         if (var1.equals("zh")) {
            var2 = 7;
         }
      }

      String var10000;
      switch(var2) {
      case 0:
         var10000 = "\ue001";
         break;
      case 1:
         var10000 = "\ue002";
         break;
      case 2:
         var10000 = "\ue003";
         break;
      case 3:
         var10000 = "\ue004";
         break;
      case 4:
         var10000 = "\ue005";
         break;
      case 5:
         var10000 = "\ue006";
         break;
      case 6:
         var10000 = "\ue007";
         break;
      case 7:
         var10000 = "\ue008";
         break;
      case 8:
         var10000 = "\ue009";
         break;
      case 9:
         var10000 = "\ue00a";
         break;
      case 10:
         var10000 = "\ue00b";
         break;
      case 11:
         var10000 = "\ue00c";
         break;
      case 12:
         var10000 = "\ue00d";
         break;
      case 13:
         var10000 = "\ue00e";
         break;
      case 14:
         var10000 = "\ue00f";
         break;
      case 15:
         var10000 = "\ue010";
         break;
      default:
         var10000 = "\ue000";
      }

      return var10000;
   }
}


