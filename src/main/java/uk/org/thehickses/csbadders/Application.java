package uk.org.thehickses.csbadders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.contrib.nio.testing.LocalStorageHelper;

@SpringBootApplication
public class Application
{
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args)
    {
        try
        {
            SpringApplication.run(Application.class, args);
        }
        catch (Throwable ex)
        {
            LOG.error("Unexpected error", ex);
        }
    }

    @Bean
    Templater templater()
    {
        return new Templater("templates");
    }

    @Bean
    StorageBucket storageInfo()
    {
        var bucketConfig = System.getenv("STATE_BUCKET");
        if (bucketConfig == null)
            throw new RuntimeException("STATE_BUCKET environment variable not set");
        if (bucketConfig.startsWith("local:"))
            return new StorageBucket(LocalStorageHelper.getOptions()
                    .getService(), bucketConfig.substring(6));
        return new StorageBucket(StorageOptions.getDefaultInstance()
                .getService(), bucketConfig);
    }

    @Bean
    ObjectMapper yamlMapper()
    {
        return new ObjectMapper(new YAMLFactory().configure(Feature.MINIMIZE_QUOTES, true));
    }

    @Bean
    RequestHandler requestHandler(StorageBucket storage, ObjectMapper yamlMapper,
            Templater templater)
    {
        return new RequestHandler(storage, yamlMapper, templater);
    }
}
