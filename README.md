# Core Content Jobs 

Framework to embed jobs in content, when you don't have an evironment to run jobs (e.g. when you use CMCC-S)

(developed against Version 2010.3)

## Overview
CoreMedia CMS Extension to run any kind of Jobs in your Preview-CAE: 
* Import-/Export-Jobs, 
* CoreMedia-Utilities (Cleanup, Publish, ..)
* create content-related reports
* ...

Especially in the CMCC-S product - hosted by CoreMedia - which allows no Unified-API-Clients in higher environments (UAT/Prod), you can use it as a task-scheduler/runtime-environment.

Embedded actually in the `preview-cae` (but must not necessarily live there).

It introduces the ContentType `ContentSync`, which is used as a Job-Definition with the following properties:

  * `sourceContent` (multiple items are supported): Create a new resource of type `FolderProperties` and name it `_folderToSync` in the CMS folder you 
    want export/sync (serves as a marker resource). Add this resource into the `sourceFolder`-Property.
    Or just supply any other resource.
  * `active`: Used to arm this job, when enabled and the resource was checked-in. This will also cause a check-out/-in of the resource by the provided `content-jobs.user` (see below).
  * `localSettings.start-at` (optional): Supply a date at which this sync job should start.  
  * `localSettings.repeat-every` (optional):
    * `HOUR`
    * `DAY`
    * `WEEK`
  * `localSettings.job-type`: Select different types of syncs: 
    * `rssImport` (you must provide a `sourceContent`-CMFolderProperties resource named `_folderToSync`) 
    * `xmlImport` (you must provide a `localSettings.export-storage-url` fully qualified. E.g.: `s3://blackapp-content-sync/1234.zip`)  
    * `xmlExport` (you must provide a `localSettings.export-storage-url` base url. E.g.: `s3://blackapp-content-sync/`). This is the only job currently which logs to the `logOutput` aka "Execution protocol" field.
    * `cleanXmlExportsInS3Bucket` (document me)
    * `bulkPublish` (document me)
    * `bulkUnpublish` (document me)
  * `localSettings.export-storage-url`: 
    * file:///
    * s3://
    * http(s)://user:pass@host/path
    You can use this property for 
      * `xmlExport`-Jobs to provide a storage location.
      * `xmlImport`-Jobs to provide a zip with content to import.
      * `cleanXmlExportsInS3Bucket`-Jobs to provide a s3-Bucket folder path like `s3://myBucketName/myStorageFolder/`
    <br/><br/>
    **Note:** If you want to use s3 buckets, keep in mind, that you can define only ONE bucket per system at the moment, 
    because it is not possible to pass s3-credentials on the url or on any other way to CoreMedia's ServerExporter, 
    except with the variables 
    * `AWS_ACCESS_KEY_ID` and 
    * `AWS_SECRET_ACCESS_KEY` in the system environment (see global/deployment/docker/compose/default.yml).
  ---
  To make this extension work, you need to create a separate `content-sync`-admin-user, which creates a new version of each active ContentSync-Resource after a job run. You can provide the users name/pass with the following variables in the system environment or application.properties:
  * `CONTENTJOBS_USER` | `content-jobs.user=` and 
  * `CONTENTJOBS_PASS` | `content-jobs.pass=`

  !!! IF THIS USER WAS NOT SET-UP, THE CAE WILL NOT BOOT !!!
  ---

### Current Limitations (further development)
* No content validation
* No retry handling
* No Connection-/Socket-Timeouts

## Integration into the CoreMedia Blueprints

### Background

Integration of this extension is recommended as **Git SubModule**.
                                                  
Before doing so, make a fork to be able to apply your customizations.

This way, you will be able to merge new commits made in this repo back to your fork.
 
### HowTo

- From the project's root folder, clone this repository as submodule into the extensions folder. Make sure to use the branch name that matches your workspace version. 
    ```
    git submodule add https://github.com/blackappsolutions/core-content-jobs.git modules/extensions/core-content-jobs
    ```

- Use the extension tool in the root folder of the project to link the modules into your workspace.
    ```                                                          
    # Should display => #content-sync
    mvn -f workspace-configuration/extensions extensions:list -q
  
    # Shows possible plugin-points. Nice to know.
    # mvn -f workspace-configuration/extensions extensions:list-extension-points -q
    
    # Enables the extension. Check e.g. apps/cae/modules/extension-config/cae-extension-dependencies/pom.xml afterwards. 
    mvn -f workspace-configuration/extensions extensions:sync -Denable=core-content-jobs
  
    # First build, the fastest way ... 
    mvn clean install -DskipTests -DskipThemes=true -DskipContent=true -Dskip-joo-unit-tests=true \ 
                      -Dmdep.analyze.skip=true -Denforcer.skip=true
    ```
- Change the groupId and versionID of all pom.xml to your project values, if necessary.

- if you want to use the task overview page to cancel scheduled job, create
  - a new Placeholder-ViewType `content-sync-jobs` and
  - a Placeholder-Resource that has this ViewType set.
  - Set the Placeholder-Resource in an Article or Page OR
  - issue `/blueprint/servlet/dynamic/content-jobs/terminate/1234`directly.
  
## Further Development
  
### Adapt the `ContentJob` DocType to your needs

* [Server](core-content-jobs-server/src/main/resources/framework/doctypes/core-content-jobs-doctypes.xml)
* [Studio](core-content-jobs-studio-plugin/src/main/joo/de/bas/content/studio/form/ContentJobForm.mxml)
* [CAE](core-content-jobs-cae/src/main/resources/framework/spring/core-content-jobs-contentbeans.xml)<br>
  You can generate the contentbeans from scratch with this command: 
  ```                                 
  cd content-sync-cae
  mvn -PgenerateContentBeans exec:java
  ```
  Vendor-Documentation: [Generate ContentBeans](https://documentation.coremedia.com/cmcc-10/artifacts/2101/webhelp/cae-developer-en/content/GeneratingContentBeans.html)  

### Templates
apps/cae/spring-boot/cae-preview-app/src/main/resources/application-local.properties
```
########################################################################################################################
# Workspace locations for local resource loading
#
# these properties should reference the convenience
# properties above for any workspace location
########################################################################################################################
cae-base-lib.resources=${blueprint-dir}/modules/cae/cae-base-lib/src/main/resources,${blueprint-dir}/modules/cae/cae-base-lib/src/main/resources/META-INF/resources
[..]
content-sync-cae.resources=${blueprint-dir}/../../modules/extensions/core-content-jobs/content-sync-cae/src/main/resources/META-INF/resources

# Load web resources from (local) workspace to support short CAE development round-trips
spring.boot.tomcat.extraResources=\
  [..]
  ${content-sync-cae.resources}
```

---
Licence was selected with the support of https://choosealicense.com/licenses/
                                       
