/*
 * Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.internal.managers;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.GuildManager;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.cache.UpstreamReference;
import okhttp3.RequestBody;
import org.json.JSONObject;

import javax.annotation.CheckReturnValue;

public class GuildManagerImpl extends ManagerBase<GuildManager> implements GuildManager
{
    protected final UpstreamReference<Guild> guild;

    protected String name;
    protected String region;
    protected Icon icon;
    protected Icon splash;
    protected String afkChannel;
    protected String systemChannel;
    protected int afkTimeout;
    protected int mfaLevel;
    protected int notificationLevel;
    protected int explicitContentLevel;
    protected int verificationLevel;

    public GuildManagerImpl(Guild guild)
    {
        super(guild.getJDA(), Route.Guilds.MODIFY_GUILD.compile(guild.getId()));
        this.guild = new UpstreamReference<>(guild);
        if (isPermissionChecksEnabled())
            checkPermissions();
    }

    @Override
    public Guild getGuild()
    {
        return guild.get();
    }

    @Override
    @CheckReturnValue
    public GuildManagerImpl reset(long fields)
    {
        super.reset(fields);
        if ((fields & NAME) == NAME)
            this.name = null;
        if ((fields & REGION) == REGION)
            this.region = null;
        if ((fields & ICON) == ICON)
            this.icon = null;
        if ((fields & SPLASH) == SPLASH)
            this.splash = null;
        if ((fields & AFK_CHANNEL) == AFK_CHANNEL)
            this.afkChannel = null;
        if ((fields & SYSTEM_CHANNEL) == SYSTEM_CHANNEL)
            this.systemChannel = null;
        return this;
    }

    @Override
    @CheckReturnValue
    public GuildManagerImpl reset(long... fields)
    {
        super.reset(fields);
        return this;
    }

    @Override
    @CheckReturnValue
    public GuildManagerImpl reset()
    {
        super.reset();
        this.name = null;
        this.region = null;
        this.icon = null;
        this.splash = null;
        this.afkChannel = null;
        this.systemChannel = null;
        return this;
    }

    @Override
    @CheckReturnValue
    public GuildManagerImpl setName(String name)
    {
        Checks.notNull(name, "Name");
        Checks.check(name.length() >= 2 && name.length() <= 100, "Name must be between 2-100 characters long");
        this.name = name;
        set |= NAME;
        return this;
    }

    @Override
    @CheckReturnValue
    public GuildManagerImpl setRegion(Region region)
    {
        Checks.notNull(region, "Region");
        Checks.check(region != Region.UNKNOWN, "Region must not be UNKNOWN");
        Checks.check(!region.isVip() || getGuild().getFeatures().contains("VIP_REGIONS"), "Cannot set a VIP voice region on this guild");
        this.region = region.getKey();
        set |= REGION;
        return this;
    }

    @Override
    @CheckReturnValue
    public GuildManagerImpl setIcon(Icon icon)
    {
        this.icon = icon;
        set |= ICON;
        return this;
    }

    @Override
    @CheckReturnValue
    public GuildManagerImpl setSplash(Icon splash)
    {
        Checks.check(splash == null || getGuild().getFeatures().contains("INVITE_SPLASH"), "Cannot set a splash on this guild");
        this.splash = splash;
        set |= SPLASH;
        return this;
    }

    @Override
    @CheckReturnValue
    public GuildManagerImpl setAfkChannel(VoiceChannel afkChannel)
    {
        Checks.check(afkChannel == null || afkChannel.getGuild().equals(getGuild()), "Channel must be from the same guild");
        this.afkChannel = afkChannel == null ? null : afkChannel.getId();
        set |= AFK_CHANNEL;
        return this;
    }

    @Override
    @CheckReturnValue
    public GuildManagerImpl setSystemChannel(TextChannel systemChannel)
    {
        Checks.check(systemChannel == null || systemChannel.getGuild().equals(getGuild()), "Channel must be from the same guild");
        this.systemChannel = systemChannel == null ? null : systemChannel.getId();
        set |= SYSTEM_CHANNEL;
        return this;
    }

    @Override
    @CheckReturnValue
    public GuildManagerImpl setAfkTimeout(Guild.Timeout timeout)
    {
        Checks.notNull(timeout, "Timeout");
        this.afkTimeout = timeout.getSeconds();
        set |= AFK_TIMEOUT;
        return this;
    }

    @Override
    @CheckReturnValue
    public GuildManagerImpl setVerificationLevel(Guild.VerificationLevel level)
    {
        Checks.notNull(level, "Level");
        Checks.check(level != Guild.VerificationLevel.UNKNOWN, "Level must not be UNKNOWN");
        this.verificationLevel = level.getKey();
        set |= VERIFICATION_LEVEL;
        return this;
    }

    @Override
    @CheckReturnValue
    public GuildManagerImpl setDefaultNotificationLevel(Guild.NotificationLevel level)
    {
        Checks.notNull(level, "Level");
        Checks.check(level != Guild.NotificationLevel.UNKNOWN, "Level must not be UNKNOWN");
        this.notificationLevel = level.getKey();
        set |= NOTIFICATION_LEVEL;
        return this;
    }

    @Override
    @CheckReturnValue
    public GuildManagerImpl setRequiredMFALevel(Guild.MFALevel level)
    {
        Checks.notNull(level, "Level");
        Checks.check(level != Guild.MFALevel.UNKNOWN, "Level must not be UNKNOWN");
        this.mfaLevel = level.getKey();
        set |= MFA_LEVEL;
        return this;
    }

    @Override
    @CheckReturnValue
    public GuildManagerImpl setExplicitContentLevel(Guild.ExplicitContentLevel level)
    {
        Checks.notNull(level, "Level");
        Checks.check(level != Guild.ExplicitContentLevel.UNKNOWN, "Level must not be UNKNOWN");
        this.explicitContentLevel = level.getKey();
        set |= EXPLICIT_CONTENT_LEVEL;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        JSONObject body = new JSONObject().put("name", getGuild().getName());
        if (shouldUpdate(NAME))
            body.put("name", name);
        if (shouldUpdate(REGION))
            body.put("region", region);
        if (shouldUpdate(AFK_TIMEOUT))
            body.put("afk_timeout", afkTimeout);
        if (shouldUpdate(ICON))
            body.put("icon", icon == null ? JSONObject.NULL : icon.getEncoding());
        if (shouldUpdate(SPLASH))
            body.put("splash", splash == null ? JSONObject.NULL : splash.getEncoding());
        if (shouldUpdate(AFK_CHANNEL))
            body.put("afk_channel_id", opt(afkChannel));
        if (shouldUpdate(SYSTEM_CHANNEL))
            body.put("system_channel_id", opt(systemChannel));
        if (shouldUpdate(VERIFICATION_LEVEL))
            body.put("verification_level", verificationLevel);
        if (shouldUpdate(NOTIFICATION_LEVEL))
            body.put("default_message_notifications", notificationLevel);
        if (shouldUpdate(MFA_LEVEL))
            body.put("mfa_level", mfaLevel);
        if (shouldUpdate(EXPLICIT_CONTENT_LEVEL))
            body.put("explicit_content_filter", explicitContentLevel);

        reset(); //now that we've built our JSON object, reset the manager back to the non-modified state
        return getRequestBody(body);
    }

    @Override
    protected boolean checkPermissions()
    {
        if (!getGuild().getSelfMember().hasPermission(Permission.MANAGE_SERVER))
            throw new InsufficientPermissionException(Permission.MANAGE_SERVER);
        return super.checkPermissions();
    }
}
