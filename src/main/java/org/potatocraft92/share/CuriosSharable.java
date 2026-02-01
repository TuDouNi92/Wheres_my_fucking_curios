package org.potatocraft92.share;

import net.minecraft.world.entity.LivingEntity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mvplugins.multiverse.inventories.profile.data.ProfileData;
import org.mvplugins.multiverse.inventories.share.ProfileEntry;
import org.mvplugins.multiverse.inventories.share.Sharable;
import org.mvplugins.multiverse.inventories.share.SharableHandler;
import org.potatocraft92.Main;
import org.potatocraft92.snapshot.CuriosSnapShot;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class CuriosSharable {
    public static final Sharable<CuriosSnapShot> CURIO_SHARES = new Sharable.Builder<>("curioSharable", CuriosSnapShot.class, new SharableHandler<>() {
        @Override
        public void updateProfile(ProfileData profileData, Player player) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + player.getDisplayName() + " {\"text\":\"Updating profile\", \"color\":\"blue\"} ");
            CraftPlayer craftPlayer = (CraftPlayer) player;
            CuriosSnapShot snap = new CuriosSnapShot();
            try {
                Class<?> curiosApi = Class.forName("top.theillusivec4.curios.api.CuriosApi");
                Method getInv = curiosApi.getMethod("getCuriosInventory", LivingEntity.class);
                Object lazyOptional = getInv.invoke(null, craftPlayer.getHandle());
                if (lazyOptional != null) {
                    Method orElseNull = lazyOptional.getClass().getMethod("orElse", Object.class);
                    Object handler = orElseNull.invoke(lazyOptional, (Object) null);
                    if (handler != null) {
                        ICuriosItemHandler iCuriosItemHandler = (ICuriosItemHandler) handler;
                        Map<String, ICurioStacksHandler> forgeMap = iCuriosItemHandler.getCurios();

                        //Iterates Map of StacksHandler
                        for (Map.Entry<String, ICurioStacksHandler> entry : forgeMap.entrySet()) {
                            String slotName = entry.getKey();
                            ICurioStacksHandler iCurioStacksHandler = entry.getValue();
                            Method getStacksMethod = iCurioStacksHandler.getClass().getMethod("getStacks");
                            Object dynamicStackHandler = getStacksMethod.invoke(iCurioStacksHandler);
                            Method getSlotsMethod = dynamicStackHandler.getClass().getMethod("getSlots");
                            Method getStackInSlotMethod = dynamicStackHandler.getClass().getMethod("getStackInSlot", int.class);
                            int stackSize = (int) getSlotsMethod.invoke(dynamicStackHandler);

                            //Collect items for every slot, like Map<identifier,List<ItemStack>>
                            List<ItemStack> bukkitItems = new ArrayList<>();
                            for (int i = 0; i < stackSize; i++) {
                                net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) getStackInSlotMethod.invoke(dynamicStackHandler, i);
                                bukkitItems.add(CraftItemStack.asBukkitCopy(nmsStack));
                            }
                            snap.curios.put(slotName, bukkitItems);
                        }
                    }
                }
                profileData.set(CURIO_SHARES, snap);

            } catch (Throwable e) {
                Main.log().warning("Error on updating profile:\n" + e);
            }
        }

        @Override
        public boolean updatePlayer(Player player, ProfileData profileData) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + player.getDisplayName() + " {\"text\":\"Updating player\", \"color\":\"blue\"}");
            CraftPlayer craftPlayer = (CraftPlayer) player;
            AtomicBoolean result = new AtomicBoolean();
            try {
                Class<?> curiosApi = Class.forName("top.theillusivec4.curios.api.CuriosApi");
                Method getInv = curiosApi.getMethod("getCuriosInventory", LivingEntity.class);
                Object lazyOptional = getInv.invoke(null, craftPlayer.getHandle());
                if (lazyOptional != null) {
                    Method orElseNull = lazyOptional.getClass().getMethod("orElse", Object.class);
                    Object handler = orElseNull.invoke(lazyOptional, (Object) null);
                    if (handler != null) {
                        ICuriosItemHandler iCuriosItemHandler = (ICuriosItemHandler) handler;
                        CuriosSnapShot snapShot = profileData.get(CURIO_SHARES);
                        if (snapShot == null) {
                            profileData.set(CURIO_SHARES, new CuriosSnapShot());
                            snapShot = profileData.get(CURIO_SHARES);
                        }

                        //Get stacksHandler for every slot, Curios slots can be one id against multiple slots
                        Map<String, ICurioStacksHandler> iCuriosStacksHandlerWrapped = iCuriosItemHandler.getCurios();
                        CuriosSnapShot finalSnapShot = snapShot;
                        iCuriosStacksHandlerWrapped.forEach((slotName, iCurioStacksHandler) -> {
                            if (finalSnapShot.curios.isEmpty()) {
                                result.set(false);
                            }
                            //Iterate the stored stacks
                            finalSnapShot.curios.forEach((storedSlotId, storedStacks) -> {
                                if (slotName.equals(storedSlotId)) {
                                    storedStacks.forEach(storedItemStackForSlot -> {
                                        try {
                                            Method getStacksMethod = iCurioStacksHandler.getClass().getMethod("getStacks");
                                            Object curioDynamicStackHandler = getStacksMethod.invoke(iCurioStacksHandler);
                                            Method getSlotsMethod = curioDynamicStackHandler.getClass().getMethod("getSlots");
                                            Method setStackInSlotMethod = curioDynamicStackHandler.getClass().getMethod("setStackInSlot", int.class, net.minecraft.world.item.ItemStack.class);
                                            int curioSlotSize = (int) getSlotsMethod.invoke(curioDynamicStackHandler);

                                            //Apply stacks on current curio slots, note that curiosStacks does not mean how many curio slots we have.
                                            //It means How many stacks we have in current slot.
                                            //Iterates curio slots
                                            for (int currentCurioSlotsIndex = 0; currentCurioSlotsIndex < curioSlotSize; currentCurioSlotsIndex++) {
                                                net.minecraft.world.item.ItemStack storedStack = CraftItemStack.asNMSCopy(storedItemStackForSlot.clone());
                                                setStackInSlotMethod.invoke(curioDynamicStackHandler, currentCurioSlotsIndex, storedStack);
                                            }
                                        } catch (Throwable e) {
                                            Main.log().warning("Error on updating player:\n" + e);
                                        }
                                    });
                                }
                            });
                        });
                    }
                    result.set(true);
                }
            } catch (Throwable e) {
                Main.log().warning("Error on updating player:\n" + e);
                result.set(false);
            }
            return result.get();
        }

    }).stringSerializer(new ProfileEntry(false, "curioSharable")).altName("cs").build();
}
