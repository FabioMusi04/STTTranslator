package com.ilmusi.stttranslator.gui;

import com.ilmusi.stttranslator.STTTranslator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ThemedButton extends Button {
   public ThemedButton(int x, int y, int w, int h, Component msg, OnPress onPress) {
      super(x, y, w, h, msg, onPress, DEFAULT_NARRATION);
   }

   protected void renderWidget(GuiGraphics g, int mx, int my, float pt) {
      boolean dm = STTTranslator.getInstance().getConfig().isDarkMenu();
      boolean hovered = this.isHoveredOrFocused();
      int bgColor;
      int borderColor;
      int textColor;
      if (!this.active) {
         bgColor = dm ? -15066598 : -12961222;
         borderColor = dm ? -13421773 : -11908534;
         textColor = 6710886;
      } else if (hovered) {
         bgColor = dm ? -14013894 : -11184800;
         borderColor = dm ? -12264124 : -12264124;
         textColor = 16777215;
      } else {
         bgColor = dm ? -14803416 : -12566456;
         borderColor = dm ? -14013894 : -11184800;
         textColor = dm ? 14540253 : 13421772;
      }

      int x = this.getX();
      int y = this.getY();
      int w = this.getWidth();
      int h = this.getHeight();
      g.fill(x, y, x + w, y + h, borderColor);
      g.fill(x + 1, y + 1, x + w - 1, y + h - 1, bgColor);
      if (hovered && this.active) {
         g.fill(x + 1, y, x + w - 1, y + 1, -12264124);
      }

      Minecraft mc = Minecraft.getInstance();
      int textX = x + w / 2;
      int textY = y + (h - 8) / 2;
      g.drawCenteredString(mc.font, this.getMessage(), textX, textY, textColor);
   }

   public static ThemedButton create(int x, int y, int w, int h, Component msg, OnPress onPress) {
      return new ThemedButton(x, y, w, h, msg, onPress);
   }
}


