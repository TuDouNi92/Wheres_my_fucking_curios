package org.potatocraft92;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.mvplugins.multiverse.inventories.MultiverseInventoriesApi;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.potatocraft92.share.CuriosSharable;
import java.util.logging.Logger;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        createCurioGroup();
        getLogger().info("Enabled WMFC");
    }

    private static void createCurioGroup() {
        WorldGroupManager manager = MultiverseInventoriesApi.get().getWorldGroupManager();
        WorldGroup newGroup = manager.newEmptyGroup("curios");
        if (newGroup != null) {
            newGroup.addWorld("world");
            newGroup.addWorld("DIM1");
            newGroup.addWorld("DIM-1");
            newGroup.getShares().addAll(Sharables.fromSharables(CuriosSharable.CURIO_SHARES));
            manager.updateGroup(newGroup);
        } else {
            WorldGroup curiosGroup = manager.getGroup("curios");
            curiosGroup.getShares().addAll(Sharables.fromSharables(CuriosSharable.CURIO_SHARES));
            manager.updateGroup(curiosGroup);
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"mvinv reload");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled WMFC");
    }

    public static Logger log() {
        return Main.getProvidingPlugin(Main.class).getLogger();
    }
}