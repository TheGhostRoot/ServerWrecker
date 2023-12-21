/*
 * ServerWrecker
 *
 * Copyright (C) 2023 ServerWrecker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 */
package net.pistonmaster.serverwrecker.server.protocol.bot.state.entity;

import com.github.steveice10.mc.protocol.data.game.entity.EntityEvent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.pistonmaster.serverwrecker.server.data.EntityType;
import net.pistonmaster.serverwrecker.server.protocol.bot.SessionDataManager;
import net.pistonmaster.serverwrecker.server.protocol.bot.movement.BotMovementManager;
import net.pistonmaster.serverwrecker.server.protocol.bot.movement.ControlState;
import net.pistonmaster.serverwrecker.server.protocol.bot.movement.PlayerMovementState;

/**
 * Represents the bot itself as an entity.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ClientEntity extends Entity {
    private final SessionDataManager sessionDataManager;
    private final ControlState controlState;
    private final PlayerMovementState movementState;
    private final BotMovementManager botMovementManager;
    private boolean showReducedDebug;
    private int opPermissionLevel;

    public ClientEntity(int entityId, SessionDataManager sessionDataManager, ControlState controlState) {
        super(entityId, EntityType.PLAYER);
        this.sessionDataManager = sessionDataManager;
        this.controlState = controlState;
        this.movementState = new PlayerMovementState(this, sessionDataManager.inventoryManager().getPlayerInventory());
        this.botMovementManager = new BotMovementManager(sessionDataManager, movementState, this);
        yaw(-180);
    }

    @Override
    public void tick() {
        super.tick();

        movementState.updateData();

        // Tick physics movement
        var level = sessionDataManager.getCurrentLevel();
        if (level != null && level.isChunkLoaded(this.blockPos())) {
            botMovementManager.tick();
        }

        movementState.applyData();
    }

    public void handleEntityEvent(EntityEvent event) {
        switch (event) {
            case PLAYER_ENABLE_REDUCED_DEBUG -> showReducedDebug = true;
            case PLAYER_DISABLE_REDUCED_DEBUG -> showReducedDebug = false;
            case PLAYER_OP_PERMISSION_LEVEL_0 -> opPermissionLevel = 0;
            case PLAYER_OP_PERMISSION_LEVEL_1 -> opPermissionLevel = 1;
            case PLAYER_OP_PERMISSION_LEVEL_2 -> opPermissionLevel = 2;
            case PLAYER_OP_PERMISSION_LEVEL_3 -> opPermissionLevel = 3;
            case PLAYER_OP_PERMISSION_LEVEL_4 -> opPermissionLevel = 4;
            default -> super.handleEntityEvent(event);
        }
    }

    @Override
    public double getEyeHeight() {
        return this.controlState.sneaking() ? 1.50F : 1.62F;
    }
}
