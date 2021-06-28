package de.bas.content.jobs;

import de.bas.content.beans.ContentJob;
import de.bas.content.engine.ContentWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Markus Schwarz
 */
@Slf4j
@Scope("prototype")
@Component("groovyJob")
public class GroovyJob extends AbstractContentJob {
    public GroovyJob(ContentJob contentJob, ContentWriter contentWriter) {
        super(contentJob, contentWriter);
    }

    @Override
    void doTheJob() throws Exception {
        // docker/management-tools/target/tools/bin/groovysh.profile
        // org.codehaus.groovy.tools.shell.Main
        String groovy = "private void createUserAndAddToGroup(ur, username, password, group) {\n" +
            "  System.out.println(\"About to create user '$username' with the following groups '$group', if not existant\")\n" +
            "  if (ur.getUserByName(username, null) == null) {\n" +
            "    newUser = ur.createUser(username, password)\n" +
            "    for (String groupName : group.split(','))\n" +
            "      ur.getGroupByName(groupName).addMember(newUser)\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "con = com.coremedia.cap.Cap.connect(\"http://content-management-server:8080/ior\", \"admin\", \"admin\")\n" +
            "ur = con.getUserRepository() \n" +
            "\n" +
            "createUserAndAddToGroup(ur, \"test1\", \"test1\", \"staff\")\n" +
            "\n" +
            "con.close()\n";
    }

}
