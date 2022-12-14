# An app for generating badminton pairings

At the badminton group I play with, there has long been a system for determining who plays
with whom, and against whom, by the players numbering off and various printed tables being
consulted to show the numbers of the players who should be involved in each game.

When I first saw this, I laughed as I thought it was a ridiculously over-engineered way of
carrying out a task that could easily be performed by negotiation. Now, however, the joke
is on me because I over-engineered it even more by writing this app.

The advantages of this app over the existing way of doing things are:

* Nobody needs to remember what number they are (and you would be surprised how often they forget).
* The person running the session does not need to remember which line of the table they need to use
next.
* Arguably, it produces a better variety of pairings. As long as the players don't change, 
it guarantees that nobody will partner anyone more than once until they have played with each of
the others. However, I haven't done detailed analysis on this so there may still be 
scope for improvement.

To use the app, visit [http://csb.thehickses.org.uk/](http://csb.thehickses.org.uk/). Enter 
player names into the text box, one per line, and press the `Next` button to 
save the names. If there are at least four names, a set of pairings will be generated. 
Press `Next` again to generate another set of pairings. Players can be added or removed, 
and the next press of `Next` will reflect the changes. Duplicate names and blank lines 
are ignored.

The current state is stored in the page itself, so if you close the page you will need to start 
again from scratch.

The app is written in Java 17, using Spring Boot and Freemarker templating.
It can be run locally by running the 
[`Application` class](src/main/java/uk/org/thehickses/csbadders/Application.java).
It is currently hosted on Google Cloud Platform;
changes to the app are automatically deployed when checked in to the `main` branch.