package com.dreamworld

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.block.BedBlock
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.TeleportTarget
import net.minecraft.world.World
import kotlin.random.Random

class DreamWorldMod : ModInitializer {
    companion object {
        const val MOD_ID = "dreamworld"
        val DREAM_DIMENSION: RegistryKey<World> = RegistryKey.of(
            RegistryKeys.WORLD,
            Identifier.of(MOD_ID, "dream_world")
        )
    }

    override fun onInitialize() {
        println("Dream World Mod initialized!")

        UseBlockCallback.EVENT.register { player, world, hand, hitResult ->
            val block = world.getBlockState(hitResult.blockPos).block

            // Check if block is a bed and world is server and night
            if (block is BedBlock && !world.isClient && isNight(world)) {
                handleBedUse(player as? PlayerEntity ?: return@register ActionResult.PASS, world, hitResult.blockPos)
                ActionResult.SUCCESS
            } else {
                ActionResult.PASS
            }
        }
    }

    private fun handleBedUse(player: PlayerEntity, world: World, bedPos: BlockPos) {
        if (world.isClient) return

        // Ensure we have a ServerPlayerEntity for dimension travel
        if (player !is ServerPlayerEntity) return

        if (Random.nextFloat() < 0.3f) {
            val server = world.server ?: return
            val dreamWorld = server.getWorld(DREAM_DIMENSION)

            if (dreamWorld != null) {
                player.sendMessage(Text.literal("§dYou drift into a strange dream..."), true)

                // Create spawn position in dream world
                val spawnPos = Vec3d(0.5, 65.0, 0.5)

// Create TeleportTarget with proper constructor
                val teleportTarget = TeleportTarget(
                    dreamWorld,         // ServerWorld destination
                    spawnPos,          // Vec3d position
                    Vec3d.ZERO,        // Vec3d velocity
                    0.0f,              // float yaw
                    0.0f,              // float pitch
                    TeleportTarget.PostDimensionTransition {
                        // Any post-teleportation logic here
                    }
                )


                // Use vanilla teleport method
                player.teleportTo(teleportTarget)

                println("Player ${player.name.string} teleported to dream world!")
            } else {
                println("Dream world not found! Make sure your dimension is properly registered.")
            }
        }
    }

    private fun isNight(world: World): Boolean {
        val time = world.timeOfDay % 24000L
        // Night time roughly between 13000 and 23000 ticks
        return time in 13000L..23000L
    }
}