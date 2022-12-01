package uk.org.thehickses.csbadders;

import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.util.StringUtils;

public class Player
{
    private static final int MAX_COURTS = 5;

    private String name;
    private final LinkedList<Integer> courts = new LinkedList<>();

    public static Player fromString(String str)
    {
        var it = Stream.of(str.split("\s+"))
                .filter(StringUtils::hasLength)
                .iterator();
        var answer = new Player(it.next());
        while (it.hasNext())
            answer.addCourt(Integer.parseInt(it.next()));
        return answer;
    }

    @SuppressWarnings("unused")
    private Player()
    {
    }

    private Player(String name)
    {
        this.name = name;
    }

    public Player(String name, int initCourt)
    {
        this(name);
        IntStream.generate(() -> initCourt)
                .limit(MAX_COURTS)
                .forEach(courts::push);
    }

    public String getName()
    {
        return name;
    }

    public String getCourts()
    {
        return courts.stream()
                .map("%d"::formatted)
                .collect(Collectors.joining(" "));
    }

    @SuppressWarnings("unused")
    private void setCourts(String str)
    {
        courts.clear();
        courts.addAll(Stream.of(str.split(" "))
                .map(Integer::getInteger)
                .toList());
    }

    public String toString()
    {
        return "%s %s".formatted(name, courts.stream()
                .map(Object::toString)
                .collect(Collectors.joining(" ")));
    }

    public void addCourt(int court)
    {
        courts.addLast(court);
        if (courts.size() > MAX_COURTS)
            courts.removeFirst();
    }

    public IntStream sortKey()
    {
        return IntStream.range(0, courts.size())
                .map(i -> (i + 1) * courts.get(i));
    }
}
