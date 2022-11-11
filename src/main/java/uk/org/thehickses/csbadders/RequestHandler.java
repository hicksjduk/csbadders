package uk.org.thehickses.csbadders;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
        return templater.applyTemplate("home.ftlh", generateOutput(polygon, names));
    }

    public OutputData generateOutput(Player[] polygon, String[] names)
    {
        if (names.length < 5)
            return new OutputData(new ArrayList<>(), Arrays.asList(names), new ArrayList<>());
        Player[] newPolygon = newPolygon(polygon, names);
        var pairings = getPairings(newPolygon);
        var paired = new ArrayList<Pairing>();
        var byScore = pairings.stream()
                .collect(Collectors.groupingBy(p -> pairings.indexOf(p) / 2));
        byScore.entrySet()
                .forEach(e ->
                    {
                        var score = e.getKey();
                        var pairs = e.getValue();
                        var players = pairs.stream()
                                .flatMap(p -> Stream.of(p.p1(), p.p2()))
                                .toList();
                        players.stream()
                                .forEach(p -> p.incrementScore(score));
                        if (players.size() == 4)
                            paired.addAll(pairs);
                    });
        return new OutputData(paired, Arrays.asList(names), Arrays.asList(newPolygon));
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
                        .map(Player::name)
                        .toList()::contains));
        var newNames = nameStatuses.get(false);
        var existingNames = nameStatuses.get(true);
        if (newNames.isEmpty() && polygon.length == names.length)
            return rotate(polygon);
        var newList = new ArrayList<>(Stream.concat(Stream.of(polygon)
                .filter(p -> existingNames.contains(p.name())),
                newNames.stream()
                        .map(n -> new Player(n, 0)))
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
        public int courtScores()
        {
            return Stream.of(p1(), p2())
                    .mapToInt(Player::courtScore)
                    .sum();
        }

        @Override
        public int compareTo(Pairing o)
        {
            return Integer.compare(o.courtScores(), this.courtScores());
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
                            .map(Player::name)
                            .collect(Collectors.joining(" & ")))
                    .iterator();
            var matchStrings = new ArrayList<String>();
            while (it.hasNext())
                matchStrings.add(Stream.of(it.next(), it.next())
                        .collect(Collectors.joining(" vs ")));
            return matchStrings;
        }

        public String getUnpaired()
        {
            if (pairs.isEmpty())
                return "";
            var paired = pairs.stream()
                    .flatMap(p -> Stream.of(p.p1(), p.p2()))
                    .map(Player::name)
                    .toList();
            return players.stream()
                    .filter(Predicate.not(paired::contains))
                    .collect(Collectors.joining(", "));
        }
    }
}
