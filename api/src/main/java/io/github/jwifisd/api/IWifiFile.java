package io.github.jwifisd.api;


/*
 * #%L
 * jwifisd-api
 * %%
 * Copyright (C) 2015 jwifisd
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
/**
 * this inreface represents a file that is available on a wifi card, it depends
 * on the implementation if the data is lazy loaded from the card, copied
 * locally or available in memory so be carefull not to keep them in memory over
 * long periods.
 * 
 * @author Richard van Nieuwenhoven
 *
 */
public interface IWifiFile {

    /**
     * if possible clear the memory usage.
     */
    void clean();

    /**
     * @return the data contained in the file, this is normaly lazy loaded from
     *         the card but sometimes it is already in memory.
     */
    byte[] getData();

    /**
     * @return the name of the file.
     */
    String name();

    /**
     * @return return the timestamp of the file as in
     *         {@link java.io.File#lastModified()}
     */
    long timeStamp();
}
