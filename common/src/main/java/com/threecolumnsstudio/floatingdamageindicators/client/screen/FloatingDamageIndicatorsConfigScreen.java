package com.threecolumnsstudio.floatingdamageindicators.client.screen;

import java.nio.file.Path;
import java.util.function.Consumer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.threecolumnsstudio.floatingdamageindicators.FloatingDamageIndicators;
import com.threecolumnsstudio.floatingdamageindicators.ModConfig;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class FloatingDamageIndicatorsConfigScreen extends Screen {

    private static final int WIDGET_HEIGHT = 20;
    private static final int ROW_SPACING = 6;
    private static final int COLUMN_SPACING = 4;
    private static final int BUTTON_WIDTH = 100;
    private static final int CONTENT_TOP_PADDING = 8;
    private static final int CONTENT_H_MARGIN = 24;
    private static final int GENERAL_WIDTH = 240;
    private static final int ICON_SIZE = 10;

    private final Screen parent;
    private final HeaderAndFooterLayout layout;
    private final TabManager tabManager;

    private TabNavigationBar tabNavigationBar;

    private boolean showDamage;
    private boolean showReceivedDamage;

    public FloatingDamageIndicatorsConfigScreen(Screen parent) {
        super(Component.translatable("floatingdamageindicators.config.title"));
        this.parent = parent;
        this.layout = new HeaderAndFooterLayout(this);
        this.tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
        readConfig();
    }

    private void readConfig() {
        ModConfig config = ModConfig.get();
        this.showDamage = config.showDamage;
        this.showReceivedDamage = config.showReceivedDamage;
    }

    @Override
    protected void init() {
        GridLayout content = buildGeneralContent();

        this.tabNavigationBar = TabNavigationBar.builder(this.tabManager, this.width)
            .addTabs(new ConfigTab(Component.translatable("floatingdamageindicators.config.tab.general"), content))
            .build();
        this.addRenderableWidget(this.tabNavigationBar);

        LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        footer.addChild(Button.builder(Component.translatable("floatingdamageindicators.config.apply"), button -> apply())
            .width(BUTTON_WIDTH)
            .build());
        footer.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> onClose())
            .width(BUTTON_WIDTH)
            .build());

        this.layout.visitWidgets(this::addRenderableWidget);
        this.tabNavigationBar.selectTab(0, false);
        this.repositionElements();
    }

    private GridLayout buildGeneralContent() {
        int widgetWidth = Math.min(GENERAL_WIDTH,
            Math.max(160, this.width - CONTENT_H_MARGIN * 2 - ICON_SIZE - COLUMN_SPACING));
        GridLayout grid = new GridLayout();
        grid.columnSpacing(COLUMN_SPACING).rowSpacing(ROW_SPACING);

        Tooltip serverSide = Tooltip.create(Component.translatable("floatingdamageindicators.config.server_side"));
        grid.addChild(toggle("floatingdamageindicators.config.show_damage", this.showDamage, value -> this.showDamage = value, widgetWidth, serverSide), 0, 1);
        grid.addChild(toggle("floatingdamageindicators.config.show_received_damage", this.showReceivedDamage, value -> this.showReceivedDamage = value, widgetWidth, serverSide), 1, 1);

        Component restartTooltip = Component.translatable("floatingdamageindicators.config.restart_required");

        Button open = Button.builder(Component.translatable("floatingdamageindicators.config.open_config"), button -> openConfigFile())
            .width(widgetWidth)
            .tooltip(Tooltip.create(Component.translatable("floatingdamageindicators.config.open_config.tooltip")))
            .build();
        grid.addChild(new ReloadIcon(restartTooltip), 2, 0);
        grid.addChild(open, 2, 1);

        MultiLineTextWidget notice = new MultiLineTextWidget(
            Component.translatable("floatingdamageindicators.config.restart_notice").withStyle(ChatFormatting.GOLD),
            this.font);
        notice.setMaxWidth(widgetWidth);
        grid.addChild(new ReloadIcon(restartTooltip), 3, 0);
        grid.addChild(notice, 3, 1);

        return grid;
    }

    private CycleButton<Boolean> toggle(String translationKey, boolean initialValue, Consumer<Boolean> setter, int width, Tooltip tooltip) {
        CycleButton<Boolean> button = CycleButton.onOffBuilder(initialValue)
            .create(0, 0, width, WIDGET_HEIGHT, Component.translatable(translationKey),
                (cycleButton, value) -> setter.accept(value));
        button.setTooltip(tooltip);
        return button;
    }

    private void openConfigFile() {
        Path configDir = FloatingDamageIndicators.CONFIG_DIR;
        if (configDir != null) {
            Util.getPlatform().openPath(configDir.resolve(ModConfig.FILE_NAME));
        }
    }

    private void apply() {
        ModConfig config = ModConfig.get();
        config.showDamage = this.showDamage;
        config.showReceivedDamage = this.showReceivedDamage;
        Path configDir = FloatingDamageIndicators.CONFIG_DIR;
        if (configDir != null) {
            ModConfig.updateToggles(configDir);
        }
        onClose();
    }

    @Override
    protected void repositionElements() {
        if (this.tabNavigationBar == null) {
            return;
        }
        this.tabNavigationBar.setWidth(this.width);
        this.tabNavigationBar.arrangeElements();
        int headerBottom = this.tabNavigationBar.getRectangle().bottom();
        ScreenRectangle tabArea = new ScreenRectangle(0, headerBottom, this.width,
            this.height - this.layout.getFooterHeight() - headerBottom);
        this.tabManager.setTabArea(tabArea);
        this.layout.setHeaderHeight(headerBottom);
        this.layout.arrangeElements();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    protected void renderMenuBackground(GuiGraphics graphics) {
        graphics.blit(CreateWorldScreen.TAB_HEADER_BACKGROUND,
            0, 0, 0.0F, 0.0F, this.width, this.layout.getHeaderHeight(), 16, 16);
        this.renderMenuBackground(graphics, 0, this.layout.getHeaderHeight(), this.width, this.height);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        RenderSystem.enableBlend();
        graphics.blit(Screen.FOOTER_SEPARATOR,
            0, this.height - this.layout.getFooterHeight() - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
        RenderSystem.disableBlend();
    }

    private static final class ConfigTab implements Tab {

        private final Component title;
        private final GridLayout content;

        private ConfigTab(Component title, GridLayout content) {
            this.title = title;
            this.content = content;
        }

        @Override
        public Component getTabTitle() {
            return this.title;
        }

        @Override
        public void visitChildren(Consumer<AbstractWidget> consumer) {
            this.content.visitWidgets(consumer);
        }

        @Override
        public void doLayout(ScreenRectangle area) {
            this.content.arrangeElements();
            int centeredX = area.left() + (area.width() - this.content.getWidth()) / 2;
            this.content.setPosition(centeredX, area.top() + CONTENT_TOP_PADDING);
        }
    }
}
