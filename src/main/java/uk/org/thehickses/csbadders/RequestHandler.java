package uk.org.thehickses.csbadders;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class RequestHandler
{
    private final Templater templater;

    public RequestHandler(Templater templater)
    {
        this.templater = templater;
    }

    public String handle(List<String> names, Optional<Stream<String>> startingState)
    {
        var state = startingState.map(State::new).map(s -> s.setNames(names))
                .map(State::nextSet)
                .orElse(new State().setNames(names));
        return templater.applyTemplate("home.ftlh", state);
    }
}
