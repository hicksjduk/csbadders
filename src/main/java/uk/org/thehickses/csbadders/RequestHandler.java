package uk.org.thehickses.csbadders;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;

public class RequestHandler
{
    private static final String stateKey = "state";

    private final Bucket bucket;
    private final Templater templater;

    public RequestHandler(Bucket storage, Templater templater)
    {
        this.bucket = storage;
        this.templater = templater;
    }

    public String handle(Player[] polygon, String[] names)
    {
        return templater.applyTemplate("home.ftlh", generateOutput(polygon, Stream.of(names)
                .distinct()
                .sorted()
                .toArray(String[]::new)));
    }

    private State state(List<String> names)
    {
        LocalDate today = LocalDate.now();
        return Optional.of(stateKey)
                .map(k -> bucket.get(k))
                .map(Blob::getContent)
                .map(this::state)
                .filter(State::isToday)
                .orElseGet(() -> new State(null, names, null));
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

    public State generateOutput(Player[] polygon, String[] names)
    {
        if (names.length < 4)
            return new State(new ArrayList<>(), Arrays.asList(names), new ArrayList<>());
        var newPolygon = newPolygon(polygon, names);
        var pairings = Optional.of(getPairings(newPolygon))
                .map(l -> l.subList(0, l.size() / 2 * 2))
                .get();
        var playersInPairedOrder = new LinkedHashMap<String, Player>();
        pairings.stream()
                .flatMap(p -> Stream.of(p.p1(), p.p2()))
                .forEach(p -> playersInPairedOrder.put(p.getName(), p));
        Stream.of(newPolygon)
                .forEach(p -> playersInPairedOrder.putIfAbsent(p.getName(), p));
        var index = new AtomicInteger();
        playersInPairedOrder.values()
                .stream()
                .forEach(p -> p.addCourt(index.getAndIncrement() / 4 + 1));
        return new State(pairings, Arrays.asList(names), Arrays.asList(newPolygon));
    }

    private List<Pairing> getPairings(Player[] polygon)
    {
        var deque = new ArrayDeque<>(Arrays.asList(polygon));
        var answer = new ArrayList<Pairing>();
        if (polygon.length % 2 == 1)
            deque.pop();
        while (deque.size() > 1)
            answer.add(new Pairing(deque.pop(), deque.pollLast()));
        Collections.sort(answer);
        return answer;
    }

    private Player[] newPolygon(Player[] polygon, String[] names)
    {
        var nameStatuses = Stream.of(names)
                .collect(Collectors.partitioningBy(Stream.of(polygon)
                        .map(Player::getName)
                        .toList()::contains));
        var newNames = nameStatuses.get(false);
        var existingNames = nameStatuses.get(true);
        if (newNames.isEmpty() && polygon.length == names.length)
            return rotate(polygon);
        var newList = new ArrayList<>(Stream.concat(Stream.of(polygon)
                .filter(p -> existingNames.contains(p.getName())),
                newNames.stream()
                        .map(n -> new Player(n, polygon.length / 4 + 1)))
                .toList());
        Collections.shuffle(newList);
        if (names.length % 2 == 1)
            IntStream.range(0, newList.size())
                    .filter(i -> !newNames.contains(newList.get(i)
                            .getName()))
                    .boxed()
                    .findFirst()
                    .filter(i -> i != 0)
                    .map(i -> newList.remove(i.intValue()))
                    .ifPresent(p -> newList.add(0, p));
        return newList.stream()
                .toArray(Player[]::new);
    }

    private Player[] rotate(Player[] polygon)
    {
        var deque = new ArrayDeque<>(Arrays.asList(polygon));
        var last = polygon.length % 2 == 1 ? null : deque.pollLast();
        deque.push(deque.pollLast());
        if (last != null)
            deque.offer(last);
        return deque.stream()
                .toArray(Player[]::new);
    }
}
