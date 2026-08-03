package me.tyalternative.laserGame.weapon;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BlocksAttacks;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public final class WeaponItemFactory {

    private WeaponItemFactory() {
    }

    public static ItemStack create(Plugin plugin, WeaponType type) {
        ItemStack item = new ItemStack(type.material());

        item.editMeta(meta -> {
            meta.customName(Component.text(type.displayName()));
            meta.setUnbreakable(true);
            meta.getPersistentDataContainer().set(
                    WeaponManager.weaponIdKey(plugin), PersistentDataType.STRING, type.id());
        });

        item.setData(DataComponentTypes.BLOCKS_ATTACKS, BlocksAttacks.blocksAttacks().build());

        return item;
    }
}
