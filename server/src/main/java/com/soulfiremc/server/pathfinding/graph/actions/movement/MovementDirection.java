/*
 * SoulFire
 * Copyright (C) 2024  AlexProgrammerDE
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.soulfiremc.server.pathfinding.graph.actions.movement;

import com.soulfiremc.server.pathfinding.SFVec3i;
import com.soulfiremc.server.pathfinding.graph.BlockFace;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MovementDirection {
  NORTH,
  SOUTH,
  EAST,
  WEST,
  NORTH_EAST,
  NORTH_WEST,
  SOUTH_EAST,
  SOUTH_WEST;

  public static final MovementDirection[] VALUES = values();

  public SkyDirection side(MovementSide side) {
    return switch (this) {
      case NORTH_EAST -> switch (side) {
        case LEFT -> SkyDirection.NORTH;
        case RIGHT -> SkyDirection.EAST;
      };
      case NORTH_WEST -> switch (side) {
        case LEFT -> SkyDirection.NORTH;
        case RIGHT -> SkyDirection.WEST;
      };
      case SOUTH_EAST -> switch (side) {
        case LEFT -> SkyDirection.SOUTH;
        case RIGHT -> SkyDirection.EAST;
      };
      case SOUTH_WEST -> switch (side) {
        case LEFT -> SkyDirection.SOUTH;
        case RIGHT -> SkyDirection.WEST;
      };
      default -> throw new IllegalStateException("Unexpected value: " + this);
    };
  }

  public SkyDirection toSkyDirection() {
    return switch (this) {
      case NORTH -> SkyDirection.NORTH;
      case SOUTH -> SkyDirection.SOUTH;
      case EAST -> SkyDirection.EAST;
      case WEST -> SkyDirection.WEST;
      default -> throw new IllegalStateException("Unexpected value: " + this);
    };
  }

  public BlockFace toBlockFace() {
    return switch (this) {
      case NORTH -> BlockFace.NORTH;
      case SOUTH -> BlockFace.SOUTH;
      case EAST -> BlockFace.EAST;
      case WEST -> BlockFace.WEST;
      default -> throw new IllegalStateException("Unexpected value: " + this);
    };
  }

  public SFVec3i offset(SFVec3i vector) {
    return switch (this) {
      case NORTH -> vector.add(0, 0, -1);
      case SOUTH -> vector.add(0, 0, 1);
      case EAST -> vector.add(1, 0, 0);
      case WEST -> vector.add(-1, 0, 0);
      case NORTH_EAST -> vector.add(1, 0, -1);
      case NORTH_WEST -> vector.add(-1, 0, -1);
      case SOUTH_EAST -> vector.add(1, 0, 1);
      case SOUTH_WEST -> vector.add(-1, 0, 1);
    };
  }

  public boolean isDiagonal() {
    return this == NORTH_EAST || this == NORTH_WEST || this == SOUTH_EAST || this == SOUTH_WEST;
  }
}
