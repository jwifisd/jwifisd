package io.github.jwifisd.api;

import io.github.jwifisd.impl.ICardListener;

public interface ICardManager {

    void addListener(IFileListener fileListener);

    void addListener(ICardListener cardListener);

    void removeListener(IFileListener fileListener);

    void removeListener(ICardListener cardListener);
}
