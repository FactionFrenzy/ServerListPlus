/*
 * ServerListPlus - https://git.io/slp
 * Copyright (C) 2014 Minecrell (https://github.com/Minecrell)
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.minecrell.serverlistplus.sponge;

import net.minecrell.serverlistplus.core.player.PlayerIdentity;
import net.minecrell.serverlistplus.core.player.ban.BanProvider;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.ban.Ban;
import org.spongepowered.api.service.ban.BanService;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class SpongeBanProvider implements BanProvider {

    private static Optional<BanService> getBanService() {
        return Sponge.server().serviceProvider().provide(BanService.class);
    }

    private static GameProfile getGameProfile(PlayerIdentity playerIdentity) {
        return GameProfile.of(playerIdentity.getUuid(), playerIdentity.getName());
    }

    private static Optional<Ban.Profile> getBan(PlayerIdentity playerIdentity) {
        final GameProfile profile = getGameProfile(playerIdentity);

        return getBanService().flatMap(banService -> {
            try {
                return banService.find(profile).get();
            } catch (InterruptedException | ExecutionException e) {
                return Optional.empty();
            }
        });
    }

    @Override
    public boolean isBanned(PlayerIdentity playerIdentity) {
        return getBan(playerIdentity).isPresent();
    }

    @Override
    public String getBanReason(PlayerIdentity playerIdentity) {
        return getBan(playerIdentity).flatMap(Ban.Profile::reason).map(SpongePlugin.LEGACY_SERIALIZER::serialize).orElse(null);
    }

    @Override
    public String getBanOperator(PlayerIdentity playerIdentity) {
        return getBan(playerIdentity).flatMap(Ban.Profile::banSource).map(SpongePlugin.LEGACY_SERIALIZER::serialize).orElse(null);
    }

    @Override
    public Date getBanExpiration(PlayerIdentity playerIdentity) {
        return getBan(playerIdentity).flatMap(Ban.Profile::expirationDate).map(Date::from).orElse(null);
    }

}
