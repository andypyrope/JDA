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

package net.dv8tion.jda.internal.requests.restaction;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.MemberAction;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import okhttp3.RequestBody;
import org.json.JSONObject;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

public class MemberActionImpl extends RestActionImpl<Void> implements MemberAction
{
    private final String accessToken;
    private final String userId;
    private final Guild guild;

    private String nick;
    private Set<Role> roles;
    private boolean mute;
    private boolean deaf;

    public MemberActionImpl(JDA api, Guild guild, String userId, String accessToken)
    {
        super(api, Route.Guilds.ADD_MEMBER.compile(guild.getId(), userId));
        this.accessToken = accessToken;
        this.userId = userId;
        this.guild = guild;
    }

    @Override
    public MemberAction setCheck(BooleanSupplier checks)
    {
        return (MemberAction) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public String getAccessToken()
    {
        return accessToken;
    }

    @Nonnull
    @Override
    public String getUserId()
    {
        return userId;
    }

    @Nullable
    @Override
    public User getUser()
    {
        return getJDA().getUserById(userId);
    }

    @Nonnull
    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Override
    @CheckReturnValue
    public MemberActionImpl setNickname(String nick)
    {
        if (nick != null)
        {
            if (Helpers.isBlank(nick))
            {
                this.nick = null;
                return this;
            }
            Checks.check(nick.length() <= 32, "Nickname must not be greater than 32 characters in length");
        }
        this.nick = nick;
        return this;
    }

    @Override
    @CheckReturnValue
    public MemberActionImpl setRoles(Collection<Role> roles)
    {
        if (roles == null)
        {
            this.roles = null;
            return this;
        }
        Set<Role> newRoles = new HashSet<>(roles.size());
        for (Role role : roles)
            checkAndAdd(newRoles, role);
        this.roles = newRoles;
        return this;
    }

    @Override
    @CheckReturnValue
    public MemberActionImpl setRoles(Role... roles)
    {
        if (roles == null)
        {
            this.roles = null;
            return this;
        }
        Set<Role> newRoles = new HashSet<>(roles.length);
        for (Role role : roles)
            checkAndAdd(newRoles, role);
        this.roles = newRoles;
        return this;
    }

    @Override
    @CheckReturnValue
    public MemberActionImpl setMute(boolean mute)
    {
        this.mute = mute;
        return this;
    }

    @Override
    @CheckReturnValue
    public MemberActionImpl setDeafen(boolean deaf)
    {
        this.deaf = deaf;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        JSONObject obj = new JSONObject();
        obj.put("access_token", accessToken);
        if (nick != null)
            obj.put("nick", nick);
        if (roles != null && !roles.isEmpty())
            obj.put("roles", roles.stream().map(Role::getId).collect(Collectors.toList()));
        obj.put("mute", mute);
        obj.put("deaf", deaf);
        return getRequestBody(obj);
    }

    private void checkAndAdd(Set<Role> newRoles, Role role)
    {
        Checks.notNull(role, "Role");
        Checks.check(role.getGuild().equals(getGuild()), "Roles must all be from the same guild");
        newRoles.add(role);
    }
}
