package org.potatocraft92.util;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CuriosSerializer {

    //  Map to String (Base64)
    public static String toBase64(Map<String, List<ItemStack>> data) {
        if (data == null || data.isEmpty()) return "";
        try {
            YamlConfiguration config = new YamlConfiguration();
            // 直接利用 Bukkit 的序列化能力，把 Map 存入 config
            for (Map.Entry<String, List<ItemStack>> entry : data.entrySet()) {
                config.set(entry.getKey(), entry.getValue());
            }
            // 转成文本
            String yamlString = config.saveToString();
            // 转成 Base64 (为了避免 YAML 里的换行符打乱 MV-Inv 的 JSON 存储)
            return Base64.getEncoder().encodeToString(yamlString.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    //  String (Base64) to Map
    public static Map<String, List<ItemStack>> fromBase64(String base64) {
        Map<String, List<ItemStack>> data = new HashMap<>();
        if (base64 == null || base64.isEmpty()) return data;

        try {
            // 解码 Base64
            String yamlString = new String(Base64.getDecoder().decode(base64));
            // 加载 YAML
            YamlConfiguration config = new YamlConfiguration();
            config.loadFromString(yamlString);

            // 还原 Map
            for (String key : config.getKeys(false)) {
                // Bukkit 会自动 还原回来
                @SuppressWarnings("unchecked")
                List<ItemStack> iCurioStacks = (List<ItemStack>) config.getList(key);
                data.put(key, iCurioStacks);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}