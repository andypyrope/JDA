package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.requests.RestAction;

public interface MessageRetrieveAction extends RestAction<MessageHistory> {
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
    MessageRetrieveAction limit(Integer limit);
}
