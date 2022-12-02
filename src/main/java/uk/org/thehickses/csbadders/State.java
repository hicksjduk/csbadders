package uk.org.thehickses.csbadders;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "date", "polygon", "pairings" })
public class State
{
    private List<Pairing> pairs;
    private List<Player> polygon;
    private LocalDate date;

    @SuppressWarnings("unused")
    private State()
    {
        this.date = LocalDate.now();
    }

    public State(List<String> names)
    {
        this(Arrays.asList(), names, Arrays.asList());
    }

    State(List<Pairing> pairs, List<String> players, List<Player> polygon)
    {
        this.pairs = pairs;
        this.polygon = polygon;
        this.date = LocalDate.now();
    }
    
    public void doNext(List<String> names)
    {
        polygon = newPolygon(names);
        pairs = generatePairings();
        updatePlayers();
    }
    
    private void updatePlayers()
    {
        var playersInPairedOrder = new LinkedHashMap<String, Player>();
        pairs.stream()
                .flatMap(p -> Stream.of(p.p1(), p.p2()))
                .forEach(p -> playersInPairedOrder.put(p.getName(), p));
        polygon.stream()
                .forEach(p -> playersInPairedOrder.putIfAbsent(p.getName(), p));
        var index = new AtomicInteger();
        playersInPairedOrder.values()
                .stream()
                .forEach(p -> p.addCourt(index.getAndIncrement() / 4 + 1));
    }

    @JsonIgnore
    public List<Pairing> getPairs()
    {
        return pairs;
    }

    @JsonIgnore
    public List<String> getPlayers()
    {
        return polygon.stream()
                .map(Player::getName)
                .sorted()
                .toList();
    }

    public List<Player> getPolygon()
    {
        return polygon;
    }

    @JsonIgnore
    public List<String> getMatches()
    {
        var it = pairs.stream()
                .map(p -> Stream.of(p.p1(), p.p2())
                        .map(Player::getName)
                        .collect(Collectors.joining(" & ")))
                .iterator();
        var matchStrings = new ArrayList<String>();
        while (it.hasNext())
        {
            Optional.of(it.next())
                    .filter(x -> it.hasNext())
                    .map(p -> Stream.of(p, it.next())
                            .collect(Collectors.joining(" vs ")))
                    .ifPresent(matchStrings::add);
        }
        return matchStrings;
    }

    @JsonIgnore
    public String getUnpaired()
    {
        if (pairs.isEmpty())
            return "";
        var paired = pairs.stream()
                .flatMap(p -> Stream.of(p.p1(), p.p2()))
                .map(Player::getName)
                .toList();
        return getPlayers().stream()
                .filter(Predicate.not(paired::contains))
                .collect(Collectors.joining(", "));
    }

    public LocalDate date()
    {
        return date;
    }

    public String getDate()
    {
        return DateTimeFormatter.ISO_LOCAL_DATE.format(date);
    }

    public void setDate(String str)
    {
        date = LocalDate.parse(str);
    }

    @JsonIgnore
    public boolean isToday()
    {
        return LocalDate.now()
                .equals(date);
    }

    public List<List<String>> getPairings()
    {
        return pairs.stream()
                .map(p -> Stream.of(p.p1(), p.p2())
                        .map(Player::getName)
                        .toList())
                .toList();
    }

    public void setPairings(List<List<String>> pairings)
    {
        var byName = playersByName();
        pairs = pairings.stream()
                .map(l -> l.stream()
                        .map(byName::get)
                        .iterator())
                .map(it -> new Pairing(it.next(), it.next()))
                .toList();
    }

    private Map<String, Player> playersByName()
    {
        return polygon.stream()
                .collect(Collectors.toMap(Player::getName, Function.identity()));
    }

    private List<Player> newPolygon(List<String> names)
    {
        var nameStatuses = names.stream()
                .collect(Collectors.partitioningBy(polygon.stream()
                        .map(Player::getName)
                        .toList()::contains));
        var newNames = nameStatuses.get(false);
        var existingNames = nameStatuses.get(true);
        if (newNames.isEmpty() && polygon.size() == names.size())
            return rotatePolygon();
        var newList = new ArrayList<>(Stream.concat(polygon.stream()
                .filter(p -> existingNames.contains(p.getName())),
                newNames.stream()
                        .map(n -> new Player(n, polygon.size() / 4 + 1)))
                .toList());
        Collections.shuffle(newList);
        if (names.size() % 2 == 1)
            IntStream.range(0, newList.size())
                    .filter(i -> !newNames.contains(newList.get(i)
                            .getName()))
                    .boxed()
                    .findFirst()
                    .filter(i -> i != 0)
                    .map(i -> newList.remove(i.intValue()))
                    .ifPresent(p -> newList.add(0, p));
        return newList;
    }

    private List<Player> rotatePolygon()
    {
        if (polygon.size() < 4)
            return polygon;
        var deque = new ArrayDeque<>(polygon);
        var last = polygon.size() % 2 == 1 ? null : deque.pollLast();
        deque.push(deque.pollLast());
        if (last != null)
            deque.offer(last);
        return new ArrayList<>(deque);
    }

    private List<Pairing> generatePairings()
    {
        if (polygon.size() < 4)
            return Arrays.asList();
        var deque = new ArrayDeque<>(polygon);
        var answer = new ArrayList<Pairing>();
        if (polygon.size() % 2 == 1)
            deque.pop();
        while (deque.size() > 1)
            answer.add(new Pairing(deque.pop(), deque.pollLast()));
        Collections.sort(answer);
        return answer.subList(0, answer.size() / 2 * 2);
    }
}