package uk.org.thehickses.csbadders;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
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
    Bucket storageBucket()
    {
        var storage = Optional.of("LOCAL_STORAGE")
                .map(System::getenv)
                .map(s -> LocalStorageHelper.getOptions())
                .orElse(StorageOptions.getDefaultInstance())
                .getService();
        String bucketName = "csbadders-test.appspot.com";
        return Optional.of(bucketName)
                .map(storage::get)
                .orElseGet(() -> storage.create(BucketInfo.of(bucketName)));
    }

    @Bean
    RequestHandler requestHandler(Bucket bucket, Templater templater)
    {
        return new RequestHandler(bucket, templater);
    }
}
