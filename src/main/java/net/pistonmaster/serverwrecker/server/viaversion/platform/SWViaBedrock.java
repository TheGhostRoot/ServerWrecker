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
package net.pistonmaster.serverwrecker.server.viaversion.platform;

import lombok.RequiredArgsConstructor;
import net.pistonmaster.serverwrecker.server.viaversion.JLoggerToSLF4J;
import net.raphimc.viabedrock.platform.ViaBedrockPlatform;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class SWViaBedrock implements ViaBedrockPlatform {
    private final JLoggerToSLF4J logger = new JLoggerToSLF4J(LoggerFactory.getLogger("ViaBedrock"));
    private final Path dataFolder;

    public void init() {
        init(dataFolder.resolve("config.yml").toFile());
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public File getDataFolder() {
        return dataFolder.toFile();
    }
}