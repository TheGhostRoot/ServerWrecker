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
package net.pistonmaster.serverwrecker.pathfinding.graph.actions.movement;

import lombok.RequiredArgsConstructor;
import org.cloudburstmc.math.vector.Vector3i;

@RequiredArgsConstructor
public enum BodyPart {
    FEET,
    HEAD;

    // Iterating over BodyPart.values() is slower than iteration over a static array
    // Reversed because we normally want to see the head block mined before the feet
    public static final BodyPart[] BODY_PARTS_REVERSE = new BodyPart[]{
            BodyPart.HEAD,
            BodyPart.FEET
    };

    public Vector3i offset(Vector3i position) {
        return switch (this) {
            case FEET -> position;
            case HEAD -> position.add(0, 1, 0);
        };
    }
}