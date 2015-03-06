package io.github.jwifisd.api;

/*
 * #%L
 * jwifisd-api
 * %%
 * Copyright (C) 2012 - 2015 jwifisd
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.net.InetAddress;

public interface ICard {

    String title();

    String mac();

    InetAddress ipAddress();

    IBrowse browse();

    boolean addListener(IFileListener fileListener);

    boolean removeListener(IFileListener fileListener);

    /**
     * @return the api level of the card, all levels below 10 are not real cards
     *         but just potential cards. a level 0 is an end implementation and
     *         can be used.
     */
    int level();

    void reconnect();
}
