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
package net.pistonmaster.serverwrecker.server.data;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class BlockStateLoader {
    public static final Map<String, List<BlockShapeType>> BLOCK_SHAPES = new Object2ObjectArrayMap<>();

    static {
        try (var inputStream = BlockShapeType.class.getClassLoader().getResourceAsStream("minecraft/blockstates.txt")) {
            if (inputStream == null) {
                throw new IllegalStateException("blockstates.txt not found!");
            }

            new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).lines().forEach(line -> {
                var parts = line.split("\\|");
                var name = parts[0];

                var blockShapeTypes = new ObjectArrayList<BlockShapeType>();
                if (parts.length > 1) {
                    var part = parts[1];

                    var subParts = part.split(",");
                    for (var subPart : subParts) {
                        var id = Integer.parseInt(subPart);
                        var blockShapeType = BlockShapeType.getById(id);
                        blockShapeTypes.add(blockShapeType);
                    }
                }

                BLOCK_SHAPES.put(name, blockShapeTypes);
            });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static List<BlockShapeType> getBlockShapes(String name) {
        return BLOCK_SHAPES.get(name);
    }
}