package uk.org.thehickses.csbadders;

import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller
{
    public static Logger LOG = LoggerFactory.getLogger(Controller.class);

    @Autowired
    RequestHandler reqHandler;

    @GetMapping("/")
    public String doGet()
    {
        return reqHandler.handle(new Player[0], new String[0]);
    }

    @PostMapping("/")
    public String doPost(@RequestParam(defaultValue = "") String polygon,
            @RequestParam(defaultValue = "") String names)
    {
        try
        {
            return reqHandler.handle(extractPlayers(polygon), extractStrings(names));
        }
        catch (Throwable ex)
        {
            LOG.error("Unexpected error", ex);
            throw ex;
        }
    }

    private Player[] extractPlayers(String str)
    {
        return extractLines(str).map(Player::fromString)
                .toArray(Player[]::new);
    }

    private String[] extractStrings(String str)
    {
        return extractLines(str).toArray(String[]::new);
    }

    private Stream<String> extractLines(String str)
    {
        return str.lines()
                .filter(StringUtils::hasLength)
                .map(String::trim);
    }
}
