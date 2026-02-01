package org.potatocraft92.snapshot;


import org.bukkit.inventory.ItemStack;
import org.potatocraft92.util.CuriosSerializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CuriosSnapShot {
    public Map<String, List<ItemStack>> curios = new HashMap<>();

    public CuriosSnapShot() {
    }

    public CuriosSnapShot(Map<String, List<ItemStack>> curios) {
        this.curios = curios;
    }

    @Override
    public String toString() {
        return CuriosSerializer.toBase64(this.curios);
    }

    public static CuriosSnapShot valueOf(String s) {
        Map<String, List<ItemStack>> curios = CuriosSerializer.fromBase64(s);
        return new CuriosSnapShot(curios);
    }
}
