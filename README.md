**Core Content Jobs**
=====================

[Overview](#Overview) | [RSS Import](#rssImport) |  [XML Export](#xmlExport) | [XML Import](#xmlImport) | [CleanS3Bucket](#cleanXmlExportsInS3Bucket) | [Mass Publication](#bulk) | [Mass Withdrawal](#bulk)

Overview
--------
[Core Content Jobs](https://github.com/blackappsolutions/core-content-jobs) is a extensible framework provided by [Black App Solutions](https://black-app-solutions.de/). It introduces a new DocType "ContentJob" and uses the Preview-CAE as a runtime environment due to the limitations of CMCC-S. It comes pre-packaged with the following Jobs:

![](attachments/114566139/114568173.png?effects=border-simple,shadow-kn)
------------------------------------------------------------------------

But you can also develop new jobs very easy:

1.  Create a new class in modules/extensions/blackapp-core-content-jobs/core-content-jobs-cae/src/main/java/de/bas/content/jobs
2.  The class must extend de.bas.content.jobs.AbstractContentJob and should provide the following annotations
    ```
    @Slf4j
    @Scope("prototype")
    @Component("rssImport")
    public class ImportRSSJob extends AbstractContentJob {
    ```
    \=> After deplyoment, this class will be usable with the name **rssImport** in **localSettings.job-type** of ContentJob-resources in CMS.  
    
------------------------------------------------------------------------
**ContentJobs can be scheduled**

![](attachments/114566139/114566304.png)

------------------------------------------------------------------------
**ContentJobs can be triggered from external systems**
1. Prepare a ContentJob,
2. set the `web-trigger-allowed` property in `localSettings` to `true`
3. call https://preview.YOUR_HOST/blueprint/servlet/dynamic/content-jobs/execute/123456/
(for such a ContentJob, you don't need to set the `active` flag to `true`)

------------------------------------------------------------------------
**ContentJobs can be monitored**

There is also a [Freemarker-Template](https://github.com/blackappsolutions/core-content-jobs/blob/main/core-content-jobs-cae/src/main/resources/META-INF/resources/WEB-INF/templates/content-jobs/com.coremedia.blueprint.common.contentbeans/CMPlaceholder.%5Bcontent-jobs%5D.ftl) available to maintain long-running/scheduled jobs.

If you want to use the task overview page to cancel scheduled job, create

*   a new Placeholder-ViewType `content-sync-jobs` and
*   a Placeholder-Resource that has this ViewType set.
*   Set the Placeholder-Resource in an Article or Page OR
*   issue `/blueprint/servlet/dynamic/content-jobs/terminate/1234` directly.

------------------------------------------------------------------------
**ContentJobs can be paused**

To switch the whole engine off, when things go wrong (or you need to develop), use                   
```
https://preview.YOUR_HOST/blueprint/servlet/dynamic/content-jobs?enable=false`
```                                                                        
------------------------------------------------------------------------
**ContentJobs can be activated only by Users which are members of an administrative group**

When you are logged in as such a user you are able to check the "active" checkbox and arm the job.
------------------------------------------------------------------------

See the [Integration Guide](README-technical.md) for more information how to bring that extension into your CoreMedia Blueprints workspace.

* * *

rssImport
---------

Serves only as a blueprint/template for new jobs and wants to show, that you can use this framwork also to do content imports. See source code for details => [https://github.com/blackappsolutions/core-content-jobs/blob/main/core-content-jobs-cae/src/main/java/de/bas/content/jobs/ImportRSSJob.java](https://github.com/blackappsolutions/core-content-jobs/blob/main/core-content-jobs-cae/src/main/java/de/bas/content/jobs/ImportRSSJob.java)

*   Imports RSS from locaSettings.rss-import-url if specified. Otherwise uses "https://rss.nytimes.com/services/xml/rss/nyt/Technology.xml" as default.
*   If you supply another RSS-feed, its structure must match the one in the default-feed above.
*   Drag&Drop a folder into the field "The folder/content which should be synced"
*   RSS-Item->title-Attribute is mapped to CMArticle→title
*   RSS-Item->description-Attribute is mapped to CMArticle→detailText
*   For every RSS-Item an Article is created in the folder provided above [after the job was started](#startJob).
*   Articles were name with this pattern:
    
    RssImport\_" \+ System.currentTimeMillis()

xmlExport
---------

This Job makes use of [CoreMedia's ServerExport Tool](https://documentation.coremedia.com/cmcc-10/artifacts/2010/webhelp/contentserver-en/content/CMServerimportExport.html#d0e17572) by [taking the given content, recursive and the zip-url](https://github.com/blackappsolutions/core-content-jobs/blob/313dda3a416a548facd8605ab988edbe44bf3530/core-content-jobs-cae/src/main/java/de/bas/content/jobs/ExportXMLJob.java#L48). See below how this instrumentation is made in the CMS.

*   In Studio go to "/All Content/Settings/Options/Settings/Content Jobs"
*   Create a new content item of type "ContentJob"
*   set the **Job type** to "xmlExport"
*   Add content/folders you would like to export to the field **The folder/content which schould be synced** by using drag&drop

**![](attachments/114566139/114566172.png)**

*   or fill in content paths of resources (or folders) in the field **If not given above, you can specify content paths line by line here**
*   **Note**: You can check **Sync Recursive**, if you have provided content item(s) of type "Folder Properties" to **The folder/content which schould be synced** and sync this folder with all of its subfolders.
*   <a name="startJob"></a>To start the job, you just need to check **active** and push the **Finish editing and apply all changes button**

**![](attachments/114566139/114566158.png)**

*   When the job is done,
    *   you find a new version of this ContentJob created by user **content-jobs-user**  
        **![](attachments/114566139/114566160.png)**
    *   The **active** flag will be unchecked by **content-jobs-user**
    *   If the job was successful, you find a "1" at **Last run / Last run was successful?** otherweise a "0"
    *   When things went fine, you find a protocol at **Last run / Execution protocol** 
        ![](attachments/114566139/114566173.png) 
        and 
    *   you can find your content zipped at s3 (s3://YOUR_BUCKET_NAME/ID_OF_CONTENTJOB.zip) for later re-use in imports.
    *   from where you can grap it for examination via this url => https://preview.YOUR_HOST/blueprint/servlet/dynamic/content-jobs/s3download?bucketUrl=s3://YOUR_BUCKET_NAME/ID_OF_CONTENTJOB.zip  

xmlImport
---------

This Job makes use of [CoreMedia's ServerImport Tool](https://documentation.coremedia.com/cmcc-10/artifacts/2010/webhelp/contentserver-en/content/CMServerimportExport.html#cm:serverimport) by [taking the recursive flag and the zip-url](https://github.com/blackappsolutions/core-content-jobs/blob/313dda3a416a548facd8605ab988edbe44bf3530/core-content-jobs-cae/src/main/java/de/bas/content/jobs/ImportXMLJob.java#L26) and the following defaults (which could be changed as Boolean-Properties in Local Settings):

    * xmlImport-haltOnError: false
    * xmlImport-validateXml: false
    * xmlImport-skipEntities: false
    * xmlImport-skipUuids: true

See below how this instrumentation is made in the CMS.

*   In your Studio go to "/All Content/Settings/Options/Settings/Content Jobs"
*   Create a new ContentJob
*   Set **Job type** to xmlImport
*   Check **Sync recursive**
*   Set **Storage-URL** to s3://YOUR_BUCKET_NAME/203594.zip
*   Check **active** and push the **Finish editing and apply all changes button**

![](attachments/114566139/114566194.png)

cleanXmlExportsInS3Bucket
-------------------------

This job takes care of keeping the s3 bucket clean and can be run (as all other jobs also) scheduled.

To instrument this job, the following properties needs to be set:

*   **Storage-URL** (s3://YOUR_BUCKET_NAME/FOLDER)
*   localSettings.s3-bucket-cleanup-dryrun (true/false)
    

<a name="bulk"></a>bulkPublish / bulkUnpublish
----------------------------------------------

Runs the CoreMedia tool [BulkPublish](https://documentation.coremedia.com/cmcc-10/artifacts/2104/webhelp/contentserver-en/content/bulkpublish.html) with the following default parameters: `--verbose --checkin --approve --publish` (or `--unpublish`)

To instrument this job, the following properties needs to be set:  

*   [Define a folder](#defineFolder)
*   Define "Job type" **bulkPublish**
*   Check **active** and push the **Finish editing and apply all changes button**
