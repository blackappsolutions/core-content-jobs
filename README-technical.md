# Core Content Jobs Integration Guide

## Core Config
        
If you want to use s3 buckets, keep in mind, that you can define only ONE bucket per system at the moment, 
because it is not possible to pass s3-credentials on the url or on any other way to CoreMedia's ServerExporter, 
except with this variables
* `AWS_REGION`,    
* `AWS_ACCESS_KEY_ID` and 
* `AWS_SECRET_ACCESS_KEY` in the system environment.

You can set these variables in the docker ecosystem 
  * in some docker-compose file like `global/deployment/docker/compose/default.yml` or
  * `apps/cae/docker/cae-preview/Dockerfile` directly (no other chance on CMCC-S).       

## Integration into the CoreMedia Blueprints

### Background

Integration of this extension is recommended as **Git SubModule**.
                                                  
Before doing so, make a fork to be able to apply your customizations.

This way, you will be able to merge new commits made in this repo back to your fork.
 
### Integrate with Git Submodule Approach

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
### Configure to you needs
- Change the groupId and versionID of all pom.xml to your project values, if necessary.

- if you want to have more log output, use
  ```properties
  logging.level.de.bas.content=debug
  ```         
  in your `apps/cae/spring-boot/cae-preview-app/src/main/resources/application.properties`.
  
## How to enhance
  
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

### Templates-Devlopment
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
content-jobs-cae.resources=${blueprint-dir}/../../modules/extensions/core-content-jobs/core-content-jobs-cae/src/main/resources/META-INF/resources

# Load web resources from (local) workspace to support short CAE development round-trips
spring.boot.tomcat.extraResources=\
  [..]
  ${content-jobs-cae.resources}
```

---
Licence was selected with the support of https://choosealicense.com/licenses/
                                       
