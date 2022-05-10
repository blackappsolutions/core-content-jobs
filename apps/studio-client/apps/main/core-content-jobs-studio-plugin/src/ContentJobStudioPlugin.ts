import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import resourceManager from "@jangaroo/runtime/l10n/resourceManager";
import ContentTypes_properties from "@coremedia/studio-client.cap-base-models/content/ContentTypes_properties";
import Validators_properties from "@coremedia/studio-client.ext.errors-validation-components/validation/Validators_properties";
import CopyResourceBundleProperties from "@coremedia/studio-client.main.editor-components/configuration/CopyResourceBundleProperties";
// import NewContentMenu from "@coremedia/studio-client.main.editor-components/sdk/newcontent/NewContentMenu";
import AddTabbedDocumentFormsPlugin from "@coremedia/studio-client.main.editor-components/sdk/plugins/AddTabbedDocumentFormsPlugin";
import TabbedDocumentFormDispatcher from "@coremedia/studio-client.main.editor-components/sdk/premular/TabbedDocumentFormDispatcher";
// import QuickCreateMenuItem from "com.coremedia.ui.sdk__editor-components/sdk/quickcreate/QuickCreateMenuItem";
// import AddItemsPlugin from "com.coremedia.ui.toolkit__ui-components/plugins/AddItemsPlugin";
import ContentJobStudioPluginBase from "./ContentJobStudioPluginBase";
import ContentJobContentTypes_properties from "./bundles/ContentJobContentTypes_properties";
import FormValidation_properties from "./bundles/FormValidation_properties";
import ContentJobForm from "./form/ContentJobForm";

interface ContentJobStudioPluginConfig extends Config<ContentJobStudioPluginBase> {
}

class ContentJobStudioPlugin extends ContentJobStudioPluginBase {
  declare Config: ContentJobStudioPluginConfig;

  static readonly xtype: string = "de.bas.content.studio.contentJobStudioPlugin";

  constructor(config: Config<ContentJobStudioPlugin> = null) {
    super(ConfigUtils.apply(Config(ContentJobStudioPlugin, {

      rules: [

        Config(TabbedDocumentFormDispatcher, {
          plugins: [
            Config(AddTabbedDocumentFormsPlugin, {
              documentTabPanels: [
                Config(ContentJobForm, { itemId: "ContentJob" }),
              ],
            }),
          ],
        }),

        // Config(NewContentMenu, {
        //   plugins: [
        //     Config(AddItemsPlugin, {
        //       index: 0,
        //       items: [
        //         Config(QuickCreateMenuItem, { contentType: "ContentJob" }),
        //       ],
        //     }),
        //   ],
        // }),

      ],

      configuration: [

        new CopyResourceBundleProperties({
          destination: resourceManager.getResourceBundle(null, ContentTypes_properties),
          source: resourceManager.getResourceBundle(null, ContentJobContentTypes_properties),
        }),

        new CopyResourceBundleProperties({
          destination: resourceManager.getResourceBundle(null, Validators_properties),
          source: resourceManager.getResourceBundle(null, FormValidation_properties),
        }),

      ],

    }), config));
  }
}

export default ContentJobStudioPlugin;
