package com.ilmusi.stttranslator.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ilmusi.stttranslator.STTTranslator;
import com.ilmusi.stttranslator.config.ModConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent.Post;
import org.joml.Matrix4f;

public class SpeechBubbleRenderer {
   private static final Map<UUID, List<SpeechBubbleRenderer.BubbleEntry>> PLAYER_BUBBLES = new ConcurrentHashMap();
   private static final int MAX_BUBBLES = 4;
   private static final List<SpeechBubbleRenderer.SidePanelEntry> SIDE_PANEL = new CopyOnWriteArrayList();
   private static final int MAX_SIDE = 8;
   private static int panelPosition = 0;
   private static boolean playSounds = true;
   private static boolean translationOnline = true;

   public static void setPanelPosition(int pos) {
      panelPosition = pos;
   }

   public static int getPanelPosition() {
      return panelPosition;
   }

   public static void setPlaySounds(boolean play) {
      playSounds = play;
   }

   public static void setTranslationOnline(boolean online) {
      translationOnline = online;
   }

   public static void clearAll() {
      PLAYER_BUBBLES.clear();
      SIDE_PANEL.clear();
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

   private static void playLanguageSound(String lang) {
      if (playSounds) {
         String var2 = lang.toLowerCase();
         byte var3 = -1;
         switch(var2.hashCode()) {
         case 3121:
            if (var2.equals("ar")) {
               var3 = 10;
            }
            break;
         case 3201:
            if (var2.equals("de")) {
               var3 = 4;
            }
            break;
         case 3241:
            if (var2.equals("en")) {
               var3 = 0;
            }
            break;
         case 3246:
            if (var2.equals("es")) {
               var3 = 1;
            }
            break;
         case 3276:
            if (var2.equals("fr")) {
               var3 = 3;
            }
            break;
         case 3329:
            if (var2.equals("hi")) {
               var3 = 11;
            }
            break;
         case 3371:
            if (var2.equals("it")) {
               var3 = 8;
            }
            break;
         case 3383:
            if (var2.equals("ja")) {
               var3 = 6;
            }
            break;
         case 3428:
            if (var2.equals("ko")) {
               var3 = 5;
            }
            break;
         case 3588:
            if (var2.equals("pt")) {
               var3 = 2;
            }
            break;
         case 3651:
            if (var2.equals("ru")) {
               var3 = 9;
            }
            break;
         case 3886:
            if (var2.equals("zh")) {
               var3 = 7;
            }
         }

         float var10000;
         switch(var3) {
         case 0:
            var10000 = 1.8F;
            break;
         case 1:
            var10000 = 1.6F;
            break;
         case 2:
            var10000 = 1.5F;
            break;
         case 3:
            var10000 = 2.0F;
            break;
         case 4:
            var10000 = 1.3F;
            break;
         case 5:
            var10000 = 1.9F;
            break;
         case 6:
            var10000 = 2.1F;
            break;
         case 7:
            var10000 = 1.7F;
            break;
         case 8:
            var10000 = 1.4F;
            break;
         case 9:
            var10000 = 1.2F;
            break;
         case 10:
            var10000 = 1.1F;
            break;
         case 11:
            var10000 = 1.85F;
            break;
         default:
            var10000 = 1.8F;
         }

         float pitch = var10000;

         try {
            Minecraft.getInstance().execute(() -> {
               Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI((SoundEvent)SoundEvents.NOTE_BLOCK_CHIME.value(), pitch, 0.25F));
            });
         } catch (Exception var4) {
         }

      }
   }

   public static void addBubble(UUID uuid, String name, String original, String translated, String fromLang, String toLang) {
      ModConfig config = STTTranslator.getInstance().getConfig();
      long dur = (long)config.getBubbleDurationTicks() * 50L;
      long now = System.currentTimeMillis();
      if (translated != null && !translated.isEmpty()) {
         List<SpeechBubbleRenderer.BubbleEntry> list = (List)PLAYER_BUBBLES.computeIfAbsent(uuid, (k) -> {
            return new ArrayList();
         });
         list.removeIf((b) -> {
            return b.text.equals(translated);
         });
         list.add(new SpeechBubbleRenderer.BubbleEntry(name, translated, now, dur, toLang));

         while(list.size() > 4) {
            list.remove(0);
         }

         translationOnline = true;
      }

      if (translated != null && !translated.isEmpty() && original != null && !original.isEmpty()) {
         SIDE_PANEL.removeIf((e) -> {
            return e.playerName.equals(name) && e.original.equals(original);
         });
         SIDE_PANEL.add(new SpeechBubbleRenderer.SidePanelEntry(name, original, translated, fromLang, toLang, now, dur));

         while(SIDE_PANEL.size() > 8) {
            SIDE_PANEL.remove(0);
         }

         playLanguageSound(toLang);
      }

   }

   public static void addLocalBubble(UUID uuid, String name, String orig, String trans, String from, String to) {
      addBubble(uuid, name, orig, trans, from, to);
   }

   private static void drawRoundedRect(GuiGraphics g, int x, int y, int x2, int y2, int color, int r) {
      g.fill(x + r, y, x2 - r, y2, color);
      g.fill(x, y + r, x + r, y2 - r, color);
      g.fill(x2 - r, y + r, x2, y2 - r, color);
      if (r >= 1) {
         g.fill(x + 1, y, x + r, y + 1, color);
         g.fill(x2 - r, y, x2 - 1, y + 1, color);
         g.fill(x + 1, y2 - 1, x + r, y2, color);
         g.fill(x2 - r, y2 - 1, x2 - 1, y2, color);
         g.fill(x, y + 1, x + 1, y + r, color);
         g.fill(x2 - 1, y + 1, x2, y + r, color);
         g.fill(x, y2 - r, x + 1, y2 - 1, color);
         g.fill(x2 - 1, y2 - r, x2, y2 - 1, color);
      }

   }

   @SubscribeEvent
   public void onRenderPlayer(Post event) {
      Player player = event.getEntity();
      List<SpeechBubbleRenderer.BubbleEntry> bubbles = (List)PLAYER_BUBBLES.get(player.getUUID());
      if (bubbles != null && !bubbles.isEmpty()) {
         ModConfig config = STTTranslator.getInstance().getConfig();
         if (config.isShowBubbles()) {
            boolean dark = config.isDarkBubbles();
            long now = System.currentTimeMillis();
            bubbles.removeIf((bx) -> {
               return now - bx.startTime > bx.durationMs;
            });
            if (bubbles.isEmpty()) {
               PLAYER_BUBBLES.remove(player.getUUID());
            } else {
               Minecraft mc = Minecraft.getInstance();
               Font font = mc.font;
               PoseStack ps = event.getPoseStack();
               MultiBufferSource buf = event.getMultiBufferSource();
               float scale = config.getBubbleScale() * 0.025F;
               ps.pushPose();
               ps.translate(0.0D, 2.3D, 0.0D);
               ps.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
               ps.scale(scale, -scale, scale);
               float y = 0.0F;

               for(int i = bubbles.size() - 1; i >= 0; --i) {
                  SpeechBubbleRenderer.BubbleEntry b = (SpeechBubbleRenderer.BubbleEntry)bubbles.get(i);
                  long el = now - b.startTime;
                  float a = 1.0F;
                  if (el > b.durationMs - 1500L) {
                     a = 1.0F - (float)(el - (b.durationMs - 1500L)) / 1500.0F;
                  }

                  float slideIn = 1.0F;
                  float yOffset;
                  if (el < 300L) {
                     yOffset = (float)el / 300.0F;
                     slideIn = yOffset * yOffset * (3.0F - 2.0F * yOffset);
                     a = Math.min(a, slideIn);
                  }

                  if (!(a <= 0.02F)) {
                     yOffset = (1.0F - slideIn) * 8.0F;
                     String flag = getFlag(b.lang);
                     String colorCode = dark ? "\u00A7f" : "\u00A70";
                     String displayText = "\u00A7f" + flag + " " + colorCode + b.text;
                     List<String> lines = wrap(font, displayText, 300);

                     for(int li = lines.size() - 1; li >= 0; --li) {
                        String line = (String)lines.get(li);
                        int tw = font.width(line);
                        int bw = tw + 16;
                        float x = (float)(-bw) / 2.0F;
                        Matrix4f mat = ps.last().pose();
                        int ba = (int)(230.0F * a);
                        float fy = y + yOffset;
                        this.drawRect(ps, buf, x + 1.0F, fy - 2.0F, x + (float)bw + 2.0F, fy + 13.0F, (int)(60.0F * a) << 24);
                        int bgColor = dark ? 1052688 : 15790320;
                        this.drawRect(ps, buf, x + 2.0F, fy - 3.0F, x + (float)bw - 2.0F, fy + 12.0F, ba << 24 | bgColor);
                        this.drawRect(ps, buf, x, fy - 1.0F, x + 2.0F, fy + 10.0F, ba << 24 | bgColor);
                        this.drawRect(ps, buf, x + (float)bw - 2.0F, fy - 1.0F, x + (float)bw, fy + 10.0F, ba << 24 | bgColor);
                        int ta = (int)(255.0F * a);
                        font.drawInBatch(line, x + 8.0F, fy, 16777215 | ta << 24, false, mat, buf, DisplayMode.SEE_THROUGH, 0, 15728880);
                        y -= 15.0F;
                     }

                     --y;
                  }
               }

               ps.popPose();
            }
         }
      }
   }

   @SubscribeEvent
   public void onRenderGui(net.neoforged.neoforge.client.event.RenderGuiEvent.Post event) {
      long now = System.currentTimeMillis();
      SIDE_PANEL.removeIf((ex) -> {
         return now - ex.startTime > ex.durationMs;
      });
      if (SIDE_PANEL.isEmpty()) {
         return;
      }

      ModConfig config = STTTranslator.getInstance().getConfig();
      if (!config.isShowBubbles()) {
         return;
      }

      boolean dark = config.isDarkPanel();
      boolean showOriginal = config.isShowOriginal();
      GuiGraphics g = event.getGuiGraphics();
      Font font = Minecraft.getInstance().font;
      int panelW = 220;
      int sw2 = g.guiWidth();
      int sh2 = g.guiHeight();
      int px;
      int py;
      switch(panelPosition) {
      case 1:
         px = sw2 - panelW - 6;
         py = 6;
         break;
      case 2:
         px = 6;
         py = sh2 / 2 - 60;
         break;
      case 3:
         px = sw2 - panelW - 6;
         py = sh2 / 2 - 60;
         break;
      default:
         px = 6;
         py = 6;
      }

      String targetLang = config.getTargetLanguage();
      String sourceLang = config.getSourceLanguage();
      String targetFlag = getFlag(targetLang);
      String sourceFlag = getFlag(sourceLang);
      String speakerIcon = "\u00A7f\u25B6";
      int panelBg = dark ? -1728053248 : -1712394514;
      int headerBg = dark ? -1156509423 : -1143087651;
      int headerTxt = dark ? 14540253 : 3355443;
      int msgTxt = dark ? 16777215 : 1118481;
      String nameClr = dark ? "\u00A7a" : "\u00A72";
      String arrowClr = "\u00A78";
      String txtClr = dark ? "\u00A7f" : "\u00A70";
      int sepClr = dark ? 419430399 : 402653184;
      int entryCount = Math.min(SIDE_PANEL.size(), 4);
      int sectionH = this.renderSidePanelSection(g, font, px, py, panelW, panelBg, headerBg, headerTxt, msgTxt, sepClr, "\u00A7lDababel Translations " + targetLang.toUpperCase(), SIDE_PANEL, entryCount, now, targetFlag, speakerIcon, nameClr, arrowClr, txtClr, true);
      int ey = py + sectionH + 3;
      if (showOriginal && !sourceLang.equals(targetLang)) {
         this.renderSidePanelSection(g, font, px, ey, panelW, panelBg, headerBg, headerTxt, msgTxt, sepClr, "\u00A7lDababel Translations " + sourceLang.toUpperCase(), SIDE_PANEL, entryCount, now, sourceFlag, speakerIcon, nameClr, arrowClr, txtClr, false);
      }
   }

   private void drawRect(PoseStack ps, MultiBufferSource buf, float x1, float y1, float x2, float y2, int col) {
      int a = col >> 24 & 255;
      int r = col >> 16 & 255;
      int g2 = col >> 8 & 255;
      int b = col & 255;
      if (a > 0) {
         Matrix4f m = ps.last().pose();
         VertexConsumer vc = buf.getBuffer(RenderType.debugQuads());
         vc.addVertex(m, x1, y1, 0.0F).setColor(r, g2, b, a).setUv(0.0F, 0.0F).setOverlay(0).setLight(15728880).setNormal(0.0F, 0.0F, 1.0F);
         vc.addVertex(m, x1, y2, 0.0F).setColor(r, g2, b, a).setUv(0.0F, 1.0F).setOverlay(0).setLight(15728880).setNormal(0.0F, 0.0F, 1.0F);
         vc.addVertex(m, x2, y2, 0.0F).setColor(r, g2, b, a).setUv(1.0F, 1.0F).setOverlay(0).setLight(15728880).setNormal(0.0F, 0.0F, 1.0F);
         vc.addVertex(m, x2, y1, 0.0F).setColor(r, g2, b, a).setUv(1.0F, 0.0F).setOverlay(0).setLight(15728880).setNormal(0.0F, 0.0F, 1.0F);
      }
   }

   private int renderSidePanelSection(GuiGraphics g, Font font, int px, int py, int panelW, int panelBg, int headerBg, int headerTxt, int msgTxt, int sepClr, String title, List<SpeechBubbleRenderer.SidePanelEntry> entries, int entryCount, long now, String langFlag, String speakerIcon, String nameClr, String arrowClr, String txtClr, boolean useTranslatedText) {
      List<List<String>> renderedLines = new ArrayList();
      List<Integer> entryHeights = new ArrayList();

      for(int i = 0; i < entryCount; ++i) {
         SpeechBubbleRenderer.SidePanelEntry entry = (SpeechBubbleRenderer.SidePanelEntry)entries.get(i);
         String body = useTranslatedText ? entry.translated : entry.original;
         String prefix = speakerIcon + " " + langFlag + " " + nameClr + entry.playerName + " " + arrowClr + "\u00BB ";
         List<String> lines = buildSidePanelLines(font, prefix, body, txtClr, panelW - 6);
         renderedLines.add(lines);
         entryHeights.add(lines.size() * 10 + 4);
      }

      int sectionH = 13;

      for(int i = 0; i < entryHeights.size(); ++i) {
         sectionH += (Integer)entryHeights.get(i);
         if (i < entryHeights.size() - 1) {
            sectionH += 2;
         }
      }

      sectionH += 3;
      drawRoundedRect(g, px + 1, py + 1, px + panelW + 1, py + sectionH + 1, 855638016, 2);
      drawRoundedRect(g, px, py, px + panelW, py + sectionH, panelBg, 2);
      drawRoundedRect(g, px, py, px + panelW, py + 12, headerBg, 2);
      g.fill(px, py + 10, px + panelW, py + 12, headerBg);
      g.fill(px + 2, py, px + panelW - 2, py + 1, -12264124);
      g.drawString(font, title, px + 3, py + 2, headerTxt, false);

      int my = py + 14;

      for(int i = 0; i < renderedLines.size(); ++i) {
         SpeechBubbleRenderer.SidePanelEntry entry = (SpeechBubbleRenderer.SidePanelEntry)entries.get(i);
         long el = now - entry.startTime;
         float a = 1.0F;
         if (el > entry.durationMs - 1500L) {
            a = 1.0F - (float)(el - (entry.durationMs - 1500L)) / 1500.0F;
         }

         float sp = 1.0F;
         if (el < 400L) {
            float t = (float)el / 400.0F;
            sp = t * t * (3.0F - 2.0F * t);
         }

         int alpha = (int)(a * 255.0F);
         if (alpha > 5) {
            int so = (int)((1.0F - sp) * -20.0F);
            List<String> lines = (List)renderedLines.get(i);
            int blockH = (Integer)entryHeights.get(i);
            g.fill(px + 3, my - 2, px + panelW - 3, my + blockH - 1, (int)(35.0F * a) << 24 | 0x202020);

            for(int li = 0; li < lines.size(); ++li) {
               String line = (String)lines.get(li);
               g.drawString(font, line, px + 3 + so, my + li * 10, alpha << 24 | msgTxt, false);
            }

            my += blockH;
            if (i < renderedLines.size() - 1) {
               g.fill(px + 3, my, px + panelW - 3, my + 1, sepClr);
               my += 2;
            }
         }
      }

      return sectionH;
   }

   private static List<String> buildSidePanelLines(Font font, String prefix, String body, String bodyColor, int maxWidth) {
      List<String> lines = new ArrayList();
      String safeBody = body == null ? "" : body.trim();
      int availableWidth = Math.max(40, maxWidth - font.width(prefix));
      List<String> wrappedBody = wrap(font, safeBody, availableWidth);
      if (wrappedBody.isEmpty()) {
         lines.add(prefix.trim());
         return lines;
      }

      lines.add(prefix + bodyColor + (String)wrappedBody.get(0));

      for(int i = 1; i < wrappedBody.size(); ++i) {
         lines.add(bodyColor + (String)wrappedBody.get(i));
      }

      return lines;
   }

   private static List<String> wrap(Font f, String t, int w) {
      List<String> l = new ArrayList();
      if (t == null || t.isBlank()) {
         return l;
      }

      if (f.width(t) <= w) {
         l.add(t);
         return l;
      } else {
         StringBuilder current = new StringBuilder();
         String[] var5 = t.split(" ");
         int var6 = var5.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            String word = var5[var7];
            if (word.isEmpty()) {
               continue;
            }

            if (f.width(word) > w) {
               if (!current.isEmpty()) {
                  l.add(current.toString());
                  current.setLength(0);
               }

               StringBuilder chunk = new StringBuilder();

               for(int i = 0; i < word.length(); ++i) {
                  char ch = word.charAt(i);
                  String candidate = String.valueOf(chunk) + ch;
                  if (chunk.length() > 0 && f.width(candidate) > w) {
                     l.add(chunk.toString());
                     chunk.setLength(0);
                  }

                  chunk.append(ch);
               }

               if (chunk.length() > 0) {
                  if (current.isEmpty()) {
                     current.append(chunk);
                  } else {
                     current.append(chunk);
                  }
               }

               continue;
            }

            String test = current.isEmpty() ? word : String.valueOf(current) + " " + word;
            if (f.width(test) > w && !current.isEmpty()) {
               l.add(current.toString());
               current = new StringBuilder(word);
            } else {
               if (!current.isEmpty()) {
                  current.append(" ");
               }

               current.append(word);
            }
         }

         if (!current.isEmpty()) {
            l.add(current.toString());
         }

         return l;
      }
   }

   public static class BubbleEntry {
      public final String playerName;
      public final String text;
      public final String lang;
      public final long startTime;
      public final long durationMs;

      public BubbleEntry(String n, String t, long s, long d, String lang) {
         this.playerName = n;
         this.text = t;
         this.startTime = s;
         this.durationMs = d;
         this.lang = lang;
      }
   }

   public static class SidePanelEntry {
      public final String playerName;
      public final String original;
      public final String translated;
      public final String fromLang;
      public final String toLang;
      public final long startTime;
      public final long durationMs;

      public SidePanelEntry(String n, String o, String t, String fl, String tl, long s, long d) {
         this.playerName = n;
         this.original = o;
         this.translated = t;
         this.fromLang = fl;
         this.toLang = tl;
         this.startTime = s;
         this.durationMs = d;
      }
   }
}


