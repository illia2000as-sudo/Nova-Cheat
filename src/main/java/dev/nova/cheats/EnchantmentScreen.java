package dev.nova.cheats;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class EnchantmentScreen extends Screen {
    private static final int PAGE_SIZE = 12;
    private final Item item;
    private final Screen parent;
    private final List<String> selected = new ArrayList<>();
    private EditBox levelBox;
    private EditBox searchBox;
    private List<Identifier> enchantments = List.of();
    private int level = 1;
    private int page = 0;

    public EnchantmentScreen(Item item, Screen parent) {
        super(Component.literal("Зачарования"));
        this.item = item;
        this.parent = parent;
    }

    @Override
    protected void init() {
        if (minecraft.level == null) {
            enchantments = List.of();
        } else {
            enchantments = minecraft.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).keySet().stream()
                    .sorted(Comparator.comparing(Identifier::toString)).toList();
        }
        rebuildEnchantmentButtons();
    }

    private void buildControls() {
        levelBox = new EditBox(font, width / 2 + 90, 36, 55, 20, Component.literal("Уровень"));
        levelBox.setValue(Integer.toString(level));
        addRenderableWidget(levelBox);

        searchBox = new EditBox(font, width / 2 - 175, 36, 245, 20, Component.literal("Поиск чар"));
        searchBox.setHint(Component.literal("Поиск зачарования..."));
        addRenderableWidget(searchBox);

        addRenderableWidget(Button.builder(Component.literal("Уровень 1–255"), b -> {
            readLevel();
            levelBox.setValue(Integer.toString(level));
        }).bounds(width / 2 + 150, 36, 135, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Поиск"), b -> {
            page = 0;
            rebuildEnchantmentButtons();
        }).bounds(width / 2 - 175, 60, 70, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Выбрать все"), b -> {
            selected.clear();
            for (Identifier id : filteredEnchantments()) selected.add(id.toString());
            page = 0;
            rebuildEnchantmentButtons();
        }).bounds(width / 2 - 95, 60, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Снять выбор"), b -> {
            selected.clear();
            page = 0;
            rebuildEnchantmentButtons();
        }).bounds(width / 2 + 12, 60, 100, 20).build());
    }

    private void readLevel() {
        try {
            level = Math.max(1, Math.min(255, Integer.parseInt(levelBox.getValue().trim())));
        } catch (NumberFormatException ignored) {
            level = 1;
        }
    }

    private List<Identifier> filteredEnchantments() {
        String query = searchBox == null ? "" : searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        return enchantments.stream().filter(id ->
                query.isBlank() || id.toString().toLowerCase(Locale.ROOT).contains(query)
                        || id.getPath().toLowerCase(Locale.ROOT).contains(query)).toList();
    }

    private void rebuildEnchantmentButtons() {
        String currentSearch = searchBox == null ? "" : searchBox.getValue();
        List<Identifier> filtered = enchantments.stream()
                .filter(id -> currentSearch.isBlank()
                        || id.toString().toLowerCase(Locale.ROOT).contains(currentSearch.toLowerCase(Locale.ROOT)))
                .toList();
        int maxPage = Math.max(0, (filtered.size() - 1) / PAGE_SIZE);
        page = Math.max(0, Math.min(page, maxPage));
        clearWidgets();
        buildControls();
        searchBox.setValue(currentSearch);

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, filtered.size());
        for (int i = start; i < end; i++) {
            Identifier id = filtered.get(i);
            String key = id.toString();
            String label = (selected.contains(key) ? "✓ " : "+ ") + id.getPath();
            int row = i - start;
            addRenderableWidget(Button.builder(Component.literal(label), b -> {
                if (selected.contains(key)) selected.remove(key);
                else selected.add(key);
                rebuildEnchantmentButtons();
            }).bounds(width / 2 - 150 + (row % 2) * 155, 88 + (row / 2) * 25, 150, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("◀"), b -> {
            page = Math.max(0, page - 1);
            rebuildEnchantmentButtons();
        }).bounds(width / 2 - 85, height - 60, 40, 20).build());
        addRenderableWidget(Button.builder(Component.literal("▶"), b -> {
            page = Math.min(maxPage, page + 1);
            rebuildEnchantmentButtons();
        }).bounds(width / 2 + 45, height - 60, 40, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Выдать предмет"), b -> giveItem())
                .bounds(width / 2 - 75, height - 34, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Назад"), b -> minecraft.gui.setScreen(parent))
                .bounds(width / 2 - 185, height - 34, 90, 20).build());
    }

    private void giveItem() {
        readLevel();
        String itemId = BuiltInRegistries.ITEM.getKey(item).toString();
        if (selected.isEmpty()) {
            NOVACheatsClient.sendCommand("give @s " + itemId + " 1");
            return;
        }
        // Java 26.2 expects a flat enchantment-id -> level map in the enchantments component.
        StringBuilder command = new StringBuilder("give @s ")
                .append(itemId)
                .append("[minecraft:enchantments={");
        for (int i = 0; i < selected.size(); i++) {
            if (i > 0) command.append(",");
            command.append("\"").append(selected.get(i)).append("\":").append(level);
        }
        command.append("}] 1");
        NOVACheatsClient.sendCommand(command.toString());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0xB8101220);
        graphics.fill(width / 2 - 205, 12, width / 2 + 205, height - 12, 0xE51A2030);
        graphics.text(font, "Зачарования: " + item.getName(item.getDefaultInstance()).getString(), width / 2 - 180, 18, 0xFFB7A2FF, true);
        graphics.text(font, "Выбрано: " + selected.size() + " | Уровень: 1–255", width / 2 - 180, 68, 0xFFD0D5E5, false);
        graphics.text(font, "Нужны читы в одиночном мире или права оператора на сервере.", width / 2 - 155, height - 77, 0xFFFFC77D, false);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }
}
