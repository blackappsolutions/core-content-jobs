package de.bas.contentsync.beans;

import com.coremedia.blueprint.cae.contentbeans.CMObjectImpl;
import com.coremedia.blueprint.common.contentbeans.CMFolderProperties;
import com.coremedia.blueprint.common.contentbeans.CMObject;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import com.coremedia.objectserver.beans.ContentBean;
import java.util.Calendar;
import java.util.List;

import static de.bas.contentsync.beans.ContentSync.*;

/**
 * Generated base class for beans of document type "ContentSync".
 */
public abstract class ContentSyncBase extends CMObjectImpl {

  /*
   * DEVELOPER NOTE
   * Change {@link de.bas.contentsync.beans.ContentSyncImpl} instead of this class.
   */

  /**
   * Returns the value of the document property "localSettings"
   * @return the value of the document property "localSettings"
   */
  public Struct getLocalSettings() {
    return getContent().getStruct(LOCAL_SETTINGS);
  }


  /**
   * Returns the value of the document property "linkedSettings"
   * @return the value of the document property "linkedSettings"
   */
  public List/*<? extends CMSettings>*/ getLinkedSettings() {
    List/*<Content>*/ contents = getContent().getLinks(LINKED_SETTINGS);
    List/*<? extends CMSettings>*/ contentBeans = (List/*<? extends CMSettings>*/) createBeansFor(contents, ContentBean.class);
    return contentBeans;
  }


  /**
   * Returns the value of the document property "retries"
   * @return the value of the document property "retries"
   */
  public int getRetries() {
    return getContent().getInt(RETRIES);
  }


  /**
   * Returns the value of the document property "lastRun"
   * @return the value of the document property "lastRun"
   */
  public Calendar getLastRun() {
    return getContent().getDate(LAST_RUN);
  }


  /**
   * Returns the value of the document property "sourceFolder"
   * @return the value of the document property "sourceFolder"
   */
  public List<? extends CMFolderProperties> getSourceFolder() {
    List<Content> contents = getContent().getLinks(SOURCE_FOLDER);
    return createBeansFor(contents, CMFolderProperties.class);
  }

  public List<? extends CMObject> getSourceContent() {
    List<Content> contents = getContent().getLinks(SOURCE_CONTENT);
    return createBeansFor(contents, CMObject.class);
  }


  /**
   * Returns the value of the document property "active"
   * @return the value of the document property "active"
   */
  public int getActive() {
    return getContent().getInt(ACTIVE);
  }


  /**
   * Returns the value of the document property "lastRunSuccessful"
   * @return the value of the document property "lastRunSuccessful"
   */
  public int getLastRunSuccessful() {
    return getContent().getInt(LAST_RUN_SUCCESSFUL);
  }


  /**
   * Returns the value of the document property "startAt"
   * @return the value of the document property "startAt"
   */
  public Calendar getStartAt() {
    return getContent().getDate(START_AT);
  }

}
