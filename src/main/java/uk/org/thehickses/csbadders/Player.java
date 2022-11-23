package uk.org.thehickses.csbadders;

import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.util.StringUtils;

public class Player
{
    private final String name;
    private final LinkedList<Integer> courts = new LinkedList<>();
    
    public static Player fromString(String str)
    {
        var it = Stream.of(str.split("\s+")).filter(StringUtils::hasLength).iterator();
        var answer = new Player(it.next());
        while (it.hasNext())
            answer.addCourt(Integer.parseInt(it.next()));
        return answer;
    }
    
    public Player(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public String toString()
    {
        return "%s %s".formatted(name, courts.stream().map(Object::toString).collect(Collectors.joining(" ")));
    }

    public void addCourt(int court)
    {
        courts.addFirst(court);
        if (courts.size() > 5)
            courts.removeLast();
    }
    
    public int sortKey() {
        return courts.stream().reduce((t, i) -> t * 10 + i).orElse(0);
    }
}
