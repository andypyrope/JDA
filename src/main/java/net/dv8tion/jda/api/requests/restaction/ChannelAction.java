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

package net.dv8tion.jda.api.requests.restaction;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import javax.annotation.CheckReturnValue;
import java.util.Collection;
import java.util.function.BooleanSupplier;

/**
 * Extension of {@link net.dv8tion.jda.api.requests.RestAction RestAction} specifically
 * designed to create a {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel}.
 * This extension allows setting properties before executing the action.
 *
 * @since  3.0
 *
 * @see    net.dv8tion.jda.api.managers.GuildController
 * @see    net.dv8tion.jda.api.managers.GuildController#createTextChannel(String)
 * @see    net.dv8tion.jda.api.managers.GuildController#createVoiceChannel(String)
 * @see    net.dv8tion.jda.api.managers.GuildController#createCategory(String)
 * @see    GuildChannel#createCopy()
 * @see    GuildChannel#createCopy(Guild)
 *
 * @param <T>
 *        The type of channel to create
 */
public interface ChannelAction<T extends GuildChannel> extends AuditableRestAction<T>
{
    @Override
    ChannelAction<T> setCheck(BooleanSupplier checks);

    /**
     * The guild to create this {@link GuildChannel} in
     *
     * @return The guild
     */
    Guild getGuild();

    /**
     * The {@link ChannelType} for the resulting channel
     *
     * @return The channel type
     */
    ChannelType getType();

    /**
     * Sets the name for the new GuildChannel
     *
     * @param  name
     *         The not-null name for the new GuildChannel (1-100 chars long)
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided name is null or not between 1-100 chars long
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @CheckReturnValue
    ChannelAction<T> setName(String name);

    /**
     * Sets the {@link net.dv8tion.jda.api.entities.Category Category} for the new GuildChannel
     *
     * @param  category
     *         The parent for the new GuildChannel
     *
     * @throws UnsupportedOperationException
     *         If this ChannelAction is for a Category
     * @throws IllegalArgumentException
     *         If the provided category is {@code null}
     *         or not from this Guild
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @CheckReturnValue
    ChannelAction<T> setParent(Category category);

    /**
     * Sets the position where the new Channel should be inserted into.
     * This refers to the raw position value, not the computed (relative) position.
     * <p>
     * By default (or by providing this method with {@code null}),
     * the position will automatically be computed based on the other Channels (inserted last in its respective group).
     * <p>
     * Note: This does not shift the position values of existing Channels if the values collide.
     * <br>As a reminder: The ordering of Channels is determined first by its Category's position, then by its raw
     * position value and finally by its id (younger Channels are below older ones)
     *
     * @param  position
     *         The raw position value that should be used for the new Channel
     *
     * @throws IllegalArgumentException
     *         If the provided position value is {@code <0}
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @CheckReturnValue
    ChannelAction<T> setPosition(Integer position);

    /**
     * Sets the topic for the new TextChannel
     *
     * @param  topic
     *         The topic for the new GuildChannel (max 1024 chars)
     *
     * @throws UnsupportedOperationException
     *         If this ChannelAction is not for a TextChannel
     * @throws IllegalArgumentException
     *         If the provided topic is longer than 1024 chars
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @CheckReturnValue
    ChannelAction<T> setTopic(String topic);

    /**
     * Sets the NSFW flag for the new TextChannel
     *
     * @param  nsfw
     *         The NSFW flag for the new GuildChannel
     *
     * @throws UnsupportedOperationException
     *         If this ChannelAction is not for a TextChannel
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @CheckReturnValue
    ChannelAction<T> setNSFW(boolean nsfw);

    /**
     * Sets the slowmode value, which limits the amount of time that individual users must wait
     * between sending messages in the new TextChannel. This is measured in seconds.
     *
     * <p>Note that only {@link net.dv8tion.jda.api.AccountType#CLIENT CLIENT} type accounts are
     * affected by slowmode, and that {@link net.dv8tion.jda.api.AccountType#BOT BOT} accounts
     * are immune to the restrictions.
     * <br>Having {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE MESSAGE_MANAGE} or
     * {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} permission also
     * grants immunity to slowmode.
     *
     * @param  slowmode
     *         The number of seconds required to wait between sending messages in the channel.
     *
     * @throws IllegalArgumentException
     *         If the {@code slowmode} is greater than 120, or less than 0
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @CheckReturnValue
    ChannelAction<T> setSlowmode(int slowmode);

    /**
     * Adds a new Role or Member {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride}
     * for the new GuildChannel.
     *
     * <p>Example:
     * <pre>{@code
     * Role role = guild.getPublicRole();
     * EnumSet<Permission> allow = EnumSet.of(Permission.MESSAGE_READ);
     * EnumSet<Permission> deny = EnumSet.of(Permission.MESSAGE_WRITE);
     * channelAction.addPermissionOverride(role, allow, deny);
     * }</pre>
     *
     * @param  target
     *         The not-null {@link net.dv8tion.jda.api.entities.Role Role} or {@link net.dv8tion.jda.api.entities.Member Member} for the override
     * @param  allow
     *         The granted {@link net.dv8tion.jda.api.Permission Permissions} for the override or null
     * @param  deny
     *         The denied {@link net.dv8tion.jda.api.Permission Permissions} for the override or null
     *
     * @throws java.lang.IllegalArgumentException
     *         If the specified target is null or not within the same guild.
     *
     * @return The current ChannelAction, for chaining convenience
     *
     * @see    java.util.EnumSet
     */
    @CheckReturnValue
    default ChannelAction<T> addPermissionOverride(IPermissionHolder target, Collection<Permission> allow, Collection<Permission> deny)
    {
        final long allowRaw = allow != null ? Permission.getRaw(allow) : 0;
        final long denyRaw = deny != null ? Permission.getRaw(deny) : 0;

        return addPermissionOverride(target, allowRaw, denyRaw);
    }

    /**
     * Adds a new Role or Member {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride}
     * for the new GuildChannel.
     *
     * <p>Example:
     * <pre>{@code
     * Role role = guild.getPublicRole();
     * long allow = Permission.MESSAGE_READ.getRawValue();
     * long deny = Permission.MESSAGE_WRITE.getRawValue() | Permission.MESSAGE_ADD_REACTION.getRawValue();
     * channelAction.addPermissionOverride(role, allow, deny);
     * }</pre>
     *
     * @param  target
     *         The not-null {@link net.dv8tion.jda.api.entities.Role Role} or {@link net.dv8tion.jda.api.entities.Member Member} for the override
     * @param  allow
     *         The granted {@link net.dv8tion.jda.api.Permission Permissions} for the override
     *         Use {@link net.dv8tion.jda.api.Permission#getRawValue()} to retrieve these Permissions.
     * @param  deny
     *         The denied {@link net.dv8tion.jda.api.Permission Permissions} for the override
     *         Use {@link net.dv8tion.jda.api.Permission#getRawValue()} to retrieve these Permissions.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the specified target is null
     *                 or not within the same guild.</li>
     *             <li>If one of the provided Permission values is invalid</li>
     *         </ul>
     *
     * @return The current ChannelAction, for chaining convenience
     *
     * @see    net.dv8tion.jda.api.Permission#getRawValue()
     * @see    net.dv8tion.jda.api.Permission#getRaw(java.util.Collection)
     * @see    net.dv8tion.jda.api.Permission#getRaw(net.dv8tion.jda.api.Permission...)
     */
    @CheckReturnValue
    ChannelAction<T> addPermissionOverride(IPermissionHolder target, long allow, long deny);

    /**
     * Sets the bitrate for the new VoiceChannel
     *
     * @param  bitrate
     *         The bitrate for the new GuildChannel (min {@code 8000}; max {@code 96000}/{@code 128000}
     *         (for {@link net.dv8tion.jda.api.entities.Guild#getFeatures() VIP Guilds})) or null to use default ({@code 64000})
     *
     * @throws UnsupportedOperationException
     *         If this ChannelAction is not for a VoiceChannel
     * @throws IllegalArgumentException
     *         If the provided bitrate is less than 8000 or greater than 128000
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @CheckReturnValue
    ChannelAction<T> setBitrate(Integer bitrate);

    /**
     * Sets the userlimit for the new VoiceChannel
     *
     * @param  userlimit
     *         The userlimit for the new VoiceChannel or {@code null}/{@code 0} to use no limit,
     *
     * @throws UnsupportedOperationException
     *         If this ChannelAction is not for a VoiceChannel
     * @throws IllegalArgumentException
     *         If the provided userlimit is negative or above {@code 99}
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @CheckReturnValue
    ChannelAction<T> setUserlimit(Integer userlimit);
}
