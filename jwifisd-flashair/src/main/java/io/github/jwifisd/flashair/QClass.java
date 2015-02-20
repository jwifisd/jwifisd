package io.github.jwifisd.flashair;

public enum QClass {
    Reserved0000(0x0000, "Reserved", false, true, false),
    Internet(0x0001, "Internet", false, false, false),
    CSNET(0x0002, "CS", true, false, false),
    Chaos(0x0003, "CH", false, true, false),
    Hesiod(0x0004, "HS", false, false, false),
    Any(0x00FF, "Any", false, false, true),
    ReservedFFFF(0xFFFF, "Reserved", false, true, false), ;

    private final int value;

    private final String description;

    private final boolean obsolete;

    private final boolean reserved;

    private final boolean isOnlyQClass;

    private QClass(int value, String description, boolean obsolete, boolean reserved, boolean isOnlyQClass) {
        this.value = value;
        this.description = description;
        this.obsolete = obsolete;
        this.reserved = reserved;
        this.isOnlyQClass = isOnlyQClass;
    }

    public static QClass qclass(int value) {
        value=value&0xFF;
        for (QClass qClass : values()) {
            if (qClass.value == value) {
                return qClass;
            }
        }
        throw new IllegalArgumentException("Unrecognised class code ");
    }
}
