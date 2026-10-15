package dev.nova.cheats;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

public final class ItemActionScreen extends Screen {
    private final Item item;
    private final Screen parent;

    public ItemActionScreen(Item item, Screen parent) {
        super(Component.literal("Действия с предметом"));
        this.item = item;
        this.parent = parent;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Выдать без зачарований"), b -> {
            String id = BuiltInRegistries.ITEM.getKey(item).toString();
            NOVACheatsClient.sendCommand("give @s " + id + " 1");
        }).bounds(width / 2 - 115, height / 2 - 15, 230, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Настроить зачарования"), b ->
                minecraft.gui.setScreen(new EnchantmentScreen(item, this)))
                .bounds(width / 2 - 115, height / 2 + 12, 230, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Назад"), b -> minecraft.setScreen(parent))
                .bounds(width / 2 - 50, height / 2 + 44, 100, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0xB8101220);
        graphics.fill(width / 2 - 180, height / 2 - 90, width / 2 + 180, height / 2 + 85, 0xE51A2030);
        graphics.text(font, "Выбран предмет", width / 2 - 48, height / 2 - 65, 0xFFB7A2FF, true);
        graphics.text(font, item.getName(item.getDefaultInstance()).getString(), width / 2 - 90, height / 2 - 42, 0xFFFFFFFF, true);
        graphics.text(font, "Выдача использует права команды /give.", width / 2 - 112, height / 2 + 72, 0xFFFFC77D, false);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
