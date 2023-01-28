package uk.org.thehickses.csbadders;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record Match(Pairing pair1, Pairing pair2) implements Comparable<Match>
{
    public String sortKey()
    {
        return Stream.of(pair1, pair2)
                .flatMap(p -> Stream.of(p.p1(), p.p2()))
                .map(Player::sortKey)
                .flatMap(IntStream::boxed)
                .sorted((a, b) -> Integer.compare(b, a))
                .map("%02d"::formatted)
                .collect(Collectors.joining());
    }

    @Override
    public int compareTo(Match o)
    {
        return o.sortKey()
                .compareTo(sortKey());
    }

    @Override
    public String toString()
    {
        return Stream.of(pair1, pair2)
                .map(Pairing::toString)
                .collect(Collectors.joining(" vs "));
    }
}
