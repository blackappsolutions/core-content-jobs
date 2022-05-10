import Config from "@jangaroo/runtime/Config";
import StudioPlugin from "@coremedia/studio-client.main.editor-components/configuration/StudioPlugin";
import IEditorContext from "@coremedia/studio-client.main.editor-components/sdk/IEditorContext";

interface ContentJobStudioPluginBaseConfig extends Config<StudioPlugin> {
}

class ContentJobStudioPluginBase extends StudioPlugin {
  declare Config: ContentJobStudioPluginBaseConfig;

  constructor(config: Config<ContentJobStudioPluginBase> = null) {
    super(config);
  }

  override init(editorContext: IEditorContext): void {
    super.init(editorContext);

  }

}

export default ContentJobStudioPluginBase;
