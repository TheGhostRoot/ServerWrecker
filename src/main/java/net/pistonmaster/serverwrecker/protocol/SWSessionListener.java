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
package net.pistonmaster.serverwrecker.protocol;

import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundDisconnectPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundPlayerChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.ClientboundSetEntityMotionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.player.ClientboundSetHealthPacket;
import com.github.steveice10.mc.protocol.packet.login.clientbound.ClientboundLoginDisconnectPacket;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.packet.Packet;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.pistonmaster.serverwrecker.common.GameMode;

@RequiredArgsConstructor
public class SWSessionListener extends SessionAdapter {
    private final SessionEventBus bus;
    private final MinecraftProtocol wrapper;

    @Override
    public void packetReceived(Session session, Packet packet) {
        if (packet instanceof ClientboundPlayerChatPacket chatPacket) {
            Component message = chatPacket.getUnsignedContent();
            if (message == null) {
                message = Component.text(chatPacket.getContent());
            }
            bus.onChat(toPlainText(message));
        } else if (packet instanceof ClientboundSystemChatPacket systemChatPacket) {
            Component message = systemChatPacket.getContent();
            bus.onChat(toPlainText(message));
        } else if (packet instanceof ClientboundPlayerPositionPacket posPacket) {
            bus.onPosition(posPacket.getX(), posPacket.getY(), posPacket.getZ(), posPacket.getYaw(), posPacket.getPitch());
        } else if (packet instanceof ClientboundSetHealthPacket healthPacket) {
            bus.onHealth(healthPacket.getHealth(), healthPacket.getFood(), healthPacket.getSaturation());
        } else if (packet instanceof ClientboundLoginPacket playLoginPacket) {
            bus.onJoin(playLoginPacket.getEntityId(),
                    playLoginPacket.isHardcore(),
                    GameMode.valueOf(playLoginPacket.getGameMode().name()),
                    playLoginPacket.getMaxPlayers());
        } else if (packet instanceof ClientboundDisconnectPacket disconnectPacket) {
            bus.onDisconnectPacket(toPlainText(disconnectPacket.getReason()));
        } else if (packet instanceof ClientboundLoginDisconnectPacket loginDisconnectPacket) {
            bus.onLoginDisconnectPacket(toPlainText(loginDisconnectPacket.getReason()));
        } else if (packet instanceof ClientboundSetEntityMotionPacket motionPacket) {
            bus.onEntityMotion(motionPacket.getEntityId(),
                    motionPacket.getMotionX(),
                    motionPacket.getMotionY(),
                    motionPacket.getMotionZ());
        }
    }

    @Override
    public void disconnected(DisconnectedEvent event) {
        bus.onDisconnectEvent(toPlainText(event.getReason()), event.getCause());
    }

    private String toPlainText(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}
