package io.github.jwifisd.eyefi;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import io.github.jwifisd.api.IWifiFile;

public class EyeFiPhoto implements IWifiFile {

    private final String name;

    private final byte[] data;

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

}
