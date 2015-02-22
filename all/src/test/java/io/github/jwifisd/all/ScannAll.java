package io.github.jwifisd.all;

import io.github.jwifisd.api.ICard;
import io.github.jwifisd.api.IDetector;
import io.github.jwifisd.net.IDoWithNetwork;
import io.github.jwifisd.net.LocalNetwork;
import io.github.jwifisd.net.LocalNetworkScanner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

public class ScannAll implements IDoWithNetwork {

    public static void main(String[] args) throws IOException {
        LocalNetworkScanner scanner = new LocalNetworkScanner();

        scanner.scan(new ScannAll());

    }

    @Override
    public void run(LocalNetwork localNetwork) {
        List<ICard> result = new ArrayList<>();
        Iterator<IDetector> detectors = ServiceLoader.load(IDetector.class).iterator();
        while (detectors.hasNext()) {
            IDetector iDetector = (IDetector) detectors.next();
            try {
                result.addAll(iDetector.scan(localNetwork, "flashair", "transiend"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (ICard iCard : result) {
            System.out.println("found: " + iCard.title() + " - " + iCard.ipAddress());
        }
    }
}
