package com.ilmusi.stttranslator.gui;

import com.ilmusi.stttranslator.STTTranslator;
import com.ilmusi.stttranslator.config.ModConfig;
import com.ilmusi.stttranslator.render.SpeechBubbleRenderer;
import com.ilmusi.stttranslator.translation.TranslationManager;
import com.ilmusi.stttranslator.voice.ModelDownloader;
import com.ilmusi.stttranslator.voice.STTVoicechatPlugin;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TranslatorConfigScreen extends Screen {
   private final Screen parentScreen;
   private final ModConfig config;
   private EditBox apiKeyField;
   private String selectedSourceLang;
   private String selectedTargetLang;
   private TranslationManager.Provider selectedProvider;
   private String statusMessage = "";
   private int statusColor = 16777215;
   private long statusTime = 0L;
   private boolean isDownloading = false;
   private float bubbleSize;
   private int bubbleDuration;
   private int scrollOffset = 0;
   private int maxScroll = 0;
   private int contentBottom = 0;
   private final List<AbstractWidget> scrollWidgets = new ArrayList();
   private final Map<AbstractWidget, Integer> baseWidgetY = new IdentityHashMap();
   private static final int PW = 260;
   private static final int BH = 18;
   private static final int SCROLL_STEP = 18;
   private static final int CONTENT_TOP = 56;

   public TranslatorConfigScreen(Screen parent) {
      super(Component.literal("STTTranslator"));
      this.parentScreen = parent;
      this.config = STTTranslator.getInstance().getConfig();
      SpeechBubbleRenderer.setPanelPosition(this.config.getPanelPosition());
      this.selectedProvider = this.config.getTranslationProvider();
      this.selectedSourceLang = this.config.getSourceLanguage();
      this.selectedTargetLang = this.config.getTargetLanguage();
      this.bubbleSize = this.config.getBubbleScale();
      this.bubbleDuration = this.config.getBubbleDurationTicks();
   }

   protected void init() {
      super.init();
      this.scrollWidgets.clear();
      this.baseWidgetY.clear();
      int cx = this.width / 2;
      int left = cx - 130;
      int right = cx + 130;
      int half = 127;
      int y = CONTENT_TOP;
      this.addTrackedWidget(ThemedButton.create(left, y, 260, 18, Component.literal(this.provLabel()), (btn) -> {
         this.cycleProvider();
         this.persistSettings();
         this.clearWidgets();
         this.init();
      }), y);
      if (this.selectedProvider != TranslationManager.Provider.DEEPL && this.selectedProvider != TranslationManager.Provider.CLAUDE) {
         this.apiKeyField = null;
      } else {
         y += 24;
         this.apiKeyField = new EditBox(this.font, left, y, 260, 18, Component.literal("Key"));
         String key = this.selectedProvider == TranslationManager.Provider.DEEPL ? this.config.getDeeplApiKey() : this.config.getClaudeApiKey();
         if (key != null && !key.isEmpty()) {
            this.apiKeyField.setValue(key);
         }

         this.apiKeyField.setMaxLength(200);
         this.apiKeyField.setHint(Component.literal(this.selectedProvider == TranslationManager.Provider.DEEPL ? "DeepL API Key..." : "sk-ant-..."));
         this.addTrackedWidget(this.apiKeyField, y);
      }

      y += 44;
      this.addTrackedWidget(ThemedButton.create(left, y, half, 20, Component.literal(this.langLabel(this.selectedSourceLang)), (btn) -> {
         this.minecraft.setScreen(new LanguageSelectionScreen(this, "Source Language", this.selectedSourceLang, (c) -> {
            this.selectedSourceLang = c;
            this.persistSettings();
         }));
      }), y);
      this.addTrackedWidget(ThemedButton.create(left + half + 6, y, half, 20, Component.literal(this.langLabel(this.selectedTargetLang)), (btn) -> {
         this.minecraft.setScreen(new LanguageSelectionScreen(this, "Target Language", this.selectedTargetLang, (c) -> {
            this.selectedTargetLang = c;
            this.persistSettings();
         }));
      }), y);
      y += 34;
      this.addTrackedWidget(ThemedButton.create(left, y, 260, 18, Component.literal(this.tog("Chat Translation", this.config.isTranslateChat())), (btn) -> {
         this.config.setTranslateChat(!this.config.isTranslateChat());
         btn.setMessage(Component.literal(this.tog("Chat Translation", this.config.isTranslateChat())));
      }), y);
      y += 22;
      this.addTrackedWidget(ThemedButton.create(left, y, half, 18, Component.literal(this.tog("Show Original Text", this.config.isShowOriginal())), (btn) -> {
         this.config.setShowOriginal(!this.config.isShowOriginal());
         btn.setMessage(Component.literal(this.tog("Show Original Text", this.config.isShowOriginal())));
      }), y);
      this.addTrackedWidget(ThemedButton.create(left + half + 6, y, half, 18, Component.literal(this.panelThemeLabel()), (btn) -> {
         this.config.setDarkPanel(!this.config.isDarkPanel());
         btn.setMessage(Component.literal(this.panelThemeLabel()));
      }), y);
      y += 22;
      this.addTrackedWidget(ThemedButton.create(left, y, 260, 18, Component.literal(this.panelLabel()), (btn) -> {
         SpeechBubbleRenderer.setPanelPosition((SpeechBubbleRenderer.getPanelPosition() + 1) % 4);
         this.config.setPanelPosition(SpeechBubbleRenderer.getPanelPosition());
         btn.setMessage(Component.literal(this.panelLabel()));
      }), y);
      y += 34;
      this.addTrackedWidget(ThemedButton.create(left, y, half, 18, Component.literal(this.tog("Overhead Text", this.config.isShowBubbles())), (btn) -> {
         this.config.setShowBubbles(!this.config.isShowBubbles());
         btn.setMessage(Component.literal(this.tog("Overhead Text", this.config.isShowBubbles())));
      }), y);
      this.addTrackedWidget(ThemedButton.create(left + half + 6, y, half, 18, Component.literal(this.bubbleThemeLabel()), (btn) -> {
         this.config.setDarkBubbles(!this.config.isDarkBubbles());
         btn.setMessage(Component.literal(this.bubbleThemeLabel()));
      }), y);
      y += 22;
      this.addTrackedWidget(ThemedButton.create(left, y, half, 18, Component.literal(this.sizeLabel()), (btn) -> {
         this.bubbleSize += 0.25F;
         if (this.bubbleSize > 2.0F) {
            this.bubbleSize = 0.5F;
         }

         this.persistSettings();
         btn.setMessage(Component.literal(this.sizeLabel()));
      }), y);
      this.addTrackedWidget(ThemedButton.create(left + half + 6, y, half, 18, Component.literal(this.durLabel()), (btn) -> {
         this.bubbleDuration += 40;
         if (this.bubbleDuration > 400) {
            this.bubbleDuration = 80;
         }

         this.persistSettings();
         btn.setMessage(Component.literal(this.durLabel()));
      }), y);
      y += 34;
      boolean modelDL = ModelDownloader.isModelDownloaded(this.selectedSourceLang);
      boolean modelAv = ModelDownloader.hasModelAvailable(this.selectedSourceLang);
      String dlLabel;
      if (this.isDownloading) {
         dlLabel = "\u00A7e\u27F3 Downloading...";
      } else if (modelDL) {
         dlLabel = "\u00A7a\u2714 Voice model ready (" + this.selectedSourceLang.toUpperCase() + ")";
      } else if (modelAv) {
         dlLabel = "\u00A7e\u2B07 Download Voice Model";
      } else {
         dlLabel = "\u00A7c\u2718 No model available for " + this.selectedSourceLang.toUpperCase();
      }

      this.addTrackedWidget(ThemedButton.create(left, (y + 50), 260, 18, Component.literal(dlLabel), (btn) -> {
         if (!modelDL && !this.isDownloading && modelAv) {
            this.downloadVoiceModel();
         }

      }), y);
      y += 26;
      this.addTrackedWidget(ThemedButton.create(left, y, 260, 18, Component.literal("\u00A7a\u2714 Save & Close"), (btn) -> {
         this.saveAndClose();
      }), y);
      // DOPO tutti gli altri addTrackedWidget
      this.addRenderableWidget(ThemedButton.create(
         this.width / 2 + 130,
         35,
         12,
         12,
         Component.literal(this.config.isDarkMenu() ? "\u00A7e\u2726" : "\u00A7f\u263C"),
         (btn) -> {
            this.config.setDarkMenu(!this.config.isDarkMenu());
            btn.setMessage(Component.literal(this.config.isDarkMenu() ? "\u00A7e\u2726" : "\u00A7f\u263C"));
         }
      ));
      this.contentBottom = y + 30;
      this.recalculateScrollBounds();
      this.applyScroll();
   }

   public void render(GuiGraphics g, int mx, int my, float pt) {
      int cx = this.width / 2;
      int left = cx - 130;
      int right = cx + 130;
      int half = 127;
this.renderBackground(g, mx, my, pt);this.renderBlurredBackground(pt);      
g.fill(left - 16, 2, right + 16, 3, -12264124);
      g.drawCenteredString(this.font, "\u00A7lSTTTranslator", cx, 8, 4513092);
      g.drawCenteredString(this.font, "v1.4 by IlMusi", cx, 20, 5592405);
      g.enableScissor(left - 16, 30, right + 16, this.height - 14);
      super.render(g, mx, my, pt);
      int y = CONTENT_TOP - this.scrollOffset;
      this.sec(g, left, right, y - 20, "TRANSLATOR");
      int providerRowY = y;
      int keyRowY = providerRowY + 24;
      int languageRowY = providerRowY + 34 + (this.selectedProvider == TranslationManager.Provider.DEEPL || this.selectedProvider == TranslationManager.Provider.CLAUDE ? 24 : 0);
      int translationPanelRowY = languageRowY + 34;
      int overheadRowY = translationPanelRowY + 22 + 22 + 34;
      int sizeRowY = overheadRowY + 22;
      int downloadRowY = overheadRowY + 22 + 34;
      this.sec(g, left, right, languageRowY - 13, "LANGUAGES");
      g.drawString(this.font, "I speak:", left, languageRowY, 6710886);
      g.drawString(this.font, "Translate to:", left + half + 6, languageRowY, 6710886);
      g.drawCenteredString(this.font, "\u00A7a\u27A1", cx, languageRowY + 15, 4513092);
      this.sec(g, left, right, translationPanelRowY - 3, "TRANSLATION PANELS");
      this.sec(g, left, right, overheadRowY - 3, "TEXT OVERHEAD");
      this.sec(g, left, right, downloadRowY - 3, "VOICE");
      g.disableScissor();
      if (!this.statusMessage.isEmpty() && System.currentTimeMillis() - this.statusTime < 6000L) {
         int sw = Math.min(this.font.width(this.statusMessage) + 12, this.width - 20);
         int sy = this.height - 38;
         g.fill(cx - sw / 2, sy - 2, cx + sw / 2, sy + 10, -587202560);
         g.drawCenteredString(this.font, this.statusMessage, cx, sy, this.statusColor);
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      if (this.maxScroll > 0 && scrollY != 0.0) {
         this.scrollOffset = this.clamp(this.scrollOffset - (int)(scrollY * 18.0), 0, this.maxScroll);
         this.applyScroll();
         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
      }
   }

   private void sec(GuiGraphics g, int l, int r, int y, String t) {
      int w = this.font.width(t) + 6;
      int c = (l + r) / 2;
      g.fill(l, y + 8, c - w / 2, y + 9, 872415231);
      g.fill(c + w / 2, y + 8, r, y + 9, 872415231);
      g.drawCenteredString(this.font, t, c, y + 2, 5592405);
   }

   private <T extends AbstractWidget> T addTrackedWidget(T widget, int y) {
      this.scrollWidgets.add(widget);
      this.baseWidgetY.put(widget, y);
      return this.addRenderableWidget(widget);
   }

   private void recalculateScrollBounds() {
      int visibleBottom = this.height - 16;
      this.maxScroll = Math.max(0, this.contentBottom - visibleBottom);
      this.scrollOffset = this.clamp(this.scrollOffset, 0, this.maxScroll);
   }

   private void applyScroll() {
      Iterator var1 = this.scrollWidgets.iterator();

      while(var1.hasNext()) {
         AbstractWidget w = (AbstractWidget)var1.next();
         Integer baseY = (Integer)this.baseWidgetY.get(w);
         if (baseY != null) {
            w.setY(baseY - this.scrollOffset);
         }
      }

   }

   private int clamp(int value, int min, int max) {
      if (value < min) {
         return min;
      } else {
         return Math.min(value, max);
      }
   }

   private void persistSettings() {
      String previousSource = this.config.getSourceLanguage();
      String previousTarget = this.config.getTargetLanguage();
      this.config.setTranslationProvider(this.selectedProvider);
      this.config.setSourceLanguage(this.selectedSourceLang);
      this.config.setTargetLanguage(this.selectedTargetLang);
      this.config.setBubbleSourceLanguage(this.selectedSourceLang);
      this.config.setBubbleTargetLanguage(this.selectedTargetLang);
      this.config.setBubbleScale(this.bubbleSize);
      this.config.setBubbleDurationTicks(this.bubbleDuration);
      if (this.apiKeyField != null) {
         String key = this.apiKeyField.getValue().trim();
         if (this.selectedProvider == TranslationManager.Provider.DEEPL) {
            this.config.setDeeplApiKey(key);
         } else if (this.selectedProvider == TranslationManager.Provider.CLAUDE) {
            this.config.setClaudeApiKey(key);
         }
      }

      if (!previousSource.equals(this.selectedSourceLang) || !previousTarget.equals(this.selectedTargetLang)) {
         TranslationManager.clearAllCaches();
         STTVoicechatPlugin.resetClientSpeechState(true);
         SpeechBubbleRenderer.clearAll();
      }

   }

   private void cycleProvider() {
      TranslationManager.Provider var10001;
      switch(this.selectedProvider) {
      case LIBRE:
         var10001 = TranslationManager.Provider.GOOGLE;
         break;
      case GOOGLE:
         var10001 = TranslationManager.Provider.DEEPL;
         break;
      case DEEPL:
         var10001 = TranslationManager.Provider.CLAUDE;
         break;
      case CLAUDE:
         var10001 = TranslationManager.Provider.LIBRE;
         break;
      default:
         throw new IllegalStateException("Unexpected provider: " + this.selectedProvider);
      }

      this.selectedProvider = var10001;
   }

   private String provLabel() {
      String var10000;
      switch(this.selectedProvider) {
      case LIBRE:
         var10000 = "\u00A7a\u25CF LibreTranslate AI (Free)";
         break;
      case GOOGLE:
         var10000 = "\u00A7e\u25CF Google Translate (Free)";
         break;
      case DEEPL:
         var10000 = "\u00A7b\u25CF DeepL (API Key)";
         break;
      case CLAUDE:
         var10000 = "\u00A7d\u25CF Claude AI (Premium)";
         break;
      default:
         throw new IllegalStateException("Unexpected provider: " + this.selectedProvider);
      }

      return var10000;
   }

   private String langLabel(String c) {
      Iterator var2 = ModConfig.SUPPORTED_LANGUAGES.iterator();

      ModConfig.LanguageEntry l;
      do {
         if (!var2.hasNext()) {
            return c;
         }

         l = (ModConfig.LanguageEntry)var2.next();
      } while(!l.code.equals(c));

      return l.flag + " " + l.name;
   }

   private String tog(String n, boolean v) {
      return (v ? "\u00A7a\u25CF " : "\u00A7c\u25CB ") + "\u00A7f" + n;
   }

   private String bubbleThemeLabel() {
      return this.tog("Dark Overhead Text", this.config.isDarkBubbles());
   }

   private String panelThemeLabel() {
      return this.tog("Dark Panel", this.config.isDarkPanel());
   }

   private String panelLabel() {
      String var10000;
      switch(SpeechBubbleRenderer.getPanelPosition()) {
      case 1:
         var10000 = "\u2197 Top Right";
         break;
      case 2:
         var10000 = "\u2199 Mid Left";
         break;
      case 3:
         var10000 = "\u2198 Mid Right";
         break;
      default:
         var10000 = "\u2196 Top Left";
      }

      return "\u00A7fPanel Position: \u00A7a" + var10000;
   }

   private String sizeLabel() {
      Object[] var10001 = new Object[]{this.bubbleSize};
      return "\u00A7fSize \u00A7a" + String.format("%.1f", var10001) + "x";
   }

   private String durLabel() {
      return "\u00A7fDuration \u00A7a" + this.bubbleDuration / 20 + "s";
   }

   private void setStatus(String m, int c) {
      this.statusMessage = m;
      this.statusColor = c;
      this.statusTime = System.currentTimeMillis();
   }

   private void downloadVoiceModel() {
      this.isDownloading = true;
      this.setStatus("\u00A7e\u27F3 Downloading...", 16777045);
      ModelDownloader.downloadModelAsync(this.selectedSourceLang, (msg, pct) -> {
         Minecraft.getInstance().execute(() -> {
            this.setStatus("\u00A7e" + msg, 16777045);
         });
      }).thenAccept((p) -> {
         Minecraft.getInstance().execute(() -> {
            this.isDownloading = false;
            this.setStatus("\u00A7a\u2714 Model downloaded!", 5635925);
            this.clearWidgets();
            this.init();
         });
      }).exceptionally((e) -> {
         Minecraft.getInstance().execute(() -> {
            this.isDownloading = false;
            this.setStatus("\u00A7c\u2718 Error: " + e.getMessage(), 16733525);
            this.clearWidgets();
            this.init();
         });
         return null;
      });
   }

   private void testTranslation() {
      this.config.setTranslationProvider(this.selectedProvider);
      this.config.setSourceLanguage(this.selectedSourceLang);
      this.config.setTargetLanguage(this.selectedTargetLang);
      if (this.apiKeyField != null && !this.apiKeyField.getValue().trim().isEmpty()) {
         if (this.selectedProvider == TranslationManager.Provider.DEEPL) {
            this.config.setDeeplApiKey(this.apiKeyField.getValue().trim());
         } else if (this.selectedProvider == TranslationManager.Provider.CLAUDE) {
            this.config.setClaudeApiKey(this.apiKeyField.getValue().trim());
         }
      }

      if ((this.selectedProvider == TranslationManager.Provider.DEEPL || this.selectedProvider == TranslationManager.Provider.CLAUDE) && (this.apiKeyField == null || this.apiKeyField.getValue().trim().isEmpty())) {
         this.setStatus("\u00A7c\u2718 API key required", 16733525);
      } else {
         this.setStatus("\u00A7e\u27F3 Translating...", 16777045);
         String var2 = this.selectedSourceLang;
         byte var3 = -1;
         switch(var2.hashCode()) {
         case 3246:
            if (var2.equals("es")) {
               var3 = 0;
            }
            break;
         case 3276:
            if (var2.equals("fr")) {
               var3 = 2;
            }
            break;
         case 3588:
            if (var2.equals("pt")) {
               var3 = 1;
            }
         }

         String var10000;
         switch(var3) {
         case 0:
            var10000 = "Hola, bienvenidos al servidor!";
            break;
         case 1:
            var10000 = "Ola, bem-vindos ao servidor!";
            break;
         case 2:
            var10000 = "Bonjour, bienvenue sur le serveur!";
            break;
         default:
            var10000 = "Hello, welcome to the server!";
         }

         String testMsg = var10000;
         TranslationManager.translate(testMsg, this.selectedSourceLang, this.selectedTargetLang).thenAccept((r) -> {
            Minecraft.getInstance().execute(() -> {
               if (r != null && !r.isEmpty()) {
                  if (r.startsWith("[")) {
                     this.setStatus("\u00A7c\u2718 " + r, 16733525);
                  } else {
                     this.setStatus("\u00A7a\u2714 " + r, 5635925);
                  }
               } else {
                  this.setStatus("\u00A7c\u2718 Translation failed", 16733525);
               }

            });
         }).exceptionally((e) -> {
            Minecraft.getInstance().execute(() -> {
               this.setStatus("\u00A7c\u2718 Connection error", 16733525);
            });
            return null;
         });
      }
   }

   private void saveAndClose() {
      this.persistSettings();
      this.minecraft.setScreen(this.parentScreen);
   }

   public void onClose() {
      this.persistSettings();
      this.minecraft.setScreen(this.parentScreen);
   }

   public boolean isPauseScreen() {
      return true;
   }
}


