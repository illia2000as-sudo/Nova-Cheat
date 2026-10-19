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
    private static final int PAGE_SIZE = 45;
    private EditBox searchBox;
    private List<Item> allItems = List.of();
    private List<Item> filteredItems = List.of();
    private int page;
    private int gridLeft;
    private int gridTop;
    private static final int CELL = 24;
    private static final int COLS = 9;

    public ItemPickerScreen() {
        super(Component.literal("NOVA Cheats — предметы"));
    }

    @Override
    protected void init() {
        allItems = BuiltInRegistries.ITEM.stream()
                .filter(item -> !item.getDefaultInstance().isEmpty())
                .sorted(Comparator.comparing(item -> item.getName(item.getDefaultInstance()).getString()))
                .toList();
        gridLeft = width / 2 - (COLS * CELL) / 2;
        gridTop = 78;
        rebuildButtons();
    }

    private void rebuildButtons() {
        String currentSearch = searchBox == null ? "" : searchBox.getValue();
        clearWidgets();
        searchBox = new EditBox(font, width / 2 - 150, 42, 250, 20, Component.literal("Поиск"));
        searchBox.setHint(Component.literal("Название или ID предмета..."));
        searchBox.setValue(currentSearch);
        addRenderableWidget(searchBox);
        addRenderableWidget(Button.builder(Component.literal("Найти"), b -> {
            page = 0;
            updateItems();
        }).bounds(width / 2 + 106, 42, 65, 20).build());
        updateItems();
    }

    private void updateItems() {
        String query = searchBox == null ? "" : searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        filteredItems = allItems.stream().filter(item -> {
            String name = item.getName(item.getDefaultInstance()).getString().toLowerCase(Locale.ROOT);
            String id = BuiltInRegistries.ITEM.getKey(item).toString().toLowerCase(Locale.ROOT);
            return query.isBlank() || name.contains(query) || id.contains(query);
        }).toList();
        int maxPage = Math.max(0, (filteredItems.size() - 1) / PAGE_SIZE);
        page = Math.max(0, Math.min(page, maxPage));
        // Keep the search box and search button; rebuild only the grid controls.
        clearWidgets();
        addRenderableWidget(searchBox);
        addRenderableWidget(Button.builder(Component.literal("Найти"), b -> {
            page = 0;
            updateItems();
        }).bounds(width / 2 + 106, 42, 65, 20).build());

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, filteredItems.size());
        for (int i = start; i < end; i++) {
            Item item = filteredItems.get(i);
            int n = i - start;
            int x = gridLeft + (n % COLS) * CELL;
            int y = gridTop + (n / COLS) * CELL;
            addRenderableWidget(Button.builder(Component.empty(), b ->
                    minecraft.gui.setScreen(new ItemActionScreen(item, this)))
                    .bounds(x, y, 22, 22).build());
        }
        addRenderableWidget(Button.builder(Component.literal("◀"), b -> {
            page = Math.max(0, page - 1);
            updateItems();
        }).bounds(width / 2 - 100, height - 31, 40, 20).build());
        addRenderableWidget(Button.builder(Component.literal("▶"), b -> {
            page = Math.min(Math.max(0, (filteredItems.size() - 1) / PAGE_SIZE), page + 1);
            updateItems();
        }).bounds(width / 2 + 60, height - 31, 40, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Закрыть"), b -> onClose())
                .bounds(width / 2 - 45, height - 31, 90, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0xB8101220);
        graphics.fill(width / 2 - 250, 12, width / 2 + 250, height - 10, 0xE51A2030);
        graphics.text(font, "NOVA CHEATS", width / 2 - 48, 17, 0xFFB7A2FF, true);
        graphics.text(font, "Выбирай предмет по иконке — названия не занимают место", width / 2 - 155, 28, 0xFFD0D5E5, false);
        graphics.text(font, "Найдено: " + filteredItems.size(), width / 2 - 235, 48, 0xFFB8C0D8, false);
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, filteredItems.size());
        for (int i = start; i < end; i++) {
            int n = i - start;
            int x = gridLeft + (n % COLS) * CELL + 3;
            int y = gridTop + (n / COLS) * CELL + 3;
            graphics.item(filteredItems.get(i).getDefaultInstance(), x, y);
        }
        if (mouseY >= gridTop && mouseY < gridTop + 5 * CELL) {
            int col = (mouseX - gridLeft) / CELL;
            int row = (mouseY - gridTop) / CELL;
            int index = page * PAGE_SIZE + row * COLS + col;
            if (col >= 0 && col < COLS && row >= 0 && row < 5 && index >= 0 && index < filteredItems.size()) {
                graphics.text(font, filteredItems.get(index).getName(filteredItems.get(index).getDefaultInstance()),
                        width / 2 - 230, height - 48, 0xFFFFFFFF, true);
            }
        }
        graphics.text(font, "Страница " + (page + 1) + " / " + Math.max(1, (filteredItems.size() + PAGE_SIZE - 1) / PAGE_SIZE),
                width / 2 - 35, height - 48, 0xFFD0D5E5, false);
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(null);
    }
}
