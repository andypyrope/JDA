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

package net.dv8tion.jda.internal.requests.restaction.pagination;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AuditLogPaginationActionImpl
    extends PaginationActionImpl<AuditLogEntry, AuditLogPaginationAction>
    implements AuditLogPaginationAction
{
    protected final Guild guild;
    // filters
    protected ActionType type = null;
    protected String userId = null;

    public AuditLogPaginationActionImpl(Guild guild)
    {
        super(guild.getJDA(), Route.Guilds.GET_AUDIT_LOGS.compile(guild.getId()), 1, 100, 100);
        if (!guild.getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS))
            throw new InsufficientPermissionException(Permission.VIEW_AUDIT_LOGS);
        this.guild = guild;
    }

    @Override
    public AuditLogPaginationActionImpl type(ActionType type)
    {
        this.type = type;
        return this;
    }

    @Override
    public AuditLogPaginationActionImpl user(User user)
    {
        return user(user == null ? null : user.getId());
    }

    @Override
    public AuditLogPaginationActionImpl user(String userId)
    {
        Checks.isSnowflake(userId, "User ID");
        this.userId = userId;
        return this;
    }

    @Override
    public AuditLogPaginationActionImpl user(long userId)
    {
        return user(Long.toUnsignedString(userId));
    }

    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Override
    protected Route.CompiledRoute finalizeRoute()
    {
        Route.CompiledRoute route = super.finalizeRoute();

        final String limit = String.valueOf(this.limit.get());
        final long last = this.lastKey;

        route = route.withQueryParams("limit", limit);

        if (type != null)
            route = route.withQueryParams("action_type", String.valueOf(type.getKey()));

        if (userId != null)
            route = route.withQueryParams("user_id", userId);

        if (last != 0)
            route = route.withQueryParams("before", Long.toUnsignedString(last));

        return route;
    }

    @Override
    protected void handleSuccess(Response response, Request<List<AuditLogEntry>> request)
    {
        JSONObject obj = response.getObject();
        JSONArray users = obj.getJSONArray("users");
        JSONArray webhooks = obj.getJSONArray("webhooks");
        JSONArray entries = obj.getJSONArray("audit_log_entries");

        List<AuditLogEntry> list = new ArrayList<>(entries.length());
        EntityBuilder builder = api.get().getEntityBuilder();

        TLongObjectMap<JSONObject> userMap = new TLongObjectHashMap<>();
        for (int i = 0; i < users.length(); i++)
        {
            JSONObject user = users.getJSONObject(i);
            userMap.put(user.getLong("id"), user);
        }
        
        TLongObjectMap<JSONObject> webhookMap = new TLongObjectHashMap<>();
        for (int i = 0; i < webhooks.length(); i++)
        {
            JSONObject webhook = webhooks.getJSONObject(i);
            webhookMap.put(webhook.getLong("id"), webhook);
        }
        
        for (int i = 0; i < entries.length(); i++)
        {
            try
            {
                JSONObject entry = entries.getJSONObject(i);
                JSONObject user = userMap.get(Helpers.optLong(entry, "user_id", 0));
                JSONObject webhook = webhookMap.get(Helpers.optLong(entry, "target_id", 0));
                AuditLogEntry result = builder.createAuditLogEntry((GuildImpl) guild, entry, user, webhook);
                list.add(result);
                if (this.useCache)
                    this.cached.add(result);
                this.last = result;
                this.lastKey = last.getIdLong();
            }
            catch (JSONException | NullPointerException e)
            {
                LOG.warn("Encountered exception in AuditLogPagination", e);
            }
        }

        request.onSuccess(list);
    }
}
