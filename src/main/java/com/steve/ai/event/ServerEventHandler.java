package com.steve.ai.event;

import com.steve.ai.SteveMod;
import com.steve.ai.entity.SteveEntity;
import com.steve.ai.entity.SteveManager;
import com.steve.ai.memory.StructureRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = SteveMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEventHandler {
    // AtomicBoolean prevents a race condition where two players log in concurrently
    // and both see stevesSpawned == false, resulting in duplicate spawn waves.
    // compareAndSet ensures only the first thread proceeds.
    private static final AtomicBoolean stevesSpawned = new AtomicBoolean(false);

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ServerLevel level = (ServerLevel) player.level();
            SteveManager manager = SteveMod.getSteveManager();
            if (stevesSpawned.compareAndSet(false, true)) {
                manager.clearAllSteves();
                
                // Clear structure registry for fresh spatial awareness
                StructureRegistry.clear();
                
                // Remove ALL SteveEntity instances from the world (including ones loaded from NBT)
                int removedCount = 0;
                for (var entity : level.getAllEntities()) {
                    if (entity instanceof SteveEntity) {
                        entity.discard();
                        removedCount++;
                    }
                }

                Vec3 playerPos = player.position();
                Vec3 lookVec = player.getLookAngle();
                
                String[] names = {"Steve", "Alex", "Bob", "Charlie"};
                
                for (int i = 0; i < 4; i++) {
                    double offsetX = lookVec.x * 5 + (lookVec.z * (i - 1.5) * 2);
                    double offsetZ = lookVec.z * 5 + (-lookVec.x * (i - 1.5) * 2);
                    
                    Vec3 spawnPos = new Vec3(
                        playerPos.x + offsetX,
                        playerPos.y,
                        playerPos.z + offsetZ
                    );
                    
                    SteveEntity steve = manager.spawnSteve(level, spawnPos, names[i]);
                    if (steve == null) {
                        SteveMod.LOGGER.warn("Failed to spawn Steve agent '{}'", names[i]);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        stevesSpawned.set(false);
    }
}

