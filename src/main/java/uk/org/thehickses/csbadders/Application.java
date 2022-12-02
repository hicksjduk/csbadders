package uk.org.thehickses.csbadders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
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
    StorageInfo storageInfo()
    {
        var bucketConfig = System.getenv("STATE_BUCKET");
        if (bucketConfig == null)
        {
            throw new RuntimeException("STATE_BUCKET environment variable not set");
        }
        boolean local = bucketConfig.startsWith("local:");
        var bucketName = local ? bucketConfig.substring(6) : bucketConfig;
        Storage storage = (local ? LocalStorageHelper.getOptions()
                : StorageOptions.getDefaultInstance()).getService();
        return new StorageInfo(storage, bucketName);
    }

    @Bean
    ObjectMapper yamlMapper()
    {
        return new ObjectMapper(new YAMLFactory());
    }

    @Bean
    RequestHandler requestHandler(StorageInfo storage, ObjectMapper yamlMapper, Templater templater)
    {
        return new RequestHandler(storage, yamlMapper, templater);
    }

    public static record StorageInfo(Storage storage, String bucketName)
    {
        public Blob get(String name)
        {
            return storage.get(bucketName, name);
        }
        
        public void insert(String name, byte[] data)
        {
            storage.create(BlobInfo.newBuilder(BlobId.of(bucketName, name)).build(), data);
        }
    }
}
