package uk.org.thehickses.csbadders;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.cloud.storage.Blob;

import uk.org.thehickses.csbadders.Application.StorageInfo;

public class RequestHandler
{
    private static final String stateKey = "state";

    private final StorageInfo storage;
    private final ObjectMapper mapper;
    private final Templater templater;

    public RequestHandler(StorageInfo storage, ObjectMapper mapper, Templater templater)
    {
        this.storage = storage;
        this.mapper = mapper;
        this.templater = templater;
    }

    public String handle(List<String> names, boolean post)
    {
        try
        {
            var state = state(names);
            if (post)
            {
                state.doNext(names);
                storage.insert(stateKey, mapper.writeValueAsBytes(state));
            }
            return templater.applyTemplate("home.ftlh", state);
        }
        catch (JsonProcessingException e)
        {
            throw new RuntimeException("Unable to write state", e);
        }
    }

    private State state(List<String> names)
    {
        return Optional.of(stateKey)
                .map(storage::get)
                .map(Blob::getContent)
                .map(this::state)
                .filter(State::isToday)
                .orElseGet(() -> new State(names));
    }

    private State state(byte[] content)
    {
        try
        {
            return new ObjectMapper(new YAMLFactory()).readValue(content, State.class);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to convert blob to State", e);
        }
    }
}
