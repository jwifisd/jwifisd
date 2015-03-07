package io.github.jwifisd.flashair;

import io.github.jwifisd.api.ICard;
import io.github.jwifisd.api.ICardImplentation;

public class FlashAirWifiSDDetector implements ICardImplentation {

    @Override
    public ICard decreaseLevel(ICard card) {
        return FlashAirWiFiSD.create(card);
    }

}
