package uk.org.thehickses.csbadders;

public class Player
{
    private String name;
    private int courtScore;
    
    public Player(String name, int courtScore)
    {
        this.name = name;
        this.courtScore = courtScore;
    }

    public String name()
    {
        return name;
    }

    public int courtScore()
    {
        return courtScore;
    }

    public void incrementScore(int inc)
    {
        courtScore += inc;
    }
    
    public String toString()
    {
        return "%s %d".formatted(name, courtScore);
    }
    
}
