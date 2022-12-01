package uk.org.thehickses.csbadders;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "date", "polygon", "pairings" })
public class State
{
    private List<Pairing> pairs;
    private List<String> players;
    private List<Player> polygon;
    private LocalDate date;

    @SuppressWarnings("unused")
    private State()
    {
    }

    public State(List<Pairing> pairs, List<String> players, List<Player> polygon)
    {
        this.pairs = pairs;
        this.players = players;
        this.polygon = polygon;
        this.date = LocalDate.now();
    }

    @JsonIgnore
    public List<Pairing> getPairs()
    {
        return pairs;
    }

    @JsonIgnore
    public List<String> getPlayers()
    {
        return players;
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
        return players.stream()
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
}