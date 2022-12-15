package uk.org.thehickses.csbadders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
        return reqHandler.handle(Arrays.asList(), Optional.empty());
    }

    @PostMapping("/")
    public String doPost(@RequestParam String polygon, @RequestParam String names)
    {
        try
        {
            return reqHandler.handle(extractStrings(names), Optional.of(polygon)
                    .map(this::extractLines));
        }
        catch (Throwable ex)
        {
            LOG.error("Unexpected error", ex);
            throw ex;
        }
    }

    private List<String> extractStrings(String str)
    {
        return extractLines(str).map(s -> s.replaceAll("\\s+", " "))
                .distinct()
                .toList();
    }

    private Stream<String> extractLines(String str)
    {
        return str.lines()
                .filter(StringUtils::hasLength)
                .map(String::trim);
    }
}
