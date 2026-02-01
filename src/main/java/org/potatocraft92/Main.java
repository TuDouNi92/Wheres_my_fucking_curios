package org.potatocraft92;

import org.bukkit.plugin.java.JavaPlugin;
import org.mvplugins.multiverse.inventories.MultiverseInventoriesApi;
import org.potatocraft92.share.CuriosSharable;

import java.util.logging.Logger;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        MultiverseInventoriesApi.get().getWorldGroupManager().recalculateApplicableShares();
        getLogger().info("Enabled WMFC,loaded:" + CuriosSharable.CURIO_SHARES.toString());
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled WMFC");
    }

    public static Logger log() {
        return Main.getProvidingPlugin(Main.class).getLogger();
    }
}