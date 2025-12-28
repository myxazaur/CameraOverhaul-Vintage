package ua.myxazaur.cameraoverhaul.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.fml.client.config.HoverChecker;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import ua.myxazaur.cameraoverhaul.config.ConfigDefaultsUtil;

import java.util.Arrays;
import java.util.List;

public final class CustomConfigWidgets {

    private CustomConfigWidgets() {}

    private static void drawBorder(int x, int y, int w, int h, int c) {
        Gui.drawRect(x, y, x + w, y + 1, c);
        Gui.drawRect(x, y + h - 1, x + w, y + h, c);
        Gui.drawRect(x, y, x + 1, y + h, c);
        Gui.drawRect(x + w - 1, y, x + w, y + h, c);
    }

    private static void playClick() {
        Minecraft.getMinecraft().getSoundHandler().playSound(
                PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private static void drawResetBtn(FontRenderer fr, int rx, int dy, int h, boolean hovered) {
        int bg = hovered ? 0xFF664444 : 0xFF442222;
        int border = hovered ? 0xFFFF6666 : 0xFFAA4444;
        Gui.drawRect(rx - 1, dy - 1, rx + 13, dy + h + 1, border);
        Gui.drawRect(rx, dy, rx + 12, dy + h, bg);
        fr.drawString("R", rx + 3, dy + (h - 8) / 2, hovered ? 0xFFFFFF : 0xFFAAAA);
    }

    // ==================== ConfigCheckbox ====================

    public static class ConfigCheckbox extends Gui {
        private static final int SIZE = 12;
        private final int x, y;
        private final String label, option;
        private final HoverChecker hover;
        private final HoverChecker resetHover;
        private boolean checked;

        public ConfigCheckbox(int x, int y, String option, boolean initial) {
            this.x = x; this.y = y;
            this.option = option;
            this.label = I18n.format("cameraoverhaul.config." + option);
            this.checked = initial;
            this.hover = new HoverChecker(y, y + SIZE, x, x + SIZE + 100, 800);
            this.resetHover = new HoverChecker(y, y + SIZE, 0, 0, 800);
        }

        public void draw(Minecraft mc, FontRenderer fr, int scroll, int mx, int my) {
            int dy = y - scroll;
            drawRect(x, dy, x + SIZE, dy + SIZE, checked ? 0xFF225522 : 0xFF222222);
            drawBorder(x, dy, SIZE, SIZE, checked ? 0xFF44AA44 : 0xFF666666);

            String text = isModified() ? "§o" + label : label;
            fr.drawString(text, x + SIZE + 6, dy + 2, checked ? 0xFFFFFF : 0xAAAAAA);

            if (isModified()) {
                drawResetBtn(fr, x + SIZE + 8 + fr.getStringWidth(label) + 4, dy, SIZE,
                        isResetHovered(mx, my, scroll));
            }
        }

        public boolean isModified() {
            return checked != ConfigDefaultsUtil.getDefaultBoolean("general", option);
        }

        public boolean isResetHovered(int mx, int my) {
            return isModified() && resetHover.checkHover(mx, my);
        }

        public boolean isResetHovered(int mx, int my, int scroll) {
            if (!isModified()) return false;
            int dy = y - scroll;
            int textWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(label);
            int rx = x + SIZE + 8 + textWidth + 4;
            return mx >= rx && mx <= rx + 12 && my >= dy && my <= dy + SIZE;
        }

        public void updateHoverRegion(int scroll) {
            int textWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(label);
            int totalWidth = SIZE + 6 + textWidth;
            hover.updateBounds(y - scroll, y + SIZE - scroll, x, x + totalWidth);

            int rx = x + SIZE + 8 + textWidth + 4;
            resetHover.updateBounds(y - scroll, y + SIZE - scroll, rx, rx + 12);
        }

        public boolean isHovered(int mx, int my) { return hover.checkHover(mx, my); }

        public List<String> getTooltip() {
            boolean def = ConfigDefaultsUtil.getDefaultBoolean("general", option);

            String modified = isModified()
                    ? " §c" + I18n.format("cameraoverhaul.gui.modified")
                    : "";

            String defStr = def
                    ? I18n.format("cameraoverhaul.gui.enabled")
                    : I18n.format("cameraoverhaul.gui.disabled");

            return Arrays.asList(
                    "§b" + label + modified,
                    "§7" + I18n.format("cameraoverhaul.config." + option + ".desc"),
                    "§8" + I18n.format("cameraoverhaul.gui.default", defStr)
            );
        }

        public List<String> getResetTooltip() {
            boolean def = ConfigDefaultsUtil.getDefaultBoolean("general", option);

            String defStr = def
                    ? I18n.format("cameraoverhaul.gui.enabled")
                    : I18n.format("cameraoverhaul.gui.disabled");

            return Arrays.asList(
                    "§c" + I18n.format("cameraoverhaul.gui.reset_to_default"),
                    "§7" + defStr
            );
        }

        public void mouseClicked(int mx, int my, int scroll) {
            int dy = y - scroll;
            if (isResetHovered(mx, my)) {
                checked = ConfigDefaultsUtil.getDefaultBoolean("general", option);
                playClick();
            } else if (mx >= x && mx <= x + SIZE + 100 && my >= dy && my <= dy + SIZE) {
                checked = !checked;
                playClick();
            }
        }

        public boolean isChecked() { return checked; }
    }

    // ==================== ConfigTextField ====================

    public static class ConfigTextField extends GuiTextField {
        private final String label, category, option;
        private final HoverChecker hover, resetHover;
        public int labelX;
        private boolean valid = true;

        public ConfigTextField(FontRenderer fr, int x, int y, int w, int h, String option, String category) {
            super(0, fr, x, y, w, h);
            this.label = I18n.format("cameraoverhaul.config." + option);
            this.labelX = x - 100;
            this.category = category;
            this.option = option;
            this.hover = new HoverChecker(y, y + h, x - 100, x + w, 800);
            this.resetHover = new HoverChecker(y, y + h, x + w + 4, x + w + 18, 800);
            setMaxStringLength(10);
        }

        public boolean isModified() {
            try {
                double cur = Double.parseDouble(getText().trim());
                double def = ConfigDefaultsUtil.getDefaultDouble(category, option);
                return Math.abs(cur - def) > 0.0001;
            } catch (Exception e) { return !getText().trim().isEmpty(); }
        }

        public void draw(FontRenderer fr, int scroll, int mx, int my) {
            if (!getVisible()) return;
            int dy = y - scroll;

            fr.drawString(isModified() ? "§o" + label : label, labelX, dy + (height - 8) / 2, 0xBBBBBB);

            int border = isFocused() ? 0xFF4488FF : (!valid ? 0xFFFF4444 : 0xFF555555);
            Gui.drawRect(x - 1, dy - 1, x + width + 1, dy + height + 1, border);
            Gui.drawRect(x, dy, x + width, dy + height, valid ? 0xFF1E1E1E : 0xFF3E1E1E);

            int color = valid ? 0xFFFFFF : 0xFFAAAA;
            int tx = x + 4, ty = dy + (height - 8) / 2;

            if (isFocused()) {
                String t = getText();
                int cur = Math.min(getCursorPosition(), t.length());
                fr.drawString(t.substring(0, cur), tx, ty, color);
                int cx = tx + fr.getStringWidth(t.substring(0, cur));
                if ((System.currentTimeMillis() / 500) % 2 == 0)
                    Gui.drawRect(cx, ty - 1, cx + 1, ty + 9, 0xFFFFFFFF);
                fr.drawString(t.substring(cur), cx + 1, ty, color);
            } else {
                fr.drawString(getText(), tx, ty, color);
            }

            if (isModified()) {
                drawResetBtn(fr, x + width + 4, dy, height, isResetHovered(mx, my, scroll));
            }
        }

        private boolean isResetHovered(int mx, int my, int scroll) {
            if (!isModified()) return false;
            int dy = y - scroll;
            return mx >= x + width + 4 && mx <= x + width + 18 && my >= dy && my <= dy + height;
        }

        public boolean handleResetClick(int mx, int my, int scroll) {
            if (isResetHovered(mx, my, scroll)) {
                double def = ConfigDefaultsUtil.getDefaultDouble(category, option);
                setText(CameraConfigGuiScreen.formatDouble(def));
                playClick();
                return true;
            }
            return false;
        }

        public void updateHoverRegion(int scroll) {
            hover.updateBounds(y - scroll, y + height - scroll, labelX, x);
            resetHover.updateBounds(y - scroll, y + height - scroll, x + width + 4, x + width + 18);
        }

        public boolean isHovered(int mx, int my) { return hover.checkHover(mx, my); }
        public boolean isResetHovered(int mx, int my) { return isModified() && resetHover.checkHover(mx, my); }

        public List<String> getTooltip() {
            double def = ConfigDefaultsUtil.getDefaultDouble(category, option);

            String modified = isModified()
                    ? " §6" + I18n.format("cameraoverhaul.gui.modified")
                    : "";

            return Arrays.asList(
                    "§b" + label + modified,
                    "§7" + I18n.format("cameraoverhaul.config." + option + ".desc"),
                    "§8" + I18n.format("cameraoverhaul.gui.default", CameraConfigGuiScreen.formatDouble(def))
            );
        }

        public List<String> getResetTooltip() {
            double def = ConfigDefaultsUtil.getDefaultDouble(category, option);
            return Arrays.asList(
                    "§c" + I18n.format("cameraoverhaul.gui.reset_to_default"),
                    "§7" + CameraConfigGuiScreen.formatDouble(def)
            );
        }
        @Override
        public void writeText(String text) {
            StringBuilder sb = new StringBuilder();
            for (char c : text.toCharArray()) if (Character.isDigit(c) || c == '.' || c == '-') sb.append(c);
            super.writeText(sb.toString());
            validate();
        }

        @Override public void setText(String t) { super.setText(t); validate(); }
        @Override public void deleteFromCursor(int n) { super.deleteFromCursor(n); validate(); }

        private void validate() {
            String t = getText().trim();
            if (t.isEmpty() || t.equals("-") || t.equals(".")) { valid = true; return; }
            try { Double.parseDouble(t); valid = true; }
            catch (NumberFormatException e) { valid = false; }
        }
    }

    // ==================== ContextButton ====================

    public static class ContextButton extends GuiButton {
        private static final int PAUSE = 1500, SCROLL = 2000;
        private boolean selected;
        private long animStart = System.currentTimeMillis();
        private int overflow;

        public ContextButton(int id, int x, int y, int w, int h, String text, boolean selected) {
            super(id, x, y, w, h, text);
            this.selected = selected;
        }

        public void setSelected(boolean s) { selected = s; }
        public void resetAnimation() { animStart = System.currentTimeMillis(); }

        @Override
        public void drawButton(Minecraft mc, int mx, int my, float pt) {
            if (!visible) return;
            GlStateManager.color(1, 1, 1, 1);
            hovered = mx >= x && my >= y && mx < x + width && my < y + height;

            int bg, border, tc;
            if (selected) { bg = 0xFF2266AA; border = 0xFF55AAFF; tc = 0xFFFFFF; }
            else if (hovered) { bg = 0xFF3A3A4A; border = 0xFF6666AA; tc = 0xFFFFAA; }
            else { bg = 0xFF2A2A3A; border = 0xFF444455; tc = 0xAAAAAA; }

            drawRect(x, y, x + width, y + height, bg);
            drawBorder(x, y, width, height, border);
            if (selected) drawRect(x + 2, y + height - 3, x + width - 2, y + height - 1, 0xFF88CCFF);

            int tw = mc.fontRenderer.getStringWidth(displayString);
            int aw = width - 8, ty = y + (height - 8) / 2;

            if (tw <= aw) {
                mc.fontRenderer.drawString(displayString, x + (width - tw) / 2, ty, tc);
            } else {
                overflow = tw - aw;
                ScaledResolution sr = new ScaledResolution(mc);
                int s = sr.getScaleFactor();
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
                GL11.glScissor((x + 4) * s, mc.displayHeight - (y + height) * s, aw * s, height * s);
                mc.fontRenderer.drawString(displayString, x + 4 - calcOffset(), ty, tc);
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
            }
        }

        private int calcOffset() {
            long p = (System.currentTimeMillis() - animStart) % (PAUSE * 2 + SCROLL * 2);
            if (p < PAUSE) return 0;
            if (p < PAUSE + SCROLL) return (int)(ease((float)(p - PAUSE) / SCROLL) * overflow);
            if (p < PAUSE * 2 + SCROLL) return overflow;
            return (int)((1 - ease((float)(p - PAUSE * 2 - SCROLL) / SCROLL)) * overflow);
        }

        private float ease(float t) { return t < 0.5f ? 2*t*t : 1 - (float)Math.pow(-2*t + 2, 2) / 2; }
    }

    // ==================== ScrollablePanel ====================

    public static class ScrollablePanel extends Gui {
        private final Minecraft mc;
        public int x, y, width, height;
        private int content, scroll, maxScroll, header;
        private boolean dragging;

        public ScrollablePanel(Minecraft mc, int x, int y, int w, int h) {
            this.mc = mc; this.x = x; this.y = y; this.width = w; this.height = h;
        }

        public void setHeaderHeight(int h) { header = h; recalc(); }
        public int getScrollableAreaTop() { return y + header; }
        public void setContentHeight(int h) { content = h; recalc(); }

        public void updateBounds(int x, int y, int w, int h) {
            this.x = x; this.y = y; this.width = w; this.height = h; recalc();
        }

        private void recalc() {
            maxScroll = Math.max(0, content - (height - header));
            scroll = Math.min(scroll, maxScroll);
        }

        public int getScrollOffset() { return scroll; }

        public void handleMouseInput() {
            int w = Mouse.getEventDWheel();
            if (Math.abs(w) < 10 && w != 0) w *= 120; // Cleanroom fix

            if (w != 0 && mc.currentScreen != null) {
                int mx = Mouse.getEventX() * mc.currentScreen.width / mc.displayWidth;
                int my = mc.currentScreen.height - Mouse.getEventY() * mc.currentScreen.height / mc.displayHeight - 1;
                if (isMouseOverScrollableArea(mx, my))
                    scroll = Math.max(0, Math.min(maxScroll, scroll - w / 6));
            }
        }

        public boolean isMouseOver(int mx, int my) {
            return mx >= x && mx <= x + width && my >= y && my <= y + height;
        }

        public boolean isMouseOverScrollableArea(int mx, int my) {
            return mx >= x && mx <= x + width && my >= y + header && my <= y + height;
        }

        public void mouseClicked(int mx, int my, int btn) {
            if (maxScroll > 0 && btn == 0) {
                int sx = x + width - 8;
                if (mx >= sx && mx <= sx + 6 && my >= y + header + 2 && my <= y + height - 2) {
                    dragging = true;
                    updateFromMouse(my);
                }
            }
        }

        public void mouseReleased() { dragging = false; }
        public void mouseDragged(int mx, int my) { if (dragging) updateFromMouse(my); }

        private void updateFromMouse(int my) {
            int top = y + header + 2, h = height - header - 4;
            scroll = (int)(Math.max(0, Math.min(1, (float)(my - top - 10) / (h - 20))) * maxScroll);
        }

        public void beginScissor() {
            ScaledResolution sr = new ScaledResolution(mc);
            int s = sr.getScaleFactor();
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor(x * s, mc.displayHeight - (y + height) * s, width * s, (height - header) * s);
        }

        public void endScissor() { GL11.glDisable(GL11.GL_SCISSOR_TEST); }

        public void drawScrollbar() {
            if (maxScroll <= 0) return;
            int sx = x + width - 8, stop = y + header + 2, sh = height - header - 4;
            drawRect(sx, stop, sx + 6, stop + sh, 0x44FFFFFF);
            int th = Math.max(20, (int)((float)(height - header) / content * sh));
            int ty = stop + (int)((float)scroll / maxScroll * (sh - th));
            drawRect(sx, ty, sx + 6, ty + th, dragging ? 0xFFAAAAFF : 0xFF888888);
        }

        public void drawBackground(String title) {
            drawRect(x, y, x + width, y + height, 0x88000000);
            int tx = x + (width - mc.fontRenderer.getStringWidth(title)) / 2;
            mc.fontRenderer.drawString("§b§l" + title, tx, y - 12, 0x55FFFF);
        }

        public void drawBorders() {
            int c = 0xFF555555;
            drawRect(x, y, x + width, y + 1, c);
            drawRect(x, y + height - 1, x + width, y + height, c);
            drawRect(x, y, x + 1, y + height, c);
            drawRect(x + width - 1, y, x + width, y + height, c);
        }
    }

    // ==================== SectionLabel ====================

    public static class SectionLabel extends Gui {
        private final String text;
        private final int x, y;

        public SectionLabel(String text, int x, int y) {
            this.text = text; this.x = x; this.y = y;
        }

        public void draw(FontRenderer fr, int scroll) {
            int dy = y - scroll;
            drawRect(x, dy + 4, x + 8, dy + 5, 0xFF666666);
            fr.drawString("§e" + text, x + 12, dy, 0xFFFF55);
            drawRect(x + 16 + fr.getStringWidth(text), dy + 4, x + 150, dy + 5, 0xFF666666);
        }
    }
}