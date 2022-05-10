import JsonStore from "@jangaroo/ext-ts/data/JsonStore";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import BlueprintTabs_properties from "@coremedia-blueprint/studio-client.main.blueprint-forms/BlueprintTabs_properties";
import LinkedSettingsForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/LinkedSettingsForm";
import LocalSettingsForm from "@coremedia-blueprint/studio-client.main.blueprint-forms/forms/containers/LocalSettingsForm";
import session from "@coremedia/studio-client.cap-rest-client/common/session";
import DocumentForm from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentForm";
import DocumentInfo from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentInfo";
import DocumentTabPanel from "@coremedia/studio-client.main.editor-components/sdk/premular/DocumentTabPanel";
import PropertyFieldGroup from "@coremedia/studio-client.main.editor-components/sdk/premular/PropertyFieldGroup";
import ReferrerListPanel from "@coremedia/studio-client.main.editor-components/sdk/premular/ReferrerListPanel";
import VersionHistory from "@coremedia/studio-client.main.editor-components/sdk/premular/VersionHistory";
import BooleanPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/BooleanPropertyField";
import ComboBoxStringPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/ComboBoxStringPropertyField";
import DateTimePropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/DateTimePropertyField";
import IntegerPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/IntegerPropertyField";
import LinkListPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/LinkListPropertyField";
import RichTextPropertyField from "@coremedia/studio-client.main.ckeditor4-components/fields/RichTextPropertyField";
import StringPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/StringPropertyField";
import TextBlobPropertyField from "@coremedia/studio-client.main.editor-components/sdk/premular/fields/TextBlobPropertyField";
import LabelableSkin from "@coremedia/studio-client.ext.ui-components/skins/LabelableSkin";

interface ContentJobFormConfig extends Config<DocumentTabPanel> {
}

/* https://documentation.coremedia.com/cmcc-10/artifacts/2101/webhelp/studio-developer-en/content/CustomizingForms.html */
class ContentJobForm extends DocumentTabPanel {
  declare Config: ContentJobFormConfig;

  static override readonly xtype: string = "de.bas.content.studio.form.contentJobForm";

  isAdmin(): boolean {
    return session._.getUser().isAdministrative();
  }

  constructor(config: Config<ContentJobForm> = null) {
    super((()=> ConfigUtils.apply(Config(ContentJobForm, {
      items: [
        Config(DocumentForm, {
          title: BlueprintTabs_properties.Tab_content_title,
          items: [
            Config(BooleanPropertyField, {
              propertyName: "active",
              readOnly: ! this.isAdmin(),
            }),
            Config(ComboBoxStringPropertyField, {
              itemId: "job-type",
              ui: LabelableSkin.PLAIN_LABEL.getSkin(),
              propertyName: "localSettings.job-type",
              fieldWidth: 200,
              valueField: "value",
              displayField: "value",
              reverseTransformer: (value: any): any => value === null ? "" : value,
              store: new JsonStore({
                fields: ["value"],
                data: [
                  { value: "xmlExport" },
                  { value: "xmlImport" },
                  { value: "cleanXmlExportsInS3Bucket" },
                  { value: "bulkPublish" },
                  { value: "bulkUnpublish" },
                  { value: "rssImport" },
                  { value: "migrateSourceContentLinklistIntoLocalSettings" },
                ],
              }),
            }),
            Config(LinkListPropertyField, {
              propertyName: "localSettings.sourceContent",
              linkType: "Content_",
            }),
            Config(RichTextPropertyField, { propertyName: "localSettings.sourceContentPaths" }),
            Config(BooleanPropertyField, {
              propertyName: "localSettings.recursive",
              dontTransformToInteger: true,
            }),
            Config(StringPropertyField, {
              propertyName: "localSettings.export-storage-url",
              /*editor:plugins exml:mode="append">
            <ui:AddItemsPlugin>
              <ui:items>
                <Label html="{resourceManager.getString('com.vfcorp.studio.VfCorpContenttypes', 'CMTeasable_localSettings.openGraph.ogImage_help')}" />
              </ui:items>
              <ui:before>
                <Component itemId="linkGrid" />
              </ui:before>
            </ui:AddItemsPlugin>
          </editor:plugins */
            }),
            Config(StringPropertyField, { propertyName: "localSettings.zip-directory" }),
            Config(PropertyFieldGroup, {
              itemId: "scheduling",
              title: "Scheduling",
              collapsed: true,
              items: [
                Config(DateTimePropertyField, { propertyName: "localSettings.start-at" }),
                Config(ComboBoxStringPropertyField, {
                  itemId: "run-job-every",
                  ui: LabelableSkin.PLAIN_LABEL.getSkin(),
                  propertyName: "localSettings.run-job-every",
                  fieldWidth: 200,
                  valueField: "value",
                  displayField: "value",
                  reverseTransformer: (value: any): any => value === null ? "" : value,
                  store: new JsonStore({
                    fields: ["value"],
                    data: [
                      { value: "MINUTE" },
                      { value: "HOUR" },
                      { value: "DAY" },
                      { value: "WEEK" },
                    ],
                  }),
                }),
              ],
            }),
            Config(PropertyFieldGroup, {
              itemId: "last-run",
              title: "Last run",
              collapsed: true,
              items: [
                Config(TextBlobPropertyField, {
                  propertyName: "logOutput",
                  height: 300,
                }),
                Config(DateTimePropertyField, {
                  propertyName: "lastRun",
                  disabled: true,
                }),
                Config(IntegerPropertyField, {
                  propertyName: "lastRunSuccessful",
                  disabled: true,
                }),
              ],
            }),
          ],
        }),
        Config(DocumentForm, {
          title: BlueprintTabs_properties.Tab_system_title,
          itemId: "system",
          autoHide: true,
          items: [
            Config(DocumentInfo),
            Config(VersionHistory),
            Config(ReferrerListPanel),
            Config(LinkedSettingsForm, {
              bindTo: config.bindTo,
              collapsed: true,
            }),
            Config(LocalSettingsForm, {
              bindTo: config.bindTo,
              collapsed: true,
            }),
            Config(LinkListPropertyField, { propertyName: "sourceContent" }),
          ],
        }),
      ],

    }), config))());
  }
}

export default ContentJobForm;
