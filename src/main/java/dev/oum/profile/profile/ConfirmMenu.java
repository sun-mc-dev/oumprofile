package dev.oum.profile.profile;

import dev.oum.oumlib.inventory.ChestMenu;
import dev.oum.oumlib.inventory.ItemBuilder;
import dev.oum.oumlib.scheduler.Scheduler;
import dev.oum.profile.config.ProfileConfig.ConfirmGuiSection;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class ConfirmMenu {

    private final ConfirmGuiSection config;
    private final String targetName;
    private final Runnable onConfirm;
    private final Runnable onDeny;

    public ConfirmMenu(@NonNull ConfirmGuiSection config, @NonNull String targetName, @NonNull Runnable onConfirm, @NonNull Runnable onDeny) {
        this.config = config;
        this.targetName = targetName;
        this.onConfirm = onConfirm;
        this.onDeny = onDeny;
    }

    public void open(@NonNull Player player) {
        String resolvedTitle = config.title()
                .replace("<profile>", targetName)
                .replace("<name>", targetName);

        char confirmChar = config.confirmSlotChar().isEmpty() ? 'C' : config.confirmSlotChar().charAt(0);
        char denyChar = config.denySlotChar().isEmpty() ? 'D' : config.denySlotChar().charAt(0);

        List<String> patternList = config.pattern();
        ChestMenu.Builder builder = ChestMenu.builder()
                .title(resolvedTitle)
                .rows(config.rows())
                .pattern(patternList.toArray(new String[0]));

        Material borderMat = Material.matchMaterial(config.borderMaterial());
        if (borderMat == null) borderMat = Material.GRAY_STAINED_GLASS_PANE;
        ItemStack borderItem = ItemBuilder.of(borderMat).name(config.borderName()).build();

        for (String row : patternList) {
            for (char ch : row.toCharArray()) {
                if (ch != confirmChar && ch != denyChar && ch != ' ') {
                    builder = builder.bind(ch, borderItem);
                }
            }
        }

        builder = builder.bind(confirmChar, () -> {
            Material mat = Material.matchMaterial(config.confirmMaterial());
            if (mat == null) mat = Material.GREEN_WOOL;

            List<String> formattedLore = new ArrayList<>();
            for (String line : config.confirmLore()) {
                formattedLore.add(line
                        .replace("<profile>", targetName)
                        .replace("<name>", targetName)
                );
            }

            return ItemBuilder.of(mat)
                    .name(config.confirmName().replace("<profile>", targetName).replace("<name>", targetName))
                    .lore(formattedLore.toArray(new String[0]))
                    .build();
        }).onClick(confirmChar, ctx -> {
            ctx.player().closeInventory();
            Scheduler.runFor(ctx.player(), onConfirm);
        });

        builder = builder.bind(denyChar, () -> {
            Material mat = Material.matchMaterial(config.denyMaterial());
            if (mat == null) mat = Material.RED_WOOL;

            List<String> formattedLore = new ArrayList<>();
            for (String line : config.denyLore()) {
                formattedLore.add(line
                        .replace("<profile>", targetName)
                        .replace("<name>", targetName)
                );
            }

            return ItemBuilder.of(mat)
                    .name(config.denyName().replace("<profile>", targetName).replace("<name>", targetName))
                    .lore(formattedLore.toArray(new String[0]))
                    .build();
        }).onClick(denyChar, ctx -> {
            ctx.player().closeInventory();
            Scheduler.runFor(ctx.player(), onDeny);
        });

        builder.build().open(player);
    }
}
