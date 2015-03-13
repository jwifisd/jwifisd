# JWiFiSD

JWiFiSD is a generic pure Java library API to access the different wifi-sd cards available on the market in a transparent way. The Java developer using the library does not have to know the specifics of the differed cards. They will be detected automatically and appropriate events are send back over the Java API.

Currently implementations for 

- eyefi (incl. mobi cards)
- transdient wifisd
- toschiba flashair

are implemented. before releasing to central, implementation of a file-browser based API are added (optionally because eyefi does not support browsing the files). Also the "china" card WIFI@SDCF will be implemented shortly.

Not jet implemented (but planned) are versions for

- PQI Air card
- ez share

if someone provides me with the cards (or the money to buy them) the implementation will go faster. Now i am waiting for a cheap offer in on-line shops. 

There are some custom Rom's out there for some of these cards, if you want a specific implementation, just make an issue in this project. But implementing depends on someone sending me a card (or the money).
