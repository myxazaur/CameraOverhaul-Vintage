package ua.myxazaur.cameraoverhaul.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;
import ua.myxazaur.cameraoverhaul.Tags;
import ua.myxazaur.cameraoverhaul.config.CameraConfig;

import java.io.IOException;
import java.util.*;

import static ua.myxazaur.cameraoverhaul.client.gui.CustomConfigWidgets.*;

public class CameraConfigGuiScreen extends GuiScreen {

    private final GuiScreen parentScreen;
    private final Map<Context, double[]> tempContextValues = new HashMap<>();

    private enum Context {
        WALKING, SPRINTING, SWIMMING, FLYING, MOUNTS, VEHICLES;

        String getDisplayName() {
            return I18n.format("cameraoverhaul.config.category." + name().toLowerCase());
        }
    }

    private Context currentContext = Context.WALKING;
    private ScrollablePanel leftPanel, rightPanel;

    private final List<ConfigTextField> generalFields = new ArrayList<>();
    private final List<ConfigTextField> contextualFields = new ArrayList<>();
    private final List<ConfigCheckbox> checkboxes = new ArrayList<>();
    private final List<ContextButton> contextButtons = new ArrayList<>();
    private final List<SectionLabel> sectionLabels = new ArrayList<>();

    private static final int BTN_SAVE = 0, BTN_CANCEL = 1, BTN_CONTEXT_START = 100;
    private static final int FIELD_W = 70, FIELD_H = 16, LABEL_W = 110, ELEM_H = 22, GAP = 8;

    // Contextual field options now include mouseSmoothing
    private static final String[] CONTEXTUAL_OPTIONS = {
            "strafingRollFactor", "forwardVelocityPitchFactor",
            "verticalVelocityPitchFactor", "horizontalVelocitySmoothingFactor",
            "verticalVelocitySmoothingFactor", "mouseSmoothing"
    };

    public CameraConfigGuiScreen(GuiScreen parent) {
        this.parentScreen = parent;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        if (tempContextValues.isEmpty()) {
            for (Context ctx : Context.values()) {
                CameraConfig.Contextual cfg = getContextConfig(ctx);
                tempContextValues.put(ctx, new double[]{
                        cfg.strafingRollFactor, cfg.forwardVelocityPitchFactor,
                        cfg.verticalVelocityPitchFactor, cfg.horizontalVelocitySmoothingFactor,
                        cfg.verticalVelocitySmoothingFactor, cfg.mouseSmoothing
                });
            }
        }
        rebuildGui();
    }

    private void rebuildGui() {
        buttonList.clear();
        generalFields.clear();
        contextualFields.clear();
        checkboxes.clear();
        sectionLabels.clear();
        contextButtons.clear();

        initPanels();
        addButton(new GuiButton(BTN_SAVE, width/2 - 85, height - 30, 80, 20, I18n.format("gui.done")));
        addButton(new GuiButton(BTN_CANCEL, width/2 + 5, height - 30, 80, 20, I18n.format("gui.cancel")));
        initGeneralFields();
        initContextButtons();
        initContextualFields();
    }

    private CameraConfig.Contextual getContextConfig(Context ctx) {
        switch (ctx) {
            case SPRINTING: return CameraConfig.sprinting;
            case SWIMMING:  return CameraConfig.swimming;
            case FLYING:    return CameraConfig.flying;
            case MOUNTS:    return CameraConfig.mounts;
            case VEHICLES:  return CameraConfig.vehicles;
            default:        return CameraConfig.walking;
        }
    }

    private void initPanels() {
        int panelW = Math.min(280, Math.max(200, (width - 40) / 2));
        int startX = (width - panelW * 2 - 10) / 2;
        int panelTop = 55, panelH = height - panelTop - 40;

        if (leftPanel == null) {
            leftPanel = new ScrollablePanel(mc, startX, panelTop, panelW, panelH);
            rightPanel = new ScrollablePanel(mc, startX + panelW + 10, panelTop, panelW, panelH);
        } else {
            leftPanel.updateBounds(startX, panelTop, panelW, panelH);
            rightPanel.updateBounds(startX + panelW + 10, panelTop, panelW, panelH);
        }
    }

    private void initContextButtons() {
        Context[] contexts = Context.values();
        int availW = rightPanel.width - 16, gap = 3;
        int btnW = (availW - (contexts.length - 1) * gap) / contexts.length;

        for (int i = 0; i < contexts.length; i++) {
            ContextButton btn = new ContextButton(BTN_CONTEXT_START + i,
                    rightPanel.x + 9 + i * (btnW + gap), rightPanel.y + 8, btnW, 20,
                    contexts[i].getDisplayName(), contexts[i] == currentContext);
            contextButtons.add(btn);
            addButton(btn);
        }
        rightPanel.setHeaderHeight(36);
    }

    private void initGeneralFields() {
        int x = leftPanel.x + 8, baseY = leftPanel.y + 8, y = baseY;
        int fieldX = x + LABEL_W;
        CameraConfig.General g = CameraConfig.general;

        checkboxes.add(new ConfigCheckbox(x, y, "enabled", g.enabled));
        y += ELEM_H;
        checkboxes.add(new ConfigCheckbox(x, y, "enableInThirdPerson", g.enableInThirdPerson));
        y += ELEM_H;
        y = addField(generalFields, x, fieldX, y, "contextTransitionSmoothing", g.contextTransitionSmoothing, "general");
        y += GAP;

        // Turning Roll section
        y = addSection(x, y, "turningRoll");
        y = addField(generalFields, x, fieldX, y, "turningRollAccumulation", g.turningRollAccumulation, "general");
        y = addField(generalFields, x, fieldX, y, "turningRollIntensity", g.turningRollIntensity, "general");
        y = addField(generalFields, x, fieldX, y, "turningRollSmoothing", g.turningRollSmoothing, "general");
        y += GAP;

        // Camera Sway section
        y = addSection(x, y, "cameraSway");
        y = addField(generalFields, x, fieldX, y, "cameraSwayIntensity", g.cameraSwayIntensity, "general");
        y = addField(generalFields, x, fieldX, y, "cameraSwayFrequency", g.cameraSwayFrequency, "general");
        y = addField(generalFields, x, fieldX, y, "cameraSwayFadeInDelay", g.cameraSwayFadeInDelay, "general");
        y = addField(generalFields, x, fieldX, y, "cameraSwayFadeInLength", g.cameraSwayFadeInLength, "general");
        y = addField(generalFields, x, fieldX, y, "cameraSwayFadeOutLength", g.cameraSwayFadeOutLength, "general");
        y += GAP;

        // Screen Shakes section
        y = addSection(x, y, "screenShakes");
        y = addField(generalFields, x, fieldX, y, "screenShakesMaxIntensity", g.screenShakesMaxIntensity, "general");
        y = addField(generalFields, x, fieldX, y, "screenShakesMaxFrequency", g.screenShakesMaxFrequency, "general");
        y = addField(generalFields, x, fieldX, y, "explosionTrauma", g.explosionTrauma, "general");

        checkboxes.add(new ConfigCheckbox(x, y, "scaleExplosionByStrength", g.scaleExplosionByStrength));
        y += ELEM_H;

        y = addField(generalFields, x, fieldX, y, "thunderTrauma", g.thunderTrauma, "general");
        y = addField(generalFields, x, fieldX, y, "handSwingTrauma", g.handSwingTrauma, "general");

        leftPanel.setContentHeight(y - baseY + 16);
    }

    private void initContextualFields() {
        contextualFields.clear();
        double[] values = tempContextValues.get(currentContext);
        String category = currentContext.name().toLowerCase();

        int x = rightPanel.x + 8, baseY = rightPanel.getScrollableAreaTop() + 8, y = baseY;
        int fieldX = x + LABEL_W;

        for (int i = 0; i < CONTEXTUAL_OPTIONS.length; i++) {
            y = addField(contextualFields, x, fieldX, y, CONTEXTUAL_OPTIONS[i], values[i], category) + 4;
        }
        rightPanel.setContentHeight(y - rightPanel.getScrollableAreaTop() + ELEM_H);
    }

    private int addSection(int x, int y, String section) {
        sectionLabels.add(new SectionLabel(I18n.format("cameraoverhaul.config.section." + section), x, y));
        return y + 16;
    }

    private int addField(List<ConfigTextField> list, int labelX, int fieldX, int y,
                         String option, double value, String category) {
        ConfigTextField field = new ConfigTextField(fontRenderer, fieldX, y, FIELD_W, FIELD_H, option, category);
        field.setText(formatDouble(value));
        field.labelX = labelX;
        list.add(field);
        return y + ELEM_H;
    }

    static String formatDouble(double v) {
        return v == (long) v ? String.valueOf((long) v) :
                String.format("%.2f", v).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, "§l" + I18n.format("cameraoverhaul.config.title"), width/2, 15, 0xFFFFFF);
        drawCenteredString(fontRenderer, "§7" + I18n.format("cameraoverhaul.config.subtitle"), width/2, 28, 0xAAAAAA);

        // Left panel
        leftPanel.drawBackground(I18n.format("cameraoverhaul.config.category.general"));
        leftPanel.beginScissor();
        int scrollL = leftPanel.getScrollOffset();
        for (ConfigCheckbox cb : checkboxes) cb.draw(mc, fontRenderer, scrollL, mx, my);
        for (SectionLabel lbl : sectionLabels) lbl.draw(fontRenderer, scrollL);
        for (ConfigTextField f : generalFields) f.draw(fontRenderer, scrollL, mx, my);
        leftPanel.endScissor();
        leftPanel.drawScrollbar();

        // Right panel
        rightPanel.drawBackground(I18n.format("cameraoverhaul.config.category.contextual"));
        for (ContextButton btn : contextButtons) btn.drawButton(mc, mx, my, pt);
        drawHorizontalLine(rightPanel.x + 8, rightPanel.x + rightPanel.width - 8,
                rightPanel.getScrollableAreaTop() - 1, 0xFF555555);
        rightPanel.beginScissor();
        int scrollR = rightPanel.getScrollOffset();
        for (ConfigTextField f : contextualFields) f.draw(fontRenderer, scrollR, mx, my);
        rightPanel.endScissor();
        rightPanel.drawScrollbar();

        leftPanel.mouseDragged(mx, my);
        rightPanel.mouseDragged(mx, my);
        leftPanel.drawBorders();
        rightPanel.drawBorders();

        for (GuiButton btn : buttonList) {
            if (!(btn instanceof ContextButton)) btn.drawButton(mc, mx, my, pt);
        }
        drawTooltips(mx, my);
    }

    private void drawTooltips(int mx, int my) {
        if (leftPanel.isMouseOver(mx, my)) {
            int scroll = leftPanel.getScrollOffset();
            for (ConfigCheckbox cb : checkboxes) {
                cb.updateHoverRegion(scroll);
                if (cb.isResetHovered(mx, my)) { drawHoveringText(cb.getResetTooltip(), mx, my); return; }
                if (cb.isHovered(mx, my)) { drawHoveringText(cb.getTooltip(), mx, my); return;}
            }
            for (ConfigTextField f : generalFields) {
                f.updateHoverRegion(scroll);
                if (f.isResetHovered(mx, my)) { drawHoveringText(f.getResetTooltip(), mx, my); return; }
                if (f.isHovered(mx, my)) { drawHoveringText(f.getTooltip(), mx, my); return; }
            }
        }
        if (rightPanel.isMouseOverScrollableArea(mx, my)) {
            int scroll = rightPanel.getScrollOffset();
            for (ConfigTextField f : contextualFields) {
                f.updateHoverRegion(scroll);
                if (f.isResetHovered(mx, my)) { drawHoveringText(f.getResetTooltip(), mx, my); return; }
                if (f.isHovered(mx, my)) { drawHoveringText(f.getTooltip(), mx, my); return; }
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        leftPanel.handleMouseInput();
        rightPanel.handleMouseInput();
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        if (btn.id == BTN_SAVE) {
            saveAll();
            mc.displayGuiScreen(parentScreen);
        } else if (btn.id == BTN_CANCEL) {
            mc.displayGuiScreen(parentScreen);
        } else if (btn.id >= BTN_CONTEXT_START) {
            saveCurrentContext();
            int idx = btn.id - BTN_CONTEXT_START;
            currentContext = Context.values()[idx];
            for (int i = 0; i < contextButtons.size(); i++) {
                contextButtons.get(i).setSelected(i == idx);
                contextButtons.get(i).resetAnimation();
            }
            initContextualFields();
        }
    }

    @Override
    protected void keyTyped(char c, int key) throws IOException {
        if (key == Keyboard.KEY_ESCAPE) { mc.displayGuiScreen(parentScreen); return; }
        if (key == Keyboard.KEY_TAB) { cycleFocus(); return; }

        for (ConfigTextField f : generalFields) f.textboxKeyTyped(c, key);
        for (ConfigTextField f : contextualFields) f.textboxKeyTyped(c, key);
        super.keyTyped(c, key);
    }

    private void cycleFocus() {
        List<ConfigTextField> all = new ArrayList<>(generalFields);
        all.addAll(contextualFields);
        int focused = -1;
        for (int i = 0; i < all.size(); i++) if (all.get(i).isFocused()) { focused = i; break; }
        all.forEach(f -> f.setFocused(false));
        all.get((focused + 1) % all.size()).setFocused(true);
    }

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        super.mouseClicked(mx, my, btn);
        leftPanel.mouseClicked(mx, my, btn);
        rightPanel.mouseClicked(mx, my, btn);

        generalFields.forEach(f -> f.setFocused(false));
        contextualFields.forEach(f -> f.setFocused(false));

        if (leftPanel.isMouseOver(mx, my)) {
            int scroll = leftPanel.getScrollOffset();
            checkboxes.forEach(cb -> cb.mouseClicked(mx, my, scroll));
            for (ConfigTextField f : generalFields) if (f.handleResetClick(mx, my, scroll)) return;
            for (ConfigTextField f : generalFields) f.mouseClicked(mx, my + scroll, btn);
        }

        if (rightPanel.isMouseOverScrollableArea(mx, my)) {
            int scroll = rightPanel.getScrollOffset();
            for (ConfigTextField f : contextualFields) if (f.handleResetClick(mx, my, scroll)) return;
            for (ConfigTextField f : contextualFields) f.mouseClicked(mx, my + scroll, btn);
        }
    }

    @Override
    protected void mouseReleased(int mx, int my, int state) {
        super.mouseReleased(mx, my, state);
        leftPanel.mouseReleased();
        rightPanel.mouseReleased();
    }

    @Override
    public void updateScreen() {
        generalFields.forEach(ConfigTextField::updateCursorCounter);
        contextualFields.forEach(ConfigTextField::updateCursorCounter);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    private void saveCurrentContext() {
        if (contextualFields.size() < CONTEXTUAL_OPTIONS.length) return;
        double[] values = new double[CONTEXTUAL_OPTIONS.length];
        for (int i = 0; i < CONTEXTUAL_OPTIONS.length; i++) values[i] = parseDouble(contextualFields.get(i));
        tempContextValues.put(currentContext, values);
    }

    private void saveAll() {
        CameraConfig.General g = CameraConfig.general;
        g.enabled = checkboxes.get(0).isChecked();
        g.enableInThirdPerson = checkboxes.get(1).isChecked();

        int i = 0;
        g.contextTransitionSmoothing = parseDouble(generalFields.get(i++));

        g.turningRollAccumulation = parseDouble(generalFields.get(i++));
        g.turningRollIntensity = parseDouble(generalFields.get(i++));
        g.turningRollSmoothing = parseDouble(generalFields.get(i++));
        g.cameraSwayIntensity = parseDouble(generalFields.get(i++));
        g.cameraSwayFrequency = parseDouble(generalFields.get(i++));
        g.cameraSwayFadeInDelay = parseDouble(generalFields.get(i++));
        g.cameraSwayFadeInLength = parseDouble(generalFields.get(i++));
        g.cameraSwayFadeOutLength = parseDouble(generalFields.get(i++));
        g.screenShakesMaxIntensity = parseDouble(generalFields.get(i++));
        g.screenShakesMaxFrequency = parseDouble(generalFields.get(i++));
        g.explosionTrauma = parseDouble(generalFields.get(i++));
        g.thunderTrauma = parseDouble(generalFields.get(i++));
        g.handSwingTrauma = parseDouble(generalFields.get(i));

        g.scaleExplosionByStrength = checkboxes.get(2).isChecked();

        saveCurrentContext();

        for (Context ctx : Context.values()) {
            double[] vals = tempContextValues.get(ctx);
            CameraConfig.Contextual cfg = getContextConfig(ctx);
            cfg.strafingRollFactor = vals[0];
            cfg.forwardVelocityPitchFactor = vals[1];
            cfg.verticalVelocityPitchFactor = vals[2];
            cfg.horizontalVelocitySmoothingFactor = vals[3];
            cfg.verticalVelocitySmoothingFactor = vals[4];
            cfg.mouseSmoothing = vals[5];
        }

        net.minecraftforge.common.config.ConfigManager.sync(Tags.MOD_ID,
                net.minecraftforge.common.config.Config.Type.INSTANCE);
    }

    private double parseDouble(ConfigTextField f) {
        try { return Double.parseDouble(f.getText().trim()); }
        catch (Exception e) { return 0; }
    }
}