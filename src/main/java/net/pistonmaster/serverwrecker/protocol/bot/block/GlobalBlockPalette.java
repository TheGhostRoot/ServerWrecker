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
package net.pistonmaster.serverwrecker.protocol.bot.block;

import com.viaversion.viaversion.libs.fastutil.objects.Object2ObjectArrayMap;
import lombok.Getter;
import lombok.ToString;
import net.pistonmaster.serverwrecker.protocol.bot.state.ChunkData;

import java.util.Map;

@ToString
public class GlobalBlockPalette {
    @Getter
    private final int maxStates;
    @Getter
    private final int blockBitsPerEntry;
    private final String[] stateIdToBlockName;
    private final Map<String, Integer> blockNameToStateId;

    public GlobalBlockPalette(int maxStates) {
        this.maxStates = maxStates;
        this.blockBitsPerEntry = ChunkData.log2RoundUp(maxStates);
        stateIdToBlockName = new String[maxStates];
        blockNameToStateId = new Object2ObjectArrayMap<>(maxStates);
    }

    public void add(int id, String name) {
        stateIdToBlockName[id] = name;
        blockNameToStateId.put(name, id);
    }

    public String getBlockNameForStateId(int id) {
        return stateIdToBlockName[id];
    }
}
