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

import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import lombok.AccessLevel;
import lombok.With;
import net.kyori.adventure.key.Key;

@SuppressWarnings("unused")
@With(value = AccessLevel.PRIVATE)
public record EffectType(int id, Key key, EffectCategory category, boolean beneficial, boolean instantenous) {
  public static final Int2ReferenceMap<EffectType> FROM_ID = new Int2ReferenceOpenHashMap<>();
  public static final Object2ReferenceMap<Key, EffectType> FROM_KEY = new Object2ReferenceOpenHashMap<>();

  //@formatter:off
  // VALUES REPLACE
  //@formatter:on

  public static EffectType register(String key) {
    var instance =
      GsonDataHelper.fromJson("/minecraft/effects.json", key, EffectType.class);

    FROM_ID.put(instance.id(), instance);
    FROM_KEY.put(instance.key(), instance);
    return instance;
  }

  public static EffectType getById(int id) {
    return FROM_ID.get(id);
  }

  public static EffectType getByKey(Key key) {
    return FROM_KEY.get(key);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EffectType other)) {
      return false;
    }
    return id == other.id;
  }

  @Override
  public int hashCode() {
    return id;
  }

  public enum EffectCategory {
    BENEFICIAL,
    HARMFUL,
    NEUTRAL
  }
}
