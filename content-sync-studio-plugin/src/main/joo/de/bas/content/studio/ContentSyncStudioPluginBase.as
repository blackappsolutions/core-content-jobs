package de.bas.contentsync.studio {
import com.coremedia.cms.editor.configuration.StudioPlugin;
import com.coremedia.cms.editor.sdk.IEditorContext;

[ResourceBundle('com.coremedia.cms.editor.Editor')]
[ResourceBundle('de.bas.contentsync.studio.bundles.ContentSyncContentTypes')]
public class ContentSyncStudioPluginBase extends StudioPlugin {

  public function ContentSyncStudioPluginBase(config:ContentSyncStudioPluginBase = null) {
    super(config)
  }

  override public function init(editorContext:IEditorContext):void {
    super.init(editorContext);

  }

}
}
