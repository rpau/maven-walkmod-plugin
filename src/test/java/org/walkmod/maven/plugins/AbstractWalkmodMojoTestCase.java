package org.walkmod.maven.plugins;


import org.apache.maven.DefaultMaven;
import org.apache.maven.Maven;
import org.apache.maven.execution.*;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.internal.impl.SimpleLocalRepositoryManagerFactory;
import org.eclipse.aether.repository.LocalRepository;

import java.io.File;
import java.util.Arrays;

public abstract class AbstractWalkmodMojoTestCase extends AbstractMojoTestCase {

    private static MavenExecutionRequestPopulator POPULATOR;

    protected MavenSession newMavenSession() {
        try {
            MavenExecutionRequest request = new DefaultMavenExecutionRequest();
            MavenExecutionResult result = new DefaultMavenExecutionResult();

            // populate sensible defaults, including repository basedir and remote repos
            if (POPULATOR == null) {
                POPULATOR = getContainer().lookup(MavenExecutionRequestPopulator.class);
            }

            POPULATOR.populateDefaults(request);

            // this is needed to allow java profiles to get resolved; i.e. avoid during project builds:
            // [ERROR] Failed to determine Java version for profile java-1.5-detected @ org.apache.commons:commons-parent:22, /Users/alex/.m2/repository/org/apache/commons/commons-parent/22/commons-parent-22.pom, line 909, column 14
            request.setSystemProperties(System.getProperties());

            // and this is needed so that the repo session in the maven session
            // has a repo manager, and it points at the local repo
            // (cf MavenRepositorySystemUtils.newSession() which is what is otherwise done)
            DefaultMaven maven = (DefaultMaven) getContainer().lookup(Maven.class);
            DefaultRepositorySystemSession repoSession =
                    (DefaultRepositorySystemSession) maven.newRepositorySession(request);
            repoSession.setLocalRepositoryManager(
                    new SimpleLocalRepositoryManagerFactory().newInstance(repoSession,
                            new LocalRepository(request.getLocalRepository().getBasedir())));

            @SuppressWarnings("deprecation")
            MavenSession session = new MavenSession(getContainer(),
                    repoSession,
                    request, result);
            return session;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Extends the super to use the new {@link #newMavenSession()} introduced here
     * which sets the defaults one expects from maven; the standard test case leaves a lot of things blank
     */
    @Override
    protected MavenSession newMavenSession(MavenProject project) {
        MavenSession session = newMavenSession();
        session.setCurrentProject(project);
        session.setProjects(Arrays.asList(project));

        return session;
    }

    /**
     * As {@link #lookupConfiguredMojo(MavenProject, String)} but taking the pom file
     * and creating the {@link MavenProject}.
     */
    protected Mojo lookupConfiguredMojo(File pom, String goal) throws Exception {
        assertNotNull(pom);
        assertTrue(pom.exists());

        ProjectBuildingRequest buildingRequest = newMavenSession().getProjectBuildingRequest();
        buildingRequest.setResolveDependencies(true);
        ProjectBuilder projectBuilder = lookup(ProjectBuilder.class);
        MavenProject project = projectBuilder.build(pom, buildingRequest).getProject();

        return lookupConfiguredMojo(project, goal);
    }
}
