
package com.example.pvzcraft;

import com.mojang.brigadier.Command;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(PvZCraft.MOD_ID)
public class PvZCraft {
    public static final String MOD_ID = "pvzcraft";

    public PvZCraft() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            net.minecraft.commands.Commands.literal("pvz")
                .then(net.minecraft.commands.Commands.literal("arena")
                    .executes(ctx -> {
                        if (!(ctx.getSource().getEntity()
                                instanceof ServerPlayer player)) {
                            return 0;
                        }

                        buildArena(
                            ctx.getSource().getLevel(),
                            player.blockPosition(),
                            player.getDirection()
                        );

                        ctx.getSource().sendSuccess(
                            () -> Component.literal(
                                "PvZCraft: Arena berhasil dibuat!"
                            ),
                            false
                        );

                        return Command.SINGLE_SUCCESS;
                    })
                )
                .then(net.minecraft.commands.Commands.literal("wave")
                    .executes(ctx -> {
                        if (!(ctx.getSource().getEntity()
                                instanceof ServerPlayer player)) {
                            return 0;
                        }

                        ServerLevel level =
                            ctx.getSource().getLevel();

                        Direction forward = player.getDirection();
                        Direction right = forward.getClockWise();

                        BlockPos origin = player.blockPosition()
                            .relative(forward, 15);

                        for (int lane = -2; lane <= 2; lane++) {
                            BlockPos pos = origin.relative(
                                right, lane * 2
                            );

                            Zombie zombie =
                                EntityType.ZOMBIE.create(level);

                            if (zombie != null) {
                                zombie.moveTo(
                                    pos.getX() + 0.5,
                                    pos.getY(),
                                    pos.getZ() + 0.5,
                                    player.getYRot(),
                                    0
                                );

                                level.addFreshEntity(zombie);
                            }
                        }

                        ctx.getSource().sendSuccess(
                            () -> Component.literal(
                                "PvZCraft: 5 zombie muncul!"
                            ),
                            false
                        );

                        return Command.SINGLE_SUCCESS;
                    })
                )
        );
    }

    private static void buildArena(
            ServerLevel level,
            BlockPos playerPos,
            Direction forward) {

        Direction right = forward.getClockWise();
        BlockPos start = playerPos.relative(forward, 4);

        for (int x = 0; x < 25; x++) {
            for (int lane = -5; lane <= 5; lane++) {
                BlockPos pos = start.relative(forward, x)
                    .relative(right, lane);

                level.setBlock(
                    pos.below(),
                    Blocks.DIRT.defaultBlockState(),
                    3
                );

                boolean border = lane == -5 || lane == 5
                    || x == 0 || x == 24;

                level.setBlock(
                    pos,
                    border
                        ? Blocks.OAK_PLANKS.defaultBlockState()
                        : Blocks.GRASS_BLOCK.defaultBlockState(),
                    3
                );

                level.setBlock(
                    pos.above(),
                    Blocks.AIR.defaultBlockState(),
                    3
                );
            }
        }
    }
}
