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

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.pagination.MessagePaginationAction;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.requests.Route;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MessagePaginationActionImpl
    extends PaginationActionImpl<Message, MessagePaginationAction>
    implements MessagePaginationAction
{
    private final MessageChannel channel;

    public MessagePaginationActionImpl(MessageChannel channel)
    {
        super(channel.getJDA(), Route.Messages.GET_MESSAGE_HISTORY.compile(channel.getId()), 1, 100, 100);

        if (channel.getType() == ChannelType.TEXT)
        {
            TextChannel textChannel = (TextChannel) channel;
            if (!textChannel.getGuild().getSelfMember().hasPermission(textChannel, Permission.MESSAGE_HISTORY))
                throw new InsufficientPermissionException(Permission.MESSAGE_HISTORY);
        }

        this.channel = channel;
    }

    @Override
    public MessageChannel getChannel()
    {
        return channel;
    }

    @Override
    protected Route.CompiledRoute finalizeRoute()
    {
        Route.CompiledRoute route = super.finalizeRoute();

        final String limit = String.valueOf(this.getLimit());
        final long last = this.lastKey;

        route = route.withQueryParams("limit", limit);

        if (last != 0)
            route = route.withQueryParams("before", Long.toUnsignedString(last));

        return route;
    }

    @Override
    protected void handleSuccess(Response response, Request<List<Message>> request)
    {
        JSONArray array = response.getArray();
        List<Message> messages = new ArrayList<>(array.length());
        EntityBuilder builder = api.get().getEntityBuilder();
        for (int i = 0; i < array.length(); i++)
        {
            try
            {
                Message msg = builder.createMessage(array.getJSONObject(i), channel, false);
                messages.add(msg);
                if (useCache)
                    cached.add(msg);
                last = msg;
                lastKey = last.getIdLong();
            }
            catch (JSONException | NullPointerException e)
            {
                LOG.warn("Encountered an exception in MessagePagination", e);
            }
        }

        request.onSuccess(messages);
    }
}
