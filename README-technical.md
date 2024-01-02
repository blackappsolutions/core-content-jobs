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

### Background: Integration as git submodule

Integration of this extension is recommended as **Git SubModule**. Before doing so, make a fork if it (on GitHub just press the "Fork" Button) to be able to persist your customizations later (if any).

This way you can merge back new stuff from this repo very easy like this:
```shell
git add remote vendor_repo_github https://github.com/blackappsolutions/core-content-jobs.git 
git fetch vendor_repo_github
git merge vendor_repo_github/cmcc-11
```
### Plug it in

1. Add the submodule
    ```shell
    cd $BLUEPRINTS_ROOT/modules/extensions
    git clone https://github.com/blackappsolutions/core-content-jobs.git
    cd core-content-jobs
    git checkout cmcc-11
    cd $BLUEPRINTS_ROOT 
    git submodule add https://github.com/blackappsolutions/core-content-jobs.git modules/extensions/core-content-jobs
    ```
2. Add the Java part of the extension to the maven reactor
    ```shell
    # Should display => #core-content-jobs
    mvn -f workspace-configuration/extensions extensions:list -q
    
    # Enables the extension. Check e.g. apps/cae/modules/extension-config/cae-extension-dependencies/pom.xml afterwards. 
    mvn -f workspace-configuration/extensions extensions:sync -Denable=core-content-jobs
    
    # First build, the fastest way ... 
    mvn clean install -DskipTests -DskipContent=true -Dmdep.analyze.skip=true -Denforcer.skip=true
    ```
3. Add the Typescript part of the extension to the studio-client
   1. In the workspace root directory: 
       ```shell
       ln -s \ 
       modules/extensions/core-content-jobs/apps/studio-client/apps/main/core-content-jobs-studio-plugin \
       $(pwd)/apps/studio-client/apps/main/extensions/
       ```
   2. Add a new line under `packages` in `apps/studio-client/pnpm-workspace.yaml` 
       ```yaml
       - "../../modules/extensions/core-content-jobs/apps/studio-client/apps/main/core-content-jobs-studio-plugin"
       ```
   3. Add a new dependency in `apps/studio-client/apps/main/extension-config/extension-dependencies/package.json`
       ```json
       "dependencies": {
         ...
         "@coremedia-blueprint/studio-client.main.core-content-jobs-studio-plugin": "1.0.0-SNAPSHOT",
         ...
       }       
       ```
   4. Set up your environment with
      1. NodeJS 18
      2. [pnpm](https://pnpm.io/installation) 
      3. Sencha-Cmd v7.2.0.84
      4. Access to the [NPM-Registry `npm.coremedia.io`](https://documentation.coremedia.com/cmcc-11/artifacts/2310/webhelp/coremedia-en/content/Prerequisites.html#d0e2306)
      
   5. Build the studio-client with
      ```shell
      cd apps/studio-client             
      nvm use 18 # if you have multiple node versions on your machine and nvm in place
      export PUPPETEER_SKIP_DOWNLOAD=true # only on M1/M2-Macs      
      pnpm install
      pnpm -r run build            
      ```               
   6. Optional: Run the studio client (you need a running local docker environment for the command below)
      ```shell                 
      cd global/studio # you are in apps/studio-client 
      pnpm run start                                                                                 
      ```                                                                                      
      For further information see https://documentation.coremedia.com/cmcc-11/artifacts/2310/webhelp/studio-developer-en/content/clientDevelopment.html#d0e5550
   7. Background information on how studio-client extensions are set up: https://documentation.coremedia.com/cmcc-11/artifacts/2310/webhelp/coremedia-en/content/plugins_starter_kit.html  
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

### Templates-Development
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
                                       
