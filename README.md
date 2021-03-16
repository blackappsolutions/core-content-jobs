# Content Sync

## Overview
CoreMedia CMS Extension to sync Content from A to B.
This is only a shell, where an actual import-export-process can be embedded. It introduces the ContentType `ContentSync`,
which is used as a Job-Definition with the following properties:

  * `sourceFolder`: Create a new resource of type `FolderProperties` and the name `_folderToSync` in the CMS folder you 
    want export/sync, which serves as a marker resource. Add this resource into the `sourceFolder`-Property.
  * `active`: Used to arm this job, when enable. Also check in the resource after setting this property!   
  * (Comming soon)`startAt`: When given and active=true, start the content-sync at the given time.   
  * ...

## Integration into the CoreMedia Blueprints

### Background

Integration of this extension is recommended as **Git SubModule**.
                                                  
Before doing so, make a fork to be able to apply your customizations.

This way, you will be able to merge new commits made in this repo back to your fork.

and add this to your existing CoreMedia Blueprint-Workspace.
 
### HowTo

- From the project's root folder, clone this repository as submodule into the extensions folder. Make sure to use the branch name that matches your workspace version. 
    ```
    git submodule add  https://github.com/blackappsolutions/content-sync.git modules/extensions/content-sync
    ```

- Use the extension tool in the root folder of the project to link the modules into your workspace.
    ```                                                          
    # Should display => #content-sync
    mvn -f workspace-configuration/extensions extensions:list -q
  
    # Shows possible plugin-points. Nice to know.
    # mvn -f workspace-configuration/extensions extensions:list-extension-points -q
    
    # Enables the extension. Check e.g. apps/cae/modules/extension-config/cae-extension-dependencies/pom.xml afterwards. 
    mvn -f workspace-configuration/extensions extensions:sync -Denable=content-sync
  
    # First build, the fastest way ... 
    mvn clean install -DskipTests -DskipThemes=true -DskipContent=true -Dskip-joo-unit-tests=true \ 
                      -Dmdep.analyze.skip=true -Denforcer.skip=true
    ```
- Change the groupId and versionID of all pom.xml to your project values, if neccessary.

- The [schema.xml](../../modules/search/solr-config/src/main/app/configsets/content/conf/schema.xml) (this link only
works, if this code is within a blueprint workspace) of the content config-set must contain these two fields:
    ```
    <field name="active" type="boolean" indexed="true" stored="true"/>
    <field name="startAt" type="pdate" indexed="true" stored="true"/>
    ```
---
Licence was selected with the support of https://choosealicense.com/licenses/
                                       
