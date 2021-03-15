package de.bas.contentsync;

import org.graalvm.compiler.lir.CompositeValue;

import java.util.Collection;

/**
 * @author Markus Schwarz
 */
@Component
public class ContentSyncListener extends ContentRepositoryListenerBase {

    @Inject
    private ContentRepository contentRepository;

    @Value("${initial.query:TYPE ContentSync: NOT isDeleted AND active = 1}")
    private String initialQuery;

    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        try {
            QueryService queryService = contentRepository.getQueryService();
            Collection<Content> activeContentSyncs = queryService.poseContentQuery(initialQuery);
            for (Content job : activeContentSyncs) {
                // launch content-sync job
            }
        } catch (Exception e) {
            LOG.error("afterPropertiesSet()", e);
        }
        contentRepository.addContentRepositoryListener(this);
    }

}
