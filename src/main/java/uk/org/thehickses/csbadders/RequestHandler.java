package uk.org.thehickses.csbadders;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RequestHandler
{
    private final Templater templater;

    public RequestHandler(Templater templater)
    {
        this.templater = templater;
    }

    public String handle(Player[] polygon, String[] names)
    {
        return templater.applyTemplate("home.ftlh", generateOutput(polygon, Stream.of(names)
                .distinct()
                .sorted()
                .toArray(String[]::new)));
    }

    public OutputData generateOutput(Player[] polygon, String[] names)
    {
        if (names.length < 4)
            return new OutputData(new ArrayList<>(), Arrays.asList(names), new ArrayList<>());
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
                .forEach(p -> p.addCourt(index.getAndIncrement() / 4));
        return new OutputData(pairings, Arrays.asList(names), Arrays.asList(newPolygon));
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
                        .map(n -> new Player(n)))
                .toList());
        Collections.shuffle(newList);
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

    public static record Pairing(Player p1, Player p2) implements Comparable<Pairing>
    {
        public int sortKey()
        {
            return Stream.of(p1(), p2())
                    .mapToInt(Player::sortKey)
                    .sorted()
                    .max()
                    .getAsInt();
        }

        @Override
        public int compareTo(Pairing o)
        {
            return Integer.compare(o.sortKey(), this.sortKey());
        }
    }

    public static class OutputData
    {
        private final List<Pairing> pairs;
        private final List<String> players;
        private final List<Player> polygon;

        public OutputData(List<Pairing> pairs, List<String> players, List<Player> polygon)
        {
            this.pairs = pairs;
            this.players = players;
            this.polygon = polygon;
        }

        public List<Pairing> getPairs()
        {
            return pairs;
        }

        public List<String> getPlayers()
        {
            return players;
        }

        public List<Player> getPolygon()
        {
            return polygon;
        }

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

        public String getUnpaired()
        {
            if (pairs.isEmpty())
                return "";
            var paired = pairs.stream()
                    .flatMap(p -> Stream.of(p.p1(), p.p2()))
                    .map(Player::getName)
                    .toList();
            return players.stream()
                    .filter(Predicate.not(paired::contains))
                    .collect(Collectors.joining(", "));
        }
    }
}
