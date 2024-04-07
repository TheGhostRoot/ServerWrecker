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
package com.soulfiremc.server.protocol.bot;

import com.github.steveice10.mc.protocol.data.game.entity.RotationOrigin;
import com.github.steveice10.mc.protocol.data.game.entity.object.Direction;
import com.github.steveice10.mc.protocol.data.game.entity.player.Hand;
import com.github.steveice10.mc.protocol.data.game.entity.player.PlayerAction;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.player.ServerboundPlayerActionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.player.ServerboundSwingPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.player.ServerboundUseItemOnPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.player.ServerboundUseItemPacket;
import com.soulfiremc.server.data.BlockState;
import com.soulfiremc.server.pathfinding.SFVec3i;
import com.soulfiremc.server.protocol.BotConnection;
import com.soulfiremc.server.protocol.bot.movement.AABB;
import java.util.ArrayList;
import java.util.Optional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.cloudburstmc.math.vector.Vector3d;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.jetbrains.annotations.NotNull;

/**
 * Manages mostly block and interaction related stuff that requires to keep track of sequence
 * numbers.
 */
@Data
@RequiredArgsConstructor
public class BotActionManager {
  @ToString.Exclude
  private final SessionDataManager dataManager;
  @ToString.Exclude
  private final BotConnection connection;
  private int sequenceNumber = 0;

  private static Optional<Vector3f> rayCastToBlock(
    BlockState blockState, Vector3d eyePosition, Vector3d headRotation, Vector3i targetBlock) {
    var intersections = new ArrayList<Vector3f>();

    for (var shape : blockState.getCollisionBoxes(targetBlock)) {
      shape
        .getIntersection(eyePosition, headRotation)
        .map(Vector3d::toFloat)
        .ifPresent(intersections::add);
    }

    if (intersections.isEmpty()) {
      return Optional.empty();
    }

    Vector3f closestIntersection = null;
    var closestDistance = Double.MAX_VALUE;

    for (var intersection : intersections) {
      double distance =
        intersection.distance(eyePosition.getX(), eyePosition.getY(), eyePosition.getZ());

      if (distance < closestDistance) {
        closestIntersection = intersection;
        closestDistance = distance;
      }
    }

    assert closestIntersection != null;
    return Optional.of(closestIntersection);
  }

  public static Vector3d getMiddleBlockFace(Vector3i blockPos, Direction blockFace) {
    var blockPosDouble = blockPos.toDouble();
    return switch (blockFace) {
      case DOWN -> blockPosDouble.add(0.5, 0, 0.5);
      case UP -> blockPosDouble.add(0.5, 1, 0.5);
      case NORTH -> blockPosDouble.add(0.5, 0.5, 0);
      case SOUTH -> blockPosDouble.add(0.5, 0.5, 1);
      case WEST -> blockPosDouble.add(0, 0.5, 0.5);
      case EAST -> blockPosDouble.add(1, 0.5, 0.5);
    };
  }

  public void incrementSequenceNumber() {
    sequenceNumber++;
  }

  public void useItemInHand(Hand hand) {
    incrementSequenceNumber();
    connection.sendPacket(new ServerboundUseItemPacket(hand, sequenceNumber));
  }

  public void placeBlock(Hand hand, BlockPlaceData blockPlaceData) {
    placeBlock(hand, blockPlaceData.againstPos().toVector3i(), blockPlaceData.blockFace());
  }

  public void placeBlock(Hand hand, Vector3i againstBlock, Direction againstFace) {
    incrementSequenceNumber();
    var clientEntity = dataManager.clientEntity();
    var level = dataManager.currentLevel();

    var eyePosition = clientEntity.eyePosition();

    var againstPlacePosition = getMiddleBlockFace(againstBlock, againstFace);

    var previousYaw = clientEntity.yaw();
    var previousPitch = clientEntity.pitch();
    clientEntity.lookAt(RotationOrigin.EYES, againstPlacePosition);
    if (previousPitch != clientEntity.pitch() || previousYaw != clientEntity.yaw()) {
      clientEntity.sendRot();
    }

    var rayCast =
      rayCastToBlock(
        level.getBlockStateAt(againstBlock),
        eyePosition,
        clientEntity.rotationVector(),
        againstBlock);
    if (rayCast.isEmpty()) {
      return;
    }

    var rayCastPosition = rayCast.get().sub(againstBlock.toFloat());
    var insideBlock = !level.getCollisionBoxes(new AABB(eyePosition, eyePosition)).isEmpty();

    connection.sendPacket(
      new ServerboundUseItemOnPacket(
        againstBlock,
        againstFace,
        hand,
        rayCastPosition.getX(),
        rayCastPosition.getY(),
        rayCastPosition.getZ(),
        insideBlock,
        sequenceNumber));
  }

  public void sendStartBreakBlock(Vector3i blockPos) {
    incrementSequenceNumber();
    var blockFace = getBlockFaceLookedAt(blockPos);
    connection.sendPacket(
      new ServerboundPlayerActionPacket(
        PlayerAction.START_DIGGING, blockPos, blockFace, sequenceNumber));
  }

  public void sendEndBreakBlock(Vector3i blockPos) {
    incrementSequenceNumber();
    var blockFace = getBlockFaceLookedAt(blockPos);
    connection.sendPacket(
      new ServerboundPlayerActionPacket(
        PlayerAction.FINISH_DIGGING, blockPos, blockFace, sequenceNumber));
  }

  public @NotNull Direction getBlockFaceLookedAt(Vector3i blockPos) {
    var clientEntity = dataManager.clientEntity();
    var eyePosition = clientEntity.eyePosition();
    var headRotation = clientEntity.rotationVector();
    var blockPosDouble = blockPos.toDouble();
    var blockBoundingBox = new AABB(blockPosDouble, blockPosDouble.add(1, 1, 1));
    var intersection =
      blockBoundingBox.getIntersection(eyePosition, headRotation).map(Vector3d::toFloat);
    if (intersection.isEmpty()) {
      throw new IllegalStateException("Block is not in line of sight");
    }

    var intersectionFloat = intersection.get();
    var blockPosFloat = blockPos.toFloat();
    var relativeIntersection = intersectionFloat.sub(blockPosFloat);

    // Check side the intersection is the closest to
    if (relativeIntersection.getX() > relativeIntersection.getY()
      && relativeIntersection.getX() > relativeIntersection.getZ()) {
      return intersectionFloat.getX() > blockPosFloat.getX() ? Direction.EAST : Direction.WEST;
    } else if (relativeIntersection.getY() > relativeIntersection.getZ()) {
      return intersectionFloat.getY() > blockPosFloat.getY() ? Direction.UP : Direction.DOWN;
    } else {
      return intersectionFloat.getZ() > blockPosFloat.getZ() ? Direction.SOUTH : Direction.NORTH;
    }
  }

  public void sendBreakBlockAnimation() {
    connection.sendPacket(new ServerboundSwingPacket(Hand.MAIN_HAND));
  }

  public record BlockPlaceData(SFVec3i againstPos, Direction blockFace) {}
}
