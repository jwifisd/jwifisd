package org.jwifisd.eyefi;

/*
 * #%L
 * jwifisd-eyefi
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

import org.jwifisd.api.IWifiFile;

/**
 * Eyefi file that was reported to the server, important to know that eyefi just
 * reports photos so no other files will come this way.
 * 
 * @author Richard van Nieuwenhoven
 */
public class EyeFiPhoto implements IWifiFile {

    /**
     * The name of the file.
     */
    private final String name;

    /**
     * the file contents (this is memory because the eyefi cards send the file
     * once only).
     */
    private final byte[] data;

    /**
     * construct a eyefi card for the name and the file contents.
     * 
     * @param name
     *            the name of the file
     * @param data
     *            the contents of the file.
     */
    public EyeFiPhoto(String name, byte[] data) {
        this.name = name;
        this.data = data;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public long timeStamp() {
        return 0;
    }

    @Override
    public void clean() {

    }
}
