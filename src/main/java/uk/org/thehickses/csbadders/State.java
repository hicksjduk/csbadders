package uk.org.thehickses.csbadders;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class State
{
    private List<Match> matches;
    private List<Player> polygon;

    public State()
    {
        this(Stream.empty());
    }

    public State(Stream<String> players)
    {
        polygon = players.map(Player::fromString)
                .toList();
        matches = new ArrayList<>();
    }

    public State setNames(List<String> names)
    {
        polygon = newPolygon(names);
        return this;
    }

    public State nextSet()
    {
        matches = generateMatches();
        updatePlayers();
        return this;
    }

    private void updatePlayers()
    {
        var playersInPairedOrder = new LinkedHashMap<String, Player>();
        matches.stream()
                .flatMap(p -> Stream.of(p.pair1(), p.pair2()))
                .flatMap(p -> Stream.of(p.p1(), p.p2()))
                .forEach(p -> playersInPairedOrder.put(p.getName(), p));
        polygon.stream()
                .forEach(p -> playersInPairedOrder.putIfAbsent(p.getName(), p));
        var index = new AtomicInteger();
        playersInPairedOrder.values()
                .stream()
                .forEach(p -> p.addCourt(index.getAndIncrement() / 4 + 1));
    }

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

    public List<String> getMatches()
    {
        return matches.stream()
                .map(Match::toString)
                .toList();
    }

    public String getUnpaired()
    {
        if (matches.isEmpty())
            return "";
        var paired = matches.stream()
                .flatMap(p -> Stream.of(p.pair1(), p.pair2()))
                .flatMap(p -> Stream.of(p.p1(), p.p2()))
                .map(Player::getName)
                .toList();
        return getPlayers().stream()
                .filter(Predicate.not(paired::contains))
                .collect(Collectors.joining(", "));
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
        deque.push(deque.pollLast());
        return new ArrayList<>(deque);
    }
    
    private List<Match> generateMatches()
    {
        var pairs = generatePairings();
        var p1 = new ArrayDeque<>(pairs.subList(0, pairs.size() / 2));
        var p2 = new ArrayDeque<>(pairs.subList(pairs.size() / 2, pairs.size()));
        if (p2.size() > p1.size())
            p2.pop();
        var answer = new ArrayList<Match>();
        while (!p1.isEmpty())
            answer.add(new Match(p1.pop(), p2.pop()));
        return answer.stream().sorted().toList();
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