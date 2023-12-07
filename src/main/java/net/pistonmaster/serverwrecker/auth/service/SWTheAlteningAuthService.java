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
package net.pistonmaster.serverwrecker.auth.service;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.pistonmaster.serverwrecker.auth.AuthType;
import net.pistonmaster.serverwrecker.auth.HttpHelper;
import net.pistonmaster.serverwrecker.auth.MinecraftAccount;
import net.pistonmaster.serverwrecker.proxy.SWProxy;
import net.pistonmaster.serverwrecker.util.UUIDHelper;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

public final class SWTheAlteningAuthService implements MCAuthService<SWTheAlteningAuthService.TheAlteningAuthData> {
    @SuppressWarnings("HttpUrlsUsage") // The Altening doesn't support encrypted HTTPS
    private static final URI AUTHENTICATE_ENDPOINT = URI.create("http://authserver.thealtening.com/authenticate");
    private static final String PASSWORD = "ServerWreckerIsCool"; // Password doesn't matter for The Altening
    private final Gson gson = new Gson();

    @Override
    public MinecraftAccount login(TheAlteningAuthData data, SWProxy proxyData) throws IOException {
        try (var httpClient = HttpHelper.createMCAuthHttpClient(proxyData)) {
            var request = new AuthenticationRequest(data.altToken, PASSWORD, UUID.randomUUID().toString());
            var httpPost = new HttpPost(AUTHENTICATE_ENDPOINT);
            httpPost.setEntity(new StringEntity(gson.toJson(request), ContentType.APPLICATION_JSON));
            var response = gson.fromJson(EntityUtils.toString(httpClient.execute(httpPost).getEntity()),
                    AuthenticateRefreshResponse.class);

            return new MinecraftAccount(
                    AuthType.THE_ALTENING,
                    response.getSelectedProfile().getName(),
                    new JavaData(
                            UUIDHelper.convertToDashed(response.getSelectedProfile().getId()),
                            response.getAccessToken(),
                            -1
                    ),
                    true
            );
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public TheAlteningAuthData createData(String data) {
        return new TheAlteningAuthData(data);
    }

    public record TheAlteningAuthData(String altToken) {
    }

    private record Agent(String name, int version) {
    }

    @SuppressWarnings("unused") // Used by GSON
    @RequiredArgsConstructor
    private static class AuthenticationRequest {
        private final Agent agent = new Agent("Minecraft", 1);
        private final String username;
        private final String password;
        private final String clientToken;
        private final boolean requestUser = true;
    }

    @SuppressWarnings("unused") // Used by GSON
    @Getter
    private static class AuthenticateRefreshResponse {
        private String accessToken;
        private ResponseGameProfile selectedProfile;
    }

    @SuppressWarnings("unused") // Used by GSON
    @Getter
    private static class ResponseGameProfile {
        private String id;
        private String name;
    }
}
