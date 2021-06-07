package de.bas.content.jobs;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import de.bas.content.engine.ContentWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Tasks executed repeatedly must implement Runnable
 */
@Slf4j
@Scope("prototype")
@Component("cleanXmlExportsInS3Bucket")
public class CleanXmlExportsInS3BucketJob extends AbstractContentJob implements Runnable {

    public CleanXmlExportsInS3BucketJob(de.bas.content.beans.ContentJob contentJob, ContentWriter contentWriter) {
        super(contentJob, contentWriter);
    }

    @Override
    protected void doTheJob() {
        log.info("About to start cleanXmlExportsInS3Bucket job");
        AmazonS3 s3client = AmazonS3ClientBuilder
            .standard()
            .build();

        String exportStorageURL = contentJob.getExportStorageURL();
        AmazonS3URI amazonS3URI = new AmazonS3URI(exportStorageURL);
        String bucketName = amazonS3URI.getBucket();
        String key = amazonS3URI.getKey();
        log.info("About to arm deletion request for folder {} in bucket {}.", key, bucketName);

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix(key);

        ObjectListing objectListing;

        do {
            objectListing = s3client.listObjects(listObjectsRequest);
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                Boolean s3BucketCleanupDryRun = contentJob.getS3BucketCleanupDryRun();
                log.info("About to delete {} (dryRun={})", objectSummary.getKey(), s3BucketCleanupDryRun);
                if (!s3BucketCleanupDryRun) {
                    s3client.deleteObject(new DeleteObjectRequest(bucketName, objectSummary.getKey()));
                }

            }
            listObjectsRequest.setMarker(objectListing.getNextMarker());
        } while (objectListing.isTruncated());

        s3client.shutdown();
    }

    @Override
    public void run() {
        try {
            call();
        } catch (Exception e) {
            log.error("Error in run()-Method of {}", contentJob.getContentId(), e);
        }
    }

}
