package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.MessageRetrieveAction;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Constructs a MessageHistory object with initially retrieved Messages before or after a certain pivot message id.
 * <br>Allows to {@link #limit(Integer) limit} the amount to retrieve for better performance!
 */
public class MessageRetrieveActionImpl extends RestActionImpl<MessageHistory> implements MessageRetrieveAction
{
    private final MessageChannel channel;
    private Integer limit;

    protected MessageRetrieveActionImpl(Route.CompiledRoute route, MessageChannel channel)
    {
        super(channel.getJDA(), route);
        this.channel = channel;
    }

    /**
     * Limit between 1-100 messages that should be retrieved.
     *
     * @param  limit
     *         The limit to use, or {@code null} to use default 50
     *
     * @throws IllegalArgumentException
     *         If the provided limit is not between 1-100
     *
     * @return The current MessageRetrieveAction for chaining convenience
     */
    @Override public MessageRetrieveAction limit(Integer limit)
    {
        if (limit != null)
        {
            Checks.positive(limit, "Limit");
            Checks.check(limit <= 100, "Limit may not exceed 100!");
        }
        this.limit = limit;
        return this;
    }

    @Override
    protected Route.CompiledRoute finalizeRoute()
    {
        final Route.CompiledRoute route = super.finalizeRoute();
        return limit == null ? route : route.withQueryParams("limit", String.valueOf(limit));
    }

    @Override
    protected void handleSuccess(Response response, Request<MessageHistory> request)
    {
        final MessageHistoryImpl result = new MessageHistoryImpl(channel);
        final JSONArray array = response.getArray();
        final EntityBuilder builder = api.get().getEntityBuilder();
        for (int i = 0; i < array.length(); i++)
        {
            try
            {
                JSONObject obj = array.getJSONObject(i);
                result.history.put(obj.getLong("id"), builder.createMessage(obj, channel, false));
            }
            catch (JSONException | NullPointerException e)
            {
                LOG.warn("Encountered exception in MessagePagination", e);
            }
        }
        request.onSuccess(result);
    }
}
