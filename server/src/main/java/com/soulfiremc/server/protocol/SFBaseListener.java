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
package com.soulfiremc.server.protocol;

import com.soulfiremc.server.protocol.netty.ViaClientSession;
import com.soulfiremc.server.viaversion.SFVersionConstants;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.raphimc.vialegacy.protocols.release.protocol1_7_2_5to1_6_4.storage.ProtocolMetadataStorage;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.ConnectedEvent;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;
import org.geysermc.mcprotocollib.protocol.data.ProtocolState;
import org.geysermc.mcprotocollib.protocol.data.handshake.HandshakeIntent;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundDisconnectPacket;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundKeepAlivePacket;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundPingPacket;
import org.geysermc.mcprotocollib.protocol.packet.common.serverbound.ServerboundKeepAlivePacket;
import org.geysermc.mcprotocollib.protocol.packet.common.serverbound.ServerboundPongPacket;
import org.geysermc.mcprotocollib.protocol.packet.configuration.clientbound.ClientboundFinishConfigurationPacket;
import org.geysermc.mcprotocollib.protocol.packet.configuration.clientbound.ClientboundSelectKnownPacks;
import org.geysermc.mcprotocollib.protocol.packet.configuration.serverbound.ServerboundFinishConfigurationPacket;
import org.geysermc.mcprotocollib.protocol.packet.configuration.serverbound.ServerboundSelectKnownPacks;
import org.geysermc.mcprotocollib.protocol.packet.handshake.serverbound.ClientIntentionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundStartConfigurationPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundConfigurationAcknowledgedPacket;
import org.geysermc.mcprotocollib.protocol.packet.login.clientbound.ClientboundGameProfilePacket;
import org.geysermc.mcprotocollib.protocol.packet.login.clientbound.ClientboundHelloPacket;
import org.geysermc.mcprotocollib.protocol.packet.login.clientbound.ClientboundLoginCompressionPacket;
import org.geysermc.mcprotocollib.protocol.packet.login.clientbound.ClientboundLoginDisconnectPacket;
import org.geysermc.mcprotocollib.protocol.packet.login.serverbound.ServerboundHelloPacket;
import org.geysermc.mcprotocollib.protocol.packet.login.serverbound.ServerboundKeyPacket;
import org.geysermc.mcprotocollib.protocol.packet.login.serverbound.ServerboundLoginAcknowledgedPacket;
import org.geysermc.mcprotocollib.protocol.packet.status.clientbound.ClientboundPongResponsePacket;
import org.geysermc.mcprotocollib.protocol.packet.status.clientbound.ClientboundStatusResponsePacket;
import org.geysermc.mcprotocollib.protocol.packet.status.serverbound.ServerboundPingRequestPacket;
import org.geysermc.mcprotocollib.protocol.packet.status.serverbound.ServerboundStatusRequestPacket;

@RequiredArgsConstructor
public class SFBaseListener extends SessionAdapter {
  private final BotConnection botConnection;
  private final @NonNull ProtocolState targetState;

  @Override
  public void packetReceived(Session session, Packet packet) {
    if (!(session instanceof ViaClientSession viaSession)) {
      throw new IllegalStateException("Session is not a ViaSession!");
    }

    var protocol = (MinecraftProtocol) session.getPacketProtocol();
    if (protocol.getState() == ProtocolState.LOGIN) {
      if (packet instanceof ClientboundHelloPacket helloPacket) {
        var viaUserConnection = session.getFlag(SFProtocolConstants.VIA_USER_CONNECTION);

        var authSupport = botConnection.minecraftAccount().isPremiumJava();
        if (!authSupport) {
          botConnection
            .logger()
            .info(
              "Server sent a encryption request, but we're offline mode. Not authenticating with mojang.");
        }

        var auth = authSupport;
        var isLegacy = SFVersionConstants.isLegacy(botConnection.protocolVersion());
        if (auth && isLegacy) {
          auth =
            Objects.requireNonNull(viaUserConnection.get(ProtocolMetadataStorage.class))
              .authenticate;
        }

        botConnection.logger().debug("Performing mojang request: {}", auth);

        SecretKey key;
        try {
          var gen = KeyGenerator.getInstance("AES");
          gen.init(128);
          key = gen.generateKey();
        } catch (NoSuchAlgorithmException e) {
          throw new IllegalStateException("Failed to generate shared key.", e);
        }

        if (auth) {
          var serverId =
            SFSessionService.getServerId(
              helloPacket.getServerId(), helloPacket.getPublicKey(), key);
          botConnection.joinServerId(serverId, viaSession);
        }

        session.send(
          new ServerboundKeyPacket(helloPacket.getPublicKey(), key, helloPacket.getChallenge()));

        if (!isLegacy) { // Legacy encryption is handled in SFViaEncryptionProvider
          viaSession.enableJavaEncryption(key);
        } else {
          botConnection.logger().debug("Storing legacy secret key.");
          session.setFlag(SFProtocolConstants.ENCRYPTION_SECRET_KEY, key);
        }
      } else if (packet instanceof ClientboundGameProfilePacket) {
        session.send(new ServerboundLoginAcknowledgedPacket());
      } else if (packet instanceof ClientboundLoginDisconnectPacket loginDisconnectPacket) {
        session.disconnect(loginDisconnectPacket.getReason());
      } else if (packet instanceof ClientboundLoginCompressionPacket loginCompressionPacket) {
        viaSession.setCompressionThreshold(loginCompressionPacket.getThreshold());
      }
    } else if (protocol.getState() == ProtocolState.STATUS) {
      if (packet instanceof ClientboundStatusResponsePacket) {
        session.send(new ServerboundPingRequestPacket(System.currentTimeMillis()));
      } else if (packet instanceof ClientboundPongResponsePacket) {
        session.disconnect("Finished");
      }
    } else if (protocol.getState() == ProtocolState.GAME) {
      if (packet instanceof ClientboundKeepAlivePacket keepAlivePacket) {
        session.send(new ServerboundKeepAlivePacket(keepAlivePacket.getPingId()));
      } else if (packet instanceof ClientboundPingPacket pingPacket) {
        session.send(new ServerboundPongPacket(pingPacket.getId()));
      } else if (packet instanceof ClientboundDisconnectPacket disconnectPacket) {
        session.disconnect(disconnectPacket.getReason());
      } else if (packet instanceof ClientboundStartConfigurationPacket) {
        session.send(new ServerboundConfigurationAcknowledgedPacket());
      }
    } else if (protocol.getState() == ProtocolState.CONFIGURATION) {
      if (packet instanceof ClientboundFinishConfigurationPacket) {
        session.send(new ServerboundFinishConfigurationPacket());
      } else if (packet instanceof ClientboundSelectKnownPacks selectKnownPacks) {
        session.send(new ServerboundSelectKnownPacks(BuiltInKnownPackRegistry.INSTANCE
          .getMatchingPacks(selectKnownPacks.getKnownPacks())));
      }
    }
  }

  @Override
  public void packetSent(Session session, Packet packet) {
    var protocol = (MinecraftProtocol) session.getPacketProtocol();
    if (packet instanceof ClientIntentionPacket) {
      // Once the HandshakePacket has been sent, switch to the next protocol mode.
      protocol.setState(this.targetState);

      if (this.targetState == ProtocolState.LOGIN) {
        session.send(
          new ServerboundHelloPacket(
            botConnection.accountName(), botConnection.accountProfileId()));
      } else {
        session.send(new ServerboundStatusRequestPacket());
      }
    }
  }

  @Override
  public void connected(ConnectedEvent event) {
    var protocol = (MinecraftProtocol) event.getSession().getPacketProtocol();
    var originalAddress = botConnection.resolvedAddress().originalAddress();

    event
      .getSession()
      .send(
        new ClientIntentionPacket(
          protocol.getCodec().getProtocolVersion(),
          originalAddress.host(),
          originalAddress.port(),
          switch (this.targetState) {
            case LOGIN -> HandshakeIntent.LOGIN;
            case STATUS -> HandshakeIntent.STATUS;
            default -> throw new IllegalStateException("Unexpected value: " + this.targetState);
          }));
  }
}
