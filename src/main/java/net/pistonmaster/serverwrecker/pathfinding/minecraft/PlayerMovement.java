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
package net.pistonmaster.serverwrecker.pathfinding.minecraft;

import org.cloudburstmc.math.vector.Vector3d;

public record PlayerMovement(Vector3d from, BasicMovementAction action) implements MinecraftAction {
    @Override
    public Vector3d getTargetPos() {
        return switch (action) {
            case NORTH -> from.add(0, 0, -1);
            case SOUTH -> from.add(0, 0, 1);
            case EAST -> from.add(1, 0, 0);
            case WEST -> from.add(-1, 0, 0);
            case NORTH_EAST -> from.add(1, 0, -1);
            case NORTH_WEST -> from.add(-1, 0, -1);
            case SOUTH_EAST -> from.add(1, 0, 1);
            case SOUTH_WEST -> from.add(-1, 0, 1);
        };
    }
}