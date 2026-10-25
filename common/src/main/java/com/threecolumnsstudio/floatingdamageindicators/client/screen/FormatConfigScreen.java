package com.threecolumnsstudio.floatingdamageindicators.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.threecolumnsstudio.floatingdamageindicators.DamageType;
import com.threecolumnsstudio.floatingdamageindicators.FloatingDamageIndicators;
import com.threecolumnsstudio.floatingdamageindicators.ModConfig;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
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

public final class FormatConfigScreen extends Screen {

    private static final int WIDGET_WIDTH = 280;
    private static final int WIDGET_HEIGHT = 20;
    private static final int ROW_SPACING = 6;
    private static final int BUTTON_WIDTH = 100;
    private static final int FIELD_SPACING = 4;
    private static final int PREFIX_FIELD_WIDTH = 180;
    private static final int LABEL_WIDTH = WIDGET_WIDTH - PREFIX_FIELD_WIDTH - FIELD_SPACING;

    private final FloatingDamageIndicatorsConfigScreen parent;
    private final DamageType type;
    private final HeaderAndFooterLayout layout;
    private final TabManager tabManager;

    private TabNavigationBar tabNavigationBar;

    private boolean enabled;
    private boolean showDamage;
    private String prefix;
    private String color;

    public FormatConfigScreen(FloatingDamageIndicatorsConfigScreen parent, DamageType type) {
        super(FormatLabels.forType(type));
        this.parent = parent;
        this.type = type;
        this.layout = new HeaderAndFooterLayout(this);
        this.tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
        readConfig();
    }

    private void readConfig() {
        ModConfig.FormatEntry format = ModConfig.get().getEffectiveFormat(this.type);
        this.enabled = format.enabled;
        this.showDamage = format.showDamage;
        this.prefix = ModConfig.sanitizePrefix(this.type, format.prefix);
        this.color = ModConfig.sanitizeColor(this.type, format.color);
    }

    @Override
    protected void init() {
        GridLayout content = buildContentGrid();

        this.tabNavigationBar = TabNavigationBar.builder(this.tabManager, this.width)
            .addTabs(new GridConfigTab(FormatLabels.forType(this.type), content))
            .build();
        this.addRenderableWidget(this.tabNavigationBar);

        LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        footer.addChild(Button.builder(Component.translatable("floatingdamageindicators.config.apply"),
                button -> apply())
            .width(BUTTON_WIDTH)
            .build());
        footer.addChild(Button.builder(CommonComponents.GUI_BACK, button -> onClose())
            .width(BUTTON_WIDTH)
            .build());

        this.layout.visitWidgets(this::addRenderableWidget);
        this.tabNavigationBar.selectTab(0, false);
        this.repositionElements();
    }

    private GridLayout buildContentGrid() {
        GridLayout grid = new GridLayout();
        grid.columnSpacing(FIELD_SPACING).rowSpacing(ROW_SPACING);

        GridLayout.RowHelper rows = grid.createRowHelper(1);

        CycleButton<Boolean> enabledButton = CycleButton.onOffBuilder(this.enabled)
            .create(0, 0, WIDGET_WIDTH, WIDGET_HEIGHT, Component.translatable("floatingdamageindicators.config.enabled"),
                (button, value) -> this.enabled = value);
        if (this.type == DamageType.INSTANT_KILL) {
            enabledButton.setTooltip(Tooltip.create(
                Component.translatable("floatingdamageindicators.config.instant_kill_server_side")));
        }
        rows.addChild(enabledButton);

        rows.addChild(CycleButton.onOffBuilder(this.showDamage)
            .create(0, 0, WIDGET_WIDTH, WIDGET_HEIGHT,
                Component.translatable("floatingdamageindicators.config.show_number"),
                (button, value) -> this.showDamage = value));

        rows.addChild(prefixRow());
        rows.addChild(colorRow());

        return grid;
    }

    private LinearLayout prefixRow() {
        LinearLayout row = LinearLayout.horizontal().spacing(FIELD_SPACING);
        row.addChild(new StringWidget(LABEL_WIDTH, WIDGET_HEIGHT,
            Component.translatable("floatingdamageindicators.config.prefix"), this.font).alignLeft());

        EditBox field = new EditBox(this.font, PREFIX_FIELD_WIDTH, WIDGET_HEIGHT,
            Component.translatable("floatingdamageindicators.config.prefix"));
        field.setMaxLength(ModConfig.MAX_PREFIX_CODE_POINTS);
        field.setValue(this.prefix);
        field.setResponder(value -> this.prefix = value);
        field.setTooltip(Tooltip.create(
            Component.translatable("floatingdamageindicators.config.prefix_hint")));
        row.addChild(field);
        return row;
    }

    private LinearLayout colorRow() {
        LinearLayout row = LinearLayout.horizontal().spacing(FIELD_SPACING);
        row.addChild(new StringWidget(LABEL_WIDTH, WIDGET_HEIGHT,
            Component.translatable("floatingdamageindicators.config.color"), this.font).alignLeft());

        ColorField field = new ColorField(this.font,
            Component.translatable("floatingdamageindicators.config.color"), this.color);
        field.setWidth(PREFIX_FIELD_WIDTH);
        field.setResponder(value -> this.color = value);
        field.setTooltip(Tooltip.create(
            Component.translatable("floatingdamageindicators.config.color_hint")));
        row.addChild(field);
        return row;
    }

    private void apply() {
        ModConfig.FormatEntry format = ModConfig.get().ensureFormat(this.type);
        format.enabled = this.enabled;
        format.showDamage = this.showDamage;
        format.prefix = ModConfig.sanitizePrefix(this.type, this.prefix);
        format.color = ModConfig.sanitizeColor(this.type, this.color);
        if (FloatingDamageIndicators.CONFIG_DIR != null) {
            ModConfig.updateFormats(FloatingDamageIndicators.CONFIG_DIR);
        }
        this.minecraft.setScreen(this.parent.withTab(1));
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
        this.minecraft.setScreen(this.parent.withTab(1));
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
}
