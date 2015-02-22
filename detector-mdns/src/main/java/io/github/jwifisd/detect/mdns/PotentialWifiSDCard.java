package io.github.jwifisd.detect.mdns;

import io.github.jwifisd.api.IBrowse;
import io.github.jwifisd.api.ICard;
import io.github.jwifisd.api.IEvent;

import java.net.InetAddress;

public class PotentialWifiSDCard implements ICard {

    private final String fullQualifiedName;

    private final InetAddress ip;

    public PotentialWifiSDCard(String fullQualifiedName, InetAddress ip) {
        this.fullQualifiedName = fullQualifiedName;
        this.ip = ip;
    }

    public String getFullQualifiedName() {
        return fullQualifiedName;
    }

    public InetAddress getIp() {
        return ip;
    }

    @Override
    public String title() {
        return getFullQualifiedName();
    }

    @Override
    public InetAddress ipAddress() {
        return ip;
    }

    @Override
    public IBrowse browse() {
        return null;
    }

    @Override
    public IEvent event() {
        return null;
    }

}
