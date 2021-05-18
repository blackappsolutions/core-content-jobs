package de.bas.content.beans;

import com.coremedia.blueprint.cae.contentbeans.CMObjectImpl;
import com.coremedia.blueprint.common.contentbeans.CMObject;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;

import java.util.Calendar;
import java.util.List;

import static de.bas.content.beans.ContentJob.ACTIVE;
import static de.bas.content.beans.ContentJob.LAST_RUN;
import static de.bas.content.beans.ContentJob.LAST_RUN_SUCCESSFUL;
import static de.bas.content.beans.ContentJob.LOCAL_SETTINGS;
import static de.bas.content.beans.ContentJob.SOURCE_CONTENT;

/**
 * Generated base class for beans of document type "ContentSync".
 */
public abstract class ContentJobBase extends CMObjectImpl {

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
   * Returns the value of the document property "lastRun"
   * @return the value of the document property "lastRun"
   */
  public Calendar getLastRun() {
    return getContent().getDate(LAST_RUN);
  }


  /**
   * Returns the value of the document property "sourceContent"
   * @return the value of the document property "sourceContent"
   */
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
}
