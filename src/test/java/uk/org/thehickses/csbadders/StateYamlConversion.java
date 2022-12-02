package uk.org.thehickses.csbadders;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

class StateYamlConversion
{
    @Test
    void testToYaml() throws Exception
    {
        var names = Arrays.asList("Jeremy", "Dan", "Nigel", "Sophie", "Denise", "Pete", "Frank",
                "Hannah", "Helen", "Ciara");
        var players = names.stream()
                .map(n -> new Player(n, 0))
                .peek(p -> p.addCourt(1))
                .toList();
        var pairings = IntStream.range(0, players.size() / 2)
                .map(i -> i * 2)
                .mapToObj(players::listIterator)
                .map(it -> new Pairing(it.next(), it.next()))
                .toList();
        var state = new State(pairings, names, players);
        var yaml = new ObjectMapper(new YAMLFactory().configure(Feature.MINIMIZE_QUOTES, true))
                .writeValueAsString(state);
        // System.out.println(yaml);
        assertThat(yaml).isEqualTo(yaml());
    }

    @Test
    void testFromYaml() throws Exception
    {
        var state = new ObjectMapper(new YAMLFactory()).readValue(yaml().trim(), State.class);
        assertThat(state.getPolygon()
                .stream()
                .map(Player::getName)).containsExactly("Jeremy", "Dan", "Nigel", "Sophie", "Denise",
                        "Pete", "Frank", "Hannah", "Helen", "Ciara");
        state.getPolygon()
                .stream()
                .map(Player::courts)
                .forEach(c -> assertThat(c).containsExactly(0, 0, 0, 0, 1));
        assertThat(state.date()).isEqualTo(java.time.LocalDate.now());
        assertThat(state.getPairs()
                .stream()
                .flatMap(p -> Stream.of(p.p1(), p.p2()))
                .map(Player::getName)).containsExactly("Jeremy", "Dan", "Nigel", "Sophie", "Denise",
                        "Pete", "Frank", "Hannah", "Helen", "Ciara");
    }

    private static final String yaml()
    {
        return YAML.replace("<today>", LocalDate.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    private static final String YAML = """
            ---
            date: <today>
            polygon:
            - name: Jeremy
              courts: 0 0 0 0 1
            - name: Dan
              courts: 0 0 0 0 1
            - name: Nigel
              courts: 0 0 0 0 1
            - name: Sophie
              courts: 0 0 0 0 1
            - name: Denise
              courts: 0 0 0 0 1
            - name: Pete
              courts: 0 0 0 0 1
            - name: Frank
              courts: 0 0 0 0 1
            - name: Hannah
              courts: 0 0 0 0 1
            - name: Helen
              courts: 0 0 0 0 1
            - name: Ciara
              courts: 0 0 0 0 1
            pairings:
            - - Jeremy
              - Dan
            - - Nigel
              - Sophie
            - - Denise
              - Pete
            - - Frank
              - Hannah
            - - Helen
              - Ciara
                        """;
}