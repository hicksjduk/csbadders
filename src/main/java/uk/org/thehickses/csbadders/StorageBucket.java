package uk.org.thehickses.csbadders;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;

public record StorageBucket(Storage storage, String bucketName)
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