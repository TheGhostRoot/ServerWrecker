/*
 * ServerWrecker
 *
 * Copyright (C) 2022 ServerWrecker
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
package net.pistonmaster.serverwrecker.viaversion.platform;

import lombok.RequiredArgsConstructor;
import net.pistonmaster.serverwrecker.viaversion.JLoggerToLogback;
import net.raphimc.viaaprilfools.platform.ViaAprilFoolsPlatform;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class SWViaAprilFools implements ViaAprilFoolsPlatform {
    private final JLoggerToLogback logger = new JLoggerToLogback(LoggerFactory.getLogger("ViaAprilFools"));
    private final Path dataFolder;

    @Override
    public Logger getLogger() {
        return logger;
    }

    public void init() {
        init(getDataFolder());
    }

    @Override
    public File getDataFolder() {
        return dataFolder.toFile();
    }
}
