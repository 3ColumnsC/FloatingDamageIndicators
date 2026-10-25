package com.threecolumnsstudio.floatingdamageindicators.client.screen;

import java.util.function.Consumer;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

final class GridConfigTab implements Tab {

    private static final int CONTENT_TOP_PADDING = 8;

    private final Component title;
    private final GridLayout content;

    GridConfigTab(Component title, GridLayout content) {
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
