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
package com.soulfiremc.server.account;

import com.soulfiremc.settings.account.AuthType;
import com.soulfiremc.settings.account.MinecraftAccount;
import com.soulfiremc.settings.account.service.OnlineSimpleJavaData;
import com.soulfiremc.settings.proxy.SFProxy;
import com.soulfiremc.util.GsonInstance;
import com.soulfiremc.util.ReactorHttpHelper;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.netty.ByteBufFlux;

@Slf4j
public final class SFEasyMCAuthService
  implements MCAuthService<SFEasyMCAuthService.EasyMCAuthData> {
  private static final URI AUTHENTICATE_ENDPOINT =
    URI.create("https://api.easymc.io/v1/token/redeem");

  @Override
  public CompletableFuture<MinecraftAccount> login(EasyMCAuthData data, SFProxy proxyData) {
    var request = new AuthenticationRequest(data.altToken);
    return ReactorHttpHelper.createReactorClient(proxyData, true)
      .post()
      .uri(AUTHENTICATE_ENDPOINT)
      .send(ByteBufFlux.fromString(Flux.just(GsonInstance.GSON.toJson(request))))
      .responseSingle(
        (res, content) ->
          content
            .asString()
            .<MinecraftAccount>handle((responseText, sink) -> {
              var response =
                GsonInstance.GSON.fromJson(responseText, TokenRedeemResponse.class);

              if (response.error() != null) {
                log.error("EasyMC has returned a error: {}", response.error());
                sink.error(new RuntimeException(response.error()));
                return;
              }

              if (response.message() != null) {
                log.info(
                  "EasyMC has a message for you (This is not a error): {}",
                  response.message());
              }

              sink.next(new MinecraftAccount(
                AuthType.EASY_MC,
                UUID.fromString(response.uuid()),
                response.mcName(),
                new OnlineSimpleJavaData(response.session(), -1)));
            }))
      .toFuture();
  }

  @Override
  public EasyMCAuthData createData(String data) {
    var split = data.split(":");

    if (split.length != 1) {
      throw new IllegalArgumentException("Invalid data!");
    }

    return new EasyMCAuthData(split[0].trim());
  }

  @Override
  public CompletableFuture<MinecraftAccount> refresh(MinecraftAccount account, SFProxy proxyData) {
    // TODO: Figure out EasyMC refreshing
    return CompletableFuture.completedFuture(account);
  }

  public record EasyMCAuthData(String altToken) {}

  private record AuthenticationRequest(String token) {}

  @SuppressWarnings("unused") // Used by GSON
  @Getter
  private static class TokenRedeemResponse {
    private String mcName;
    private String uuid;
    private String session;
    private String message;
    private String error;
  }
}
