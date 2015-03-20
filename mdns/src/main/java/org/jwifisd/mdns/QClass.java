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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
/**
 * the q class of the dns record.
 * 
 * @author Richard van Nieuwenhoven
 */
public enum QClass {
    /**
     * reserved key, not used now.
     */
    Reserved0000(0x0000, "Reserved", false, true, false),
    /**
     * the normal value = internet.
     */
    Internet(0x0001, "Internet", false, false, false),
    /**
     * special x internet.
     */
    InternetX(0x8000, "InternetX", false, false, false),
    /**
     * special l internet.
     */
    InternetL(0x8001, "InternetL", false, false, false),
    /**
     * CSNET.
     */
    CSNET(0x0002, "CS", true, false, false),
    /**
     * CHAOS.
     */
    Chaos(0x0003, "CH", false, true, false),
    /**
     * Hesiod.
     */
    Hesiod(0x0004, "HS", false, false, false),
    /**
     * Not a DNS class, but a DNS query class, meaning "all classes".
     */
    Any(0x00FF, "Any", false, false, true),
    /**
     * reserved key, not used now.
     */
    ReservedFFFF(0xFFFF, "Reserved", false, true, false);

    /**
     * the mdns value of the class.
     */
    private final int value;

    /**
     * construct a mdns q class .
     * 
     * @param value
     *            the mdns value to use.
     * @param description
     *            the description string
     * @param obsolete
     *            if this value is obsolete
     * @param reserved
     *            if this value is reserved
     * @param isOnlyQClass
     *            if this value is onyl q class.
     */
    private QClass(int value, String description, boolean obsolete, boolean reserved, boolean isOnlyQClass) {
        this.value = value;
    }

    /**
     * search for a qclass type with the speciifed mdns value.
     * 
     * @param value
     *            the value to search.
     * @return the found qclass, or an exception if not found.
     */
    public static QClass qclass(int value) {
        for (QClass qClass : values()) {
            if (qClass.value == value) {
                return qClass;
            }
        }
        throw new IllegalArgumentException("Unrecognised class code ");
    }

    /**
     * @return the mdns value of the q class.
     */
    public int getValue() {
        return value;
    }
}
