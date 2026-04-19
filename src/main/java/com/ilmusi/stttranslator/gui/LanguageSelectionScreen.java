package com.ilmusi.stttranslator.gui;

import com.ilmusi.stttranslator.config.ModConfig;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class LanguageSelectionScreen extends Screen {
   private final Screen parentScreen;
   private final String screenTitle;
   private final String currentSelection;
   private final Consumer<String> onSelect;
   private final List<ModConfig.LanguageEntry> languages;

   public LanguageSelectionScreen(Screen parent, String title, String currentSelection, Consumer<String> onSelect) {
      super(Component.literal(title));
      this.languages = ModConfig.SUPPORTED_LANGUAGES;
      this.parentScreen = parent;
      this.screenTitle = title;
      this.currentSelection = currentSelection;
      this.onSelect = onSelect;
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

   protected void init() {
      super.init();
      int cols = 2;
      int btnW = 140;
      int btnH = 20;
      int gapX = 8;
      int gapY = 3;
      int totalW = cols * btnW + (cols - 1) * gapX;
      int startX = (this.width - totalW) / 2;
      int startY = 44;

      for(int i = 0; i < this.languages.size(); ++i) {
         ModConfig.LanguageEntry lang = (ModConfig.LanguageEntry)this.languages.get(i);
         int col = i % cols;
         int row = i / cols;
         int x = startX + col * (btnW + gapX);
         int y = startY + row * (btnH + gapY);
         boolean isSelected = lang.code.equals(this.currentSelection);
         String flag = getFlag(lang.code);
         String label;
         if (isSelected) {
            label = "\u00A7a\u25CF " + flag + " " + lang.name;
         } else {
            label = "\u00A7f\u25CB " + flag + " " + lang.name;
         }

         this.addRenderableWidget(ThemedButton.create(x, y, btnW, btnH, Component.literal(label), (btn) -> {
            this.onSelect.accept(lang.code);
            this.minecraft.setScreen(this.parentScreen);
         }));
      }

      this.addRenderableWidget(ThemedButton.create(this.width / 2 - 50, this.height - 30, 100, 18, Component.literal("\u00A77\u2190 Back"), (btn) -> {
         this.minecraft.setScreen(this.parentScreen);
      }));
   }

   public void render(GuiGraphics g, int mx, int my, float pt) {
      int cx = this.width / 2;
      g.fill(0, 0, this.width, this.height, -872415232);
      int panelW = 310;
      int panelL = cx - panelW / 2;
      int panelR = cx + panelW / 2;
      g.fill(panelL - 10, 2, panelR + 10, this.height - 14, -2012147432);
      g.fill(panelL - 10, 2, panelR + 10, 3, -12264124);
      g.drawCenteredString(this.font, "\u00A7l" + this.screenTitle, cx, 10, 4513092);
      g.drawCenteredString(this.font, "Select a language", cx, 24, 5592405);
      g.fill(panelL, 36, panelR, 37, 872415231);
      String currentName = "?";
      Iterator var10 = this.languages.iterator();

      while(var10.hasNext()) {
         ModConfig.LanguageEntry l = (ModConfig.LanguageEntry)var10.next();
         if (l.code.equals(this.currentSelection)) {
            currentName = l.name;
            break;
         }
      }

      g.drawCenteredString(this.font, "Current: \u00A7a" + getFlag(this.currentSelection) + " " + currentName, cx, this.height - 44, 5592405);
      super.render(g, mx, my, pt);
   }

   public void onClose() {
      this.minecraft.setScreen(this.parentScreen);
   }
}


