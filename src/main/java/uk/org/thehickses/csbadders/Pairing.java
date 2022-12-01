package uk.org.thehickses.csbadders;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record Pairing (Player p1, Player p2) implements Comparable<Pairing>
{
    public String sortKey()
    {
        return Stream.of(p1, p2)
                .map(Player::sortKey)
                .flatMap(IntStream::boxed)
                .sorted((a, b) -> Integer.compare(b, a))
                .map("%02d"::formatted)
                .collect(Collectors.joining());
    }

    @Override
    public int compareTo(Pairing o)
    {
        return o.sortKey()
                .compareTo(sortKey());
    }
}
