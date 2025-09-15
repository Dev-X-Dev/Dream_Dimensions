package com.dreamworld

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.block.BedBlock
import net.minecraft.block.Blocks
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

        // All dream dimensions
        val DREAM_DIMENSION: RegistryKey<World> = RegistryKey.of(
            RegistryKeys.WORLD, Identifier.of(MOD_ID, "dream_world")
        )
        val STONE_DIMENSION: RegistryKey<World> = RegistryKey.of(
            RegistryKeys.WORLD, Identifier.of(MOD_ID, "stone_world")
        )
        val FLOATING_ISLANDS_DIMENSION: RegistryKey<World> = RegistryKey.of(
            RegistryKeys.WORLD, Identifier.of(MOD_ID, "floating_islands")
        )
        val UPSIDE_DOWN_MOUNTAINS_DIMENSION: RegistryKey<World> = RegistryKey.of(
            RegistryKeys.WORLD, Identifier.of(MOD_ID, "upside_down_mountains")
        )
        val CRYSTAL_CAVES_DIMENSION: RegistryKey<World> = RegistryKey.of(
            RegistryKeys.WORLD, Identifier.of(MOD_ID, "crystal_caves")
        )
        val ENDLESS_OCEAN_DIMENSION: RegistryKey<World> = RegistryKey.of(
            RegistryKeys.WORLD, Identifier.of(MOD_ID, "endless_ocean")
        )
        val NIGHTMARE_REALM_DIMENSION: RegistryKey<World> = RegistryKey.of(
            RegistryKeys.WORLD, Identifier.of(MOD_ID, "nightmare_realm")
        )

        // List of all dimensions for random selection
        val ALL_DIMENSIONS = listOf(
            DREAM_DIMENSION,
            STONE_DIMENSION,
            FLOATING_ISLANDS_DIMENSION,
            UPSIDE_DOWN_MOUNTAINS_DIMENSION,
            CRYSTAL_CAVES_DIMENSION,
            ENDLESS_OCEAN_DIMENSION,
            NIGHTMARE_REALM_DIMENSION
        )
    }

    override fun onInitialize() {
        println("Dream World Mod initialized with ${ALL_DIMENSIONS.size} dream dimensions!")

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

            // Randomly select a dimension
            val targetDimension = ALL_DIMENSIONS.random()
            val targetWorld = server.getWorld(targetDimension)

            if (targetWorld != null) {
                // Send dimension-specific message
                val message = getDimensionMessage(targetDimension)
                player.sendMessage(Text.literal(message), true)

                // Get spawn position for the dimension
                val spawnPos = getSpawnPosition(targetDimension, targetWorld)

                // Generate initial structures
                generateInitialStructures(targetWorld, spawnPos, targetDimension)

                // Create TeleportTarget
                val teleportTarget = TeleportTarget(
                    targetWorld,
                    spawnPos,
                    Vec3d.ZERO,
                    0.0f,
                    0.0f,
                    TeleportTarget.PostDimensionTransition {
                        // Post-teleportation effects
                        applyDimensionEffects(player, targetDimension)
                    }
                )

                // Teleport player
                player.teleportTo(teleportTarget)

                val worldName = getDimensionName(targetDimension)
                println("Player ${player.name.string} teleported to $worldName!")
            } else {
                println("Target world not found! Make sure your dimensions are properly registered.")
            }
        }
    }

    private fun getDimensionMessage(dimension: RegistryKey<World>): String {
        return when (dimension) {
            DREAM_DIMENSION -> "§dYou drift into a strange dream..."
            STONE_DIMENSION -> "§7You find yourself in a world of endless stone..."
            FLOATING_ISLANDS_DIMENSION -> "§bYou float among clouds and sky islands..."
            UPSIDE_DOWN_MOUNTAINS_DIMENSION -> "§6Reality inverts as you enter an impossible realm..."
            CRYSTAL_CAVES_DIMENSION -> "§5Crystalline beauty surrounds you in glittering caves..."
            ENDLESS_OCEAN_DIMENSION -> "§9You dive into infinite azure depths..."
            NIGHTMARE_REALM_DIMENSION -> "§4Darkness consumes you as nightmares take form..."
            else -> "§fYou enter a mysterious realm..."
        }
    }

    private fun getDimensionName(dimension: RegistryKey<World>): String {
        return when (dimension) {
            DREAM_DIMENSION -> "dream world"
            STONE_DIMENSION -> "stone world"
            FLOATING_ISLANDS_DIMENSION -> "floating islands"
            UPSIDE_DOWN_MOUNTAINS_DIMENSION -> "upside-down mountains"
            CRYSTAL_CAVES_DIMENSION -> "crystal caves"
            ENDLESS_OCEAN_DIMENSION -> "endless ocean"
            NIGHTMARE_REALM_DIMENSION -> "nightmare realm"
            else -> "unknown world"
        }
    }

    private fun getSpawnPosition(dimension: RegistryKey<World>, world: ServerWorld): Vec3d {
        return when (dimension) {
            FLOATING_ISLANDS_DIMENSION -> Vec3d(0.5, 120.0, 0.5) // High in the sky
            UPSIDE_DOWN_MOUNTAINS_DIMENSION -> Vec3d(0.5, 80.0, 0.5) // Mid-level for platforms
            CRYSTAL_CAVES_DIMENSION -> Vec3d(0.5, 30.0, 0.5) // Underground feel
            ENDLESS_OCEAN_DIMENSION -> Vec3d(0.5, 65.0, 0.5) // Just above water
            NIGHTMARE_REALM_DIMENSION -> Vec3d(0.5, 30.0, 0.5) // Ground level in darkness
            else -> Vec3d(0.5, 65.0, 0.5) // Default spawn height
        }
    }

    private fun generateInitialStructures(world: ServerWorld, spawnPos: Vec3d, dimension: RegistryKey<World>) {
        val centerPos = BlockPos.ofFloored(spawnPos)

        when (dimension) {
            FLOATING_ISLANDS_DIMENSION -> generateFloatingIslands(world, centerPos)
            UPSIDE_DOWN_MOUNTAINS_DIMENSION -> generateUpsideDownStructures(world, centerPos)
            CRYSTAL_CAVES_DIMENSION -> generateCrystalStructures(world, centerPos)
            ENDLESS_OCEAN_DIMENSION -> generateOceanStructures(world, centerPos)
            NIGHTMARE_REALM_DIMENSION -> generateNightmareStructures(world, centerPos)
        }
    }

    private fun generateFloatingIslands(world: ServerWorld, center: BlockPos) {
        // Generate main spawn island
        generateIsland(world, center.add(0, -5, 0), 15, Blocks.GRASS_BLOCK)

        // Generate surrounding floating islands
        for (i in 0..5) {
            val angle = (i * 60.0) * Math.PI / 180.0
            val distance = 30 + Random.nextInt(20)
            val x = (center.x + distance * Math.cos(angle)).toInt()
            val z = (center.z + distance * Math.sin(angle)).toInt()
            val y = center.y + Random.nextInt(40) - 20

            generateIsland(world, BlockPos(x, y, z), 8 + Random.nextInt(8),
                listOf(Blocks.GRASS_BLOCK, Blocks.STONE, Blocks.MOSS_BLOCK).random())

            // Cloud bridges
            generateCloudBridge(world, center, BlockPos(x, y + 5, z))
        }

        // Add some trees and vegetation
        generateVegetation(world, center)
    }

    private fun generateUpsideDownStructures(world: ServerWorld, center: BlockPos) {
        // Create inverted mountains hanging from above
        for (i in 0..3) {
            val x = center.x + Random.nextInt(60) - 30
            val z = center.z + Random.nextInt(60) - 30
            val topY = 200

            generateInvertedMountain(world, BlockPos(x, topY, z), 20 + Random.nextInt(30))
        }

        // Create floating platforms
        for (i in 0..8) {
            val x = center.x + Random.nextInt(80) - 40
            val z = center.z + Random.nextInt(80) - 40
            val y = center.y + Random.nextInt(60) - 30

            generateFloatingPlatform(world, BlockPos(x, y, z), 5 + Random.nextInt(5))
        }
    }

    private fun generateCrystalStructures(world: ServerWorld, center: BlockPos) {
        val crystalTypes = listOf(
            Blocks.AMETHYST_BLOCK, Blocks.DIAMOND_BLOCK, Blocks.EMERALD_BLOCK,
            Blocks.PRISMARINE, Blocks.SEA_LANTERN, Blocks.GLOWSTONE
        )

        // Generate large crystal formations
        for (i in 0..12) {
            val x = center.x + Random.nextInt(100) - 50
            val z = center.z + Random.nextInt(100) - 50
            val y = center.y + Random.nextInt(40) - 20

            val crystalType = crystalTypes.random()
            generateCrystalFormation(world, BlockPos(x, y, z), crystalType, 8 + Random.nextInt(15))
        }

        // Create glowing paths
        generateGlowingPaths(world, center)
    }

    private fun generateOceanStructures(world: ServerWorld, center: BlockPos) {
        // Generate floating temples
        for (i in 0..3) {
            val x = center.x + Random.nextInt(200) - 100
            val z = center.z + Random.nextInt(200) - 100
            val y = 70 // Above water level

            generateFloatingTemple(world, BlockPos(x, y, z))
        }

        // Generate underwater ruins
        for (i in 0..6) {
            val x = center.x + Random.nextInt(300) - 150
            val z = center.z + Random.nextInt(300) - 150
            val y = 20 + Random.nextInt(30) // Underwater

            generateUnderwaterRuin(world, BlockPos(x, y, z))
        }

        // Create coral gardens
        generateCoralGardens(world, center)
    }

    private fun generateNightmareStructures(world: ServerWorld, center: BlockPos) {
        // Generate dark castle
        generateDarkCastle(world, center.add(0, 0, 30))

        // Generate smaller nightmare structures
        for (i in 0..5) {
            val x = center.x + Random.nextInt(120) - 60
            val z = center.z + Random.nextInt(120) - 60
            val y = center.y + Random.nextInt(20)

            generateNightmareTower(world, BlockPos(x, y, z))
        }

        // Create obsidian spikes
        generateObsidianSpikes(world, center)

        // Add soul fire
        addSoulFire(world, center)
    }

    // Helper methods for structure generation
    private fun generateIsland(world: ServerWorld, center: BlockPos, radius: Int, topBlock: net.minecraft.block.Block) {
        for (x in -radius..radius) {
            for (z in -radius..radius) {
                val distance = Math.sqrt((x * x + z * z).toDouble())
                if (distance <= radius) {
                    val height = (radius - distance).toInt() + 2
                    for (y in 0..height) {
                        val block = if (y == height) topBlock else Blocks.DIRT
                        world.setBlockState(center.add(x, -y, z), block.defaultState)
                    }
                }
            }
        }
    }

    private fun generateCloudBridge(world: ServerWorld, from: BlockPos, to: BlockPos) {
        val dx = to.x - from.x
        val dy = to.y - from.y
        val dz = to.z - from.z
        val length = Math.sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toInt()

        for (i in 0..length) {
            val progress = i.toDouble() / length
            val x = from.x + (dx * progress).toInt()
            val y = from.y + (dy * progress).toInt()
            val z = from.z + (dz * progress).toInt()

            // Create cloud blocks
            world.setBlockState(BlockPos(x, y, z), Blocks.WHITE_WOOL.defaultState)
            if (Random.nextFloat() < 0.3f) {
                world.setBlockState(BlockPos(x, y + 1, z), Blocks.LIGHT_GRAY_WOOL.defaultState)
            }
        }
    }

    private fun generateVegetation(world: ServerWorld, center: BlockPos) {
        for (i in 0..20) {
            val x = center.x + Random.nextInt(30) - 15
            val z = center.z + Random.nextInt(30) - 15
            val y = center.y

            // Find surface
            var surfaceY = y
            while (surfaceY > y - 10 && world.getBlockState(BlockPos(x, surfaceY, z)).isAir) {
                surfaceY--
            }

            if (!world.getBlockState(BlockPos(x, surfaceY, z)).isAir) {
                // Place vegetation
                when (Random.nextInt(4)) {
                    0 -> world.setBlockState(BlockPos(x, surfaceY + 1, z), Blocks.GRASS_BLOCK.defaultState)
                    1 -> world.setBlockState(BlockPos(x, surfaceY + 1, z), Blocks.POPPY.defaultState)
                    2 -> world.setBlockState(BlockPos(x, surfaceY + 1, z), Blocks.DANDELION.defaultState)
                    3 -> {
                        // Small tree
                        world.setBlockState(BlockPos(x, surfaceY + 1, z), Blocks.OAK_LOG.defaultState)
                        world.setBlockState(BlockPos(x, surfaceY + 2, z), Blocks.OAK_LOG.defaultState)
                        for (dx in -1..1) {
                            for (dz in -1..1) {
                                for (dy in 0..1) {
                                    world.setBlockState(BlockPos(x + dx, surfaceY + 2 + dy, z + dz), Blocks.OAK_LEAVES.defaultState)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun generateInvertedMountain(world: ServerWorld, top: BlockPos, height: Int) {
        val radius = height / 2
        for (y in 0..height) {
            val currentRadius = (radius * (height - y) / height.toDouble()).toInt()
            for (x in -currentRadius..currentRadius) {
                for (z in -currentRadius..currentRadius) {
                    val distance = Math.sqrt((x * x + z * z).toDouble())
                    if (distance <= currentRadius) {
                        world.setBlockState(top.add(x, -y, z), Blocks.STONE.defaultState)
                    }
                }
            }
        }
    }

    private fun generateFloatingPlatform(world: ServerWorld, center: BlockPos, size: Int) {
        for (x in -size..size) {
            for (z in -size..size) {
                if (Math.abs(x) + Math.abs(z) <= size) {
                    world.setBlockState(center.add(x, 0, z), Blocks.COBBLESTONE.defaultState)
                    if (Math.abs(x) + Math.abs(z) == size && Random.nextFloat() < 0.2f) {
                        world.setBlockState(center.add(x, 1, z), Blocks.COBBLESTONE_WALL.defaultState)
                    }
                }
            }
        }
    }

    private fun generateCrystalFormation(world: ServerWorld, center: BlockPos, crystalType: net.minecraft.block.Block, height: Int) {
        // Central crystal spire
        for (y in 0..height) {
            world.setBlockState(center.add(0, y, 0), crystalType.defaultState)

            // Add crystal branches
            if (y > height / 3 && Random.nextFloat() < 0.3f) {
                val directions = listOf(
                    intArrayOf(1, 0, 0), intArrayOf(-1, 0, 0),
                    intArrayOf(0, 0, 1), intArrayOf(0, 0, -1)
                )
                val dir = directions.random()
                world.setBlockState(center.add(dir[0], y, dir[2]), crystalType.defaultState)
            }
        }

        // Add smaller crystals around
        for (i in 0..5) {
            val x = Random.nextInt(6) - 3
            val z = Random.nextInt(6) - 3
            val smallHeight = Random.nextInt(height / 2) + 1

            for (y in 0..smallHeight) {
                world.setBlockState(center.add(x, y, z), crystalType.defaultState)
            }
        }
    }

    private fun generateGlowingPaths(world: ServerWorld, center: BlockPos) {
        val pathBlocks = listOf(Blocks.GLOWSTONE, Blocks.SEA_LANTERN, Blocks.SHROOMLIGHT)

        // Create several glowing paths
        for (path in 0..3) {
            val startX = center.x + Random.nextInt(20) - 10
            val startZ = center.z + Random.nextInt(20) - 10
            val endX = center.x + Random.nextInt(60) - 30
            val endZ = center.z + Random.nextInt(60) - 30

            val steps = 50
            for (step in 0..steps) {
                val progress = step.toDouble() / steps
                val x = (startX + (endX - startX) * progress).toInt()
                val z = (startZ + (endZ - startZ) * progress).toInt()
                val y = center.y + Random.nextInt(10) - 5

                world.setBlockState(BlockPos(x, y, z), pathBlocks.random().defaultState)
            }
        }
    }

    private fun generateFloatingTemple(world: ServerWorld, center: BlockPos) {
        // Temple platform
        for (x in -8..8) {
            for (z in -8..8) {
                world.setBlockState(center.add(x, 0, z), Blocks.QUARTZ_BLOCK.defaultState)
            }
        }

        // Temple pillars
        val pillarPositions = listOf(
            intArrayOf(-6, 6), intArrayOf(6, 6), intArrayOf(-6, -6), intArrayOf(6, -6)
        )

        for (pos in pillarPositions) {
            for (y in 1..8) {
                world.setBlockState(center.add(pos[0], y, pos[1]), Blocks.QUARTZ_PILLAR.defaultState)
            }
            // Pillar top
            world.setBlockState(center.add(pos[0], 9, pos[1]), Blocks.CHISELED_QUARTZ_BLOCK.defaultState)
        }

        // Central altar
        for (x in -2..2) {
            for (z in -2..2) {
                world.setBlockState(center.add(x, 1, z), Blocks.SMOOTH_QUARTZ.defaultState)
            }
        }
        world.setBlockState(center.add(0, 2, 0), Blocks.BEACON.defaultState)
    }

    private fun generateUnderwaterRuin(world: ServerWorld, center: BlockPos) {
        val ruinBlocks = listOf(Blocks.STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS)

        // Main structure
        for (x in -5..5) {
            for (z in -5..5) {
                for (y in 0..3) {
                    if (Random.nextFloat() < 0.6f) { // Partial destruction
                        val block = if (y == 0) Blocks.COBBLESTONE else ruinBlocks.random()
                        world.setBlockState(center.add(x, y, z), block.defaultState)
                    }
                }
            }
        }

        // Add some treasure
        world.setBlockState(center.add(0, 1, 0), Blocks.CHEST.defaultState)

        // Kelp and seagrass
        for (i in 0..15) {
            val x = center.x + Random.nextInt(20) - 10
            val z = center.z + Random.nextInt(20) - 10
            val y = center.y + Random.nextInt(5)

            if (Random.nextFloat() < 0.5f) {
                world.setBlockState(BlockPos(x, y, z), Blocks.KELP.defaultState)
            } else {
                world.setBlockState(BlockPos(x, y, z), Blocks.SEAGRASS.defaultState)
            }
        }
    }

    private fun generateCoralGardens(world: ServerWorld, center: BlockPos) {
        val coralTypes = listOf(
            Blocks.TUBE_CORAL_BLOCK, Blocks.BRAIN_CORAL_BLOCK, Blocks.BUBBLE_CORAL_BLOCK,
            Blocks.FIRE_CORAL_BLOCK, Blocks.HORN_CORAL_BLOCK
        )

        // Create coral patches
        for (patch in 0..8) {
            val patchCenterX = center.x + Random.nextInt(100) - 50
            val patchCenterZ = center.z + Random.nextInt(100) - 50
            val coralType = coralTypes.random()

            for (i in 0..20) {
                val x = patchCenterX + Random.nextInt(10) - 5
                val z = patchCenterZ + Random.nextInt(10) - 5
                val y = 40 + Random.nextInt(15) // Underwater level

                world.setBlockState(BlockPos(x, y, z), coralType.defaultState)

                // Add coral fans on top
                if (Random.nextFloat() < 0.4f) {
                    val fanType = when (coralType) {
                        Blocks.TUBE_CORAL_BLOCK -> Blocks.TUBE_CORAL_FAN
                        Blocks.BRAIN_CORAL_BLOCK -> Blocks.BRAIN_CORAL_FAN
                        Blocks.BUBBLE_CORAL_BLOCK -> Blocks.BUBBLE_CORAL_FAN
                        Blocks.FIRE_CORAL_BLOCK -> Blocks.FIRE_CORAL_FAN
                        else -> Blocks.HORN_CORAL_FAN
                    }
                    world.setBlockState(BlockPos(x, y + 1, z), fanType.defaultState)
                }
            }
        }
    }

    private fun generateDarkCastle(world: ServerWorld, center: BlockPos) {
        // Castle base
        for (x in -15..15) {
            for (z in -15..15) {
                for (y in 0..2) {
                    if (Math.abs(x) >= 12 || Math.abs(z) >= 12 || y == 0) {
                        world.setBlockState(center.add(x, y, z), Blocks.BLACKSTONE.defaultState)
                    }
                }
            }
        }

        // Castle towers
        val towerPositions = listOf(
            intArrayOf(-12, -12), intArrayOf(12, -12), intArrayOf(-12, 12), intArrayOf(12, 12)
        )

        for (pos in towerPositions) {
            for (y in 3..20) {
                for (x in -2..2) {
                    for (z in -2..2) {
                        if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                            world.setBlockState(center.add(pos[0] + x, y, pos[1] + z), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultState)
                        }
                    }
                }
            }

            // Tower top
            for (x in -3..3) {
                for (z in -3..3) {
                    if (Math.abs(x) == 3 || Math.abs(z) == 3) {
                        world.setBlockState(center.add(pos[0] + x, 21, pos[1] + z), Blocks.POLISHED_BLACKSTONE.defaultState)
                    }
                }
            }
        }

        // Central keep
        for (y in 3..15) {
            for (x in -5..5) {
                for (z in -5..5) {
                    if (Math.abs(x) == 5 || Math.abs(z) == 5) {
                        world.setBlockState(center.add(x, y, z), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultState)
                    }
                }
            }
        }

        // Entrance
        for (y in 1..3) {
            world.setBlockState(center.add(0, y, 15), Blocks.AIR.defaultState)
            world.setBlockState(center.add(1, y, 15), Blocks.AIR.defaultState)
            world.setBlockState(center.add(-1, y, 15), Blocks.AIR.defaultState)
        }
    }

    private fun generateNightmareTower(world: ServerWorld, center: BlockPos) {
        val height = 8 + Random.nextInt(12)
        val radius = 3 + Random.nextInt(3)

        for (y in 0..height) {
            val currentRadius = if (y < height / 2) radius else radius - 1
            for (x in -currentRadius..currentRadius) {
                for (z in -currentRadius..currentRadius) {
                    val distance = Math.sqrt((x * x + z * z).toDouble())
                    if (distance <= currentRadius && (distance >= currentRadius - 1 || y == 0)) {
                        val block = if (Random.nextFloat() < 0.8f) Blocks.BLACKSTONE else Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS
                        world.setBlockState(center.add(x, y, z), block.defaultState)
                    }
                }
            }
        }

        // Add some windows with soul fire
        for (y in 2..height step 3) {
            val direction = Random.nextInt(4)
            val x = if (direction % 2 == 0) (if (direction == 0) radius else -radius) else 0
            val z = if (direction % 2 == 1) (if (direction == 1) radius else -radius) else 0

            world.setBlockState(center.add(x, y, z), Blocks.AIR.defaultState)
            world.setBlockState(center.add(x, y + 1, z), Blocks.AIR.defaultState)
            if (Random.nextFloat() < 0.6f) {
                world.setBlockState(center.add(x / 2, y, z / 2), Blocks.SOUL_FIRE.defaultState)
            }
        }
    }

    private fun generateObsidianSpikes(world: ServerWorld, center: BlockPos) {
        for (i in 0..15) {
            val x = center.x + Random.nextInt(80) - 40
            val z = center.z + Random.nextInt(80) - 40
            val height = 5 + Random.nextInt(15)

            for (y in 0..height) {
                world.setBlockState(BlockPos(x, center.y + y, z), Blocks.OBSIDIAN.defaultState)

                // Taper the spike
                if (y > height * 0.7 && Random.nextFloat() < 0.3f) {
                    break
                }
            }
        }
    }

    private fun addSoulFire(world: ServerWorld, center: BlockPos) {
        for (i in 0..25) {
            val x = center.x + Random.nextInt(60) - 30
            val z = center.z + Random.nextInt(60) - 30
            val y = center.y + Random.nextInt(10)

            // Find a suitable surface
            var surfaceY = y
            while (surfaceY > center.y - 5 && world.getBlockState(BlockPos(x, surfaceY, z)).isAir) {
                surfaceY--
            }

            if (!world.getBlockState(BlockPos(x, surfaceY, z)).isAir) {
                world.setBlockState(BlockPos(x, surfaceY + 1, z), Blocks.SOUL_FIRE.defaultState)
            }
        }
    }

    private fun applyDimensionEffects(player: ServerPlayerEntity, dimension: RegistryKey<World>) {
        // Apply dimension-specific effects to the player
        when (dimension) {
            NIGHTMARE_REALM_DIMENSION -> {
                // Could add weakness or other negative effects for nightmare realm
                player.sendMessage(Text.literal("§4The shadows whisper of ancient evils..."), false)
            }
            CRYSTAL_CAVES_DIMENSION -> {
                // Could add night vision or glowing effect
                player.sendMessage(Text.literal("§5The crystals illuminate your path..."), false)
            }
            FLOATING_ISLANDS_DIMENSION -> {
                // Could add slow falling effect
                player.sendMessage(Text.literal("§bThe winds carry you gently..."), false)
            }
            ENDLESS_OCEAN_DIMENSION -> {
                // Could add water breathing
                player.sendMessage(Text.literal("§9The waters welcome you..."), false)
            }
        }
    }

    private fun isNight(world: World): Boolean {
        val time = world.timeOfDay % 24000L
        // Night time roughly between 13000 and 23000 ticks
        return time in 13000L..23000L
    }
}