package de.bas.content.studio {
import com.coremedia.cms.editor.configuration.StudioPlugin;
import com.coremedia.cms.editor.sdk.IEditorContext;

[ResourceBundle('com.coremedia.cms.editor.Editor')]
[ResourceBundle('de.bas.content.studio.bundles.ContentJobContentTypes')]
public class ContentJobStudioPluginBase extends StudioPlugin {

  public function ContentJobStudioPluginBase(config:ContentJobStudioPluginBase = null) {
    super(config)
  }

  override public function init(editorContext:IEditorContext):void {
    super.init(editorContext);

  }

}
}
