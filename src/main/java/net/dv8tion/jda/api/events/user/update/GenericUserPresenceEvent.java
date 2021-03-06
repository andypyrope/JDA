/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.api.events.user.update;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

/**
 * Indicates that the presence of a {@link net.dv8tion.jda.api.entities.User User} has changed.
 * <br>Users don't have presences directly, this is fired when a {@link net.dv8tion.jda.api.entities.Member Member} from a {@link net.dv8tion.jda.api.entities.Guild Guild}
 * changes their presence.
 *
 * <p>Can be used to track the presence updates of members.
 */
public interface GenericUserPresenceEvent
{
    /**
     * Possibly-null guild in which the presence has changed.
     *
     * @return The guild
     */
    Guild getGuild();

    /**
     * Possibly-null member who changed their presence.
     *
     * @return The member
     */
    Member getMember();
}
