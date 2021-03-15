package de.bas.contentsync;

import org.graalvm.compiler.lir.CompositeValue;

import java.util.Collection;

/**
 * @author Markus Schwarz
 */
@Component
public class ContentSyncListener extends ContentRepositoryListenerBase {

    private static final String JOB_CONTENT_TYPE = "ContentSync";

    @Inject
    private ContentRepository contentRepository;

    @Value("${initial.query:TYPE ContentSync: NOT isDeleted AND active = 1}")
    private String initialQuery;

    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        QueryService queryService = contentRepository.getQueryService();
        Collection<Content> activeContentSyncs = queryService.poseContentQuery(initialQuery);
        for (Content job : activeContentSyncs) {
            // launch content-sync job
        }
        contentRepository.addContentRepositoryListener(this);
    }

    @Override
    public void versionCreated(VersionCreatedEvent vce) {
        Version version = vce.getVersion();
        if (JOB_CONTENT_TYPE.equals(version.getType().getName())) {
            // when active => launch content-sync job
        }
    }
}
