package org.jwifisd.mdns;

/*
 * #%L
 * jwifisd-mdns
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

public enum QClass {
    Reserved0000(0x0000, "Reserved", false, true, false),
    Internet(0x0001, "Internet", false, false, false),
    InternetX(0x8000, "InternetX", false, false, false),
    InternetL(0x8001, "InternetL", false, false, false),
    CSNET(0x0002, "CS", true, false, false),
    Chaos(0x0003, "CH", false, true, false),
    Hesiod(0x0004, "HS", false, false, false),
    Any(0x00FF, "Any", false, false, true),
    ReservedFFFF(0xFFFF, "Reserved", false, true, false), ;

    private final int value;

    private QClass(int value, String description, boolean obsolete, boolean reserved, boolean isOnlyQClass) {
        this.value = value;
    }

    public static QClass qclass(int value) {
        for (QClass qClass : values()) {
            if (qClass.value == value) {
                return qClass;
            }
        }
        throw new IllegalArgumentException("Unrecognised class code ");
    }

    public int getValue() {
        return value;
    }
}
