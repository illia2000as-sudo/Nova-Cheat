package dev.nova.cheats;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class ItemPickerScreen extends Screen {
    private static final int PAGE_SIZE = 18;
    private EditBox searchBox;
    private List<Item> allItems = List.of();
    private List<Item> filteredItems = List.of();
    private int page;

    public ItemPickerScreen() {
        super(Component.literal("NOVA Cheats"));
    }

    @Override
    protected void init() {
        allItems = BuiltInRegistries.ITEM.stream()
                .filter(item -> !item.getDefaultInstance().isEmpty())
                .sorted(Comparator.comparing(item -> item.getName(item.getDefaultInstance()).getString()))
                .toList();
        searchBox = new EditBox(font, width / 2 - 150, 42, 300, 20, Component.literal("Search"));
        searchBox.setHint(Component.literal("Поиск предметов по названию или ID"));
        addRenderableWidget(searchBox);
        addRenderableWidget(Button.builder(Component.literal("Найти"), b -> {
            page = 0;
            updateItems();
        }).bounds(width / 2 + 156, 42, 60, 20).build());
        updateItems();
    }

    private void updateItems() {
        String query = searchBox == null ? "" : searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        filteredItems = allItems.stream().filter(item -> {
            String name = item.getDescription().getString().toLowerCase(Locale.ROOT);
            String id = BuiltInRegistries.ITEM.getKey(item).toString().toLowerCase(Locale.ROOT);
            return query.isBlank() || name.contains(query) || id.contains(query);
        }).toList();
        page = Math.max(0, Math.min(page, Math.max(0, (filteredItems.size() - 1) / PAGE_SIZE)));
        rebuildButtons();
    }

    private void rebuildButtons() {
        String currentSearch = searchBox == null ? "" : searchBox.getValue();
        clearWidgets();
        searchBox = new EditBox(font, width / 2 - 150, 42, 300, 20, Component.literal("Search"));
        searchBox.setHint(Component.literal("Поиск предметов по названию или ID"));
        searchBox.setValue(currentSearch);
        addRenderableWidget(searchBox);
        addRenderableWidget(Button.builder(Component.literal("Найти"), b -> {
            page = 0;
            updateItems();
        }).bounds(width / 2 + 156, 42, 60, 20).build());

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, filteredItems.size());
        int buttonW = 150, buttonH = 20, gap = 6, columns = 3;
        int left = (width - (columns * buttonW + (columns - 1) * gap)) / 2;
        for (int i = start; i < end; i++) {
            Item item = filteredItems.get(i);
            int n = i - start;
            int x = left + (n % columns) * (buttonW + gap);
            int y = 78 + (n / columns) * 25;
            String label = item.getDescription().getString();
            if (label.length() > 22) label = label.substring(0, 19) + "...";
            addRenderableWidget(Button.builder(Component.literal(label), b ->
                    minecraft.gui.setScreen(new ItemActionScreen(item, this)))
                    .bounds(x, y, buttonW, buttonH).build());
        }
        addRenderableWidget(Button.builder(Component.literal("◀ Назад"), b -> {
            page = Math.max(0, page - 1);
            rebuildButtons();
        }).bounds(width / 2 - 160, height - 55, 90, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Далее ▶"), b -> {
            page = Math.min(Math.max(0, (filteredItems.size() - 1) / PAGE_SIZE), page + 1);
            rebuildButtons();
        }).bounds(width / 2 + 70, height - 55, 90, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Закрыть"), b -> onClose())
                .bounds(width / 2 - 45, height - 29, 90, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0xB8101220);
        graphics.fill(width / 2 - 250, 15, width / 2 + 250, height - 12, 0xE51A2030);
        graphics.text(font, "NOVA CHEATS", width / 2 - 48, 20, 0xFFB7A2FF, true);
        graphics.text(font, "Выбери предмет мышкой — ID вводить не нужно", width / 2 - 140, 29, 0xFFD0D5E5, false);
        graphics.text(font, "Найдено: " + filteredItems.size(), width / 2 - 235, 48, 0xFFB8C0D8, false);
        graphics.text(font, "Страница " + (page + 1) + " / " + Math.max(1, (filteredItems.size() + PAGE_SIZE - 1) / PAGE_SIZE),
                width / 2 - 45, height - 48, 0xFFD0D5E5, false);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(null);
    }
}
