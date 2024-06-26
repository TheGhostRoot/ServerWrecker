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
package com.soulfiremc.data;

import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.key.Key;

@SuppressWarnings("unused")
public record BlockType(
  int id,
  Key key,
  float destroyTime,
  float explosionResistance,
  boolean air,
  boolean fallingBlock,
  boolean replaceable,
  boolean requiresCorrectToolForDrops,
  FluidType fluidType,
  List<LootPoolEntry> lootTableData,
  OffsetData offsetData,
  BlockStates statesData) implements RegistryValue<BlockType> {
  public static final TypeAdapter<FluidType> CUSTOM_FLUID_TYPE = new TypeAdapter<>() {
    @Override
    public void write(JsonWriter out, FluidType value) throws IOException {
      out.value(value.key().asString());
    }

    @SuppressWarnings("PatternValidation")
    @Override
    public FluidType read(JsonReader in) throws IOException {
      return FluidType.REGISTRY.getByKey(Key.key(in.nextString()));
    }
  };
  public static final Registry<BlockType> REGISTRY = new Registry<>(RegistryKeys.BLOCK);

  //@formatter:off
  // VALUES REPLACE
  //@formatter:on

  public BlockType {
    statesData = BlockStates.fromJsonArray(
      this,
      key,
      GsonDataHelper.fromJson("minecraft/blocks.json", key.toString(), JsonObject.class)
        .getAsJsonArray("states"));
  }

  public static BlockType register(String key) {
    var instance = GsonDataHelper.fromJson("minecraft/blocks.json", key, BlockType.class, Map.of(
      FluidType.class,
      CUSTOM_FLUID_TYPE
    ));

    return REGISTRY.register(instance);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BlockType other)) {
      return false;
    }
    return id == other.id;
  }

  @Override
  public int hashCode() {
    return id;
  }

  public record OffsetData(
    float maxHorizontalOffset, float maxVerticalOffset, OffsetType offsetType) {
    public enum OffsetType {
      XZ,
      XYZ
    }
  }
}
