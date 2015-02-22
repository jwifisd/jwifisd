package io.github.jwifisd.all;

/*
 * #%L
 * jwifisd-all
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
