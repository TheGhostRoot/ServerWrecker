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
package com.soulfiremc.server.pathfinding.controller;

import com.soulfiremc.server.data.BlockState;
import com.soulfiremc.server.data.BlockType;
import com.soulfiremc.server.pathfinding.SFVec3i;
import com.soulfiremc.server.pathfinding.execution.PathExecutor;
import com.soulfiremc.server.pathfinding.goals.BreakBlockPosGoal;
import com.soulfiremc.server.protocol.BotConnection;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cloudburstmc.math.vector.Vector3i;

@Slf4j
@RequiredArgsConstructor
public class CollectBlockController {
  private final Predicate<BlockType> blockTypeChecker;
  private final int requestedAmount;
  private final int maxRadius;
  private int collectedAmount;

  public void start(BotConnection bot) {
    while (collectedAmount < requestedAmount) {
      log.info("Searching for block to collect");
      var blockPos =
        searchWithinRadiusLayered(bot, blockState -> blockTypeChecker.test(blockState.blockType()), maxRadius)
          .orElseThrow(
            () -> new IllegalStateException("Could not find matching block within radius " + maxRadius));

      log.info("Found block to collect at {}", blockPos);

      var pathFuture = new CompletableFuture<Void>();
      PathExecutor.executePathfinding(bot, new BreakBlockPosGoal(SFVec3i.fromInt(blockPos)), pathFuture);

      try {
        pathFuture.get();
        collectedAmount++;
      } catch (Exception e) {
        throw new RuntimeException("Failed to break block at " + blockPos, e);
      }
    }
  }

  public static Optional<Vector3i> searchWithinRadiusLayered(BotConnection botConnection, Predicate<BlockState> checker,
                                                             int maxRadius) {
    var clientEntity = botConnection.sessionDataManager().clientEntity();
    var clientPosition = clientEntity.pos().toInt();
    var level = clientEntity.level();
    var checkedPositions = new HashSet<Vector3i>();

    for (var layer = 1; layer <= maxRadius; layer++) {
      var layerSquared = layer * layer;

      for (var y = -layer; y <= layer; y++) {
        if (level.isOutSideBuildHeight(clientPosition.getY() + y)) {
          continue;
        }

        for (var x = -layer; x <= layer; x++) {
          for (var z = -layer; z <= layer; z++) {
            // Make sure we are within the circle
            if (x * x + y * y + z * z > layerSquared) {
              continue;
            }

            var blockPos = clientPosition.add(x, y, z);
            if (checkedPositions.contains(blockPos)) {
              continue;
            }

            checkedPositions.add(blockPos);
            var blockState = level.getBlockStateAt(blockPos);
            if (checker.test(blockState)) {
              return Optional.of(blockPos);
            }
          }
        }
      }
    }

    return Optional.empty();
  }
}
