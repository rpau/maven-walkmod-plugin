/* 
  Copyright (C) 2013 Raquel Pau and Albert Coroleu.
 
 Walkmod is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 Walkmod is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with Walkmod.  If not, see <http://www.gnu.org/licenses/>.*/
package org.walkmod.maven.plugins;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DependencyResolutionException;
import org.apache.maven.project.MavenProject;
import org.walkmod.OptionsBuilder;
import org.walkmod.WalkModFacade;

public abstract class AbstractWalkmodMojo extends AbstractMojo {

    /**
     * Chains to execute
     */
    @Parameter(property = "chains", defaultValue = "${walkmod.chains}")
    protected String chains = null;

    /**
     * If dependencies are resolved off-line
     */
    @Parameter(property = "offline", defaultValue = "false")
    protected boolean offline = false;

    /**
     * If it is executed in verbose mode.
     */
    @Parameter(property = "verbose", defaultValue = "false")
    protected boolean verbose = false;

    /**
     * If it prints errors
     */
    @Parameter(property = "printError", defaultValue = "false")
    protected boolean printError = false;

    /**
     * If it skips walkmod
     */
    @Parameter(property = "skipWalkmod")
    protected boolean skipWalkmod = false;

    /**
     * Source file directory
     */
    @Parameter(property = "path")
    protected String path = null;

    /**
     * List of included files
     */
    @Parameter
    protected String[] includes = null;

    /**
     * List of excluded files
     */
    @Parameter
    protected String[] excludes = null;

    /**
     * Dynamic parameters to set
     */
    @Parameter(property = "properties", defaultValue = "${walkmod.properties}")
    protected String properties = null;

    protected Map<String, Object> dynamicParams= new HashMap<String, Object>();

    private ExecutionStatus status = ExecutionStatus.INCOMPLETE;

    /**
     * Current project
     */
    @Parameter(property = "project", defaultValue="${project}", required = true)
    protected MavenProject project = null;

    public void execute() throws MojoExecutionException {

        if (isParentPom() && isWalkModDeclaredAsPlugin()) {
            status = ExecutionStatus.IGNORED;
            return;
        }

        if (skipWalkmod) {
            status = ExecutionStatus.SKIPPED;
            return;
        }

        try {
            prepare();
        } catch (Exception e) {
            throw new MojoExecutionException("Error preparing WalkMod", e);
        }

        OptionsBuilder options = OptionsBuilder.options()
                .printErrors(printError)
                .offline(offline)
                .verbose(verbose)
                .path(path)
                .includes(includes)
                .excludes(excludes)
                .dynamicArgs(dynamicParams)
                .executionDirectory(project.getBasedir());

        WalkModFacade walkmod = new WalkModFacade(new File(project.getBasedir(), "walkmod.xml"),
                options, null);

        String[] selectedChains = null;

        if (chains != null) {
            selectedChains = chains.split(",");
        }

        run(walkmod, selectedChains);

        status = ExecutionStatus.FINISHED;
    }

    private URL[] getURLClasspathElements() throws Exception {
        List<String> classpathElements = project.getTestClasspathElements();
        URL[] entries = new URL[classpathElements.size()];
        int i = 0;
        for(String classPathEntry: classpathElements) {
            entries[i] = new File(classPathEntry).toURI().toURL();
            i++;
        }
        return entries;
    }

    private URLClassLoader resolveClasspath() throws Exception {
        return new URLClassLoader(getURLClasspathElements()) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                Class<?> result = null;
                try {
                    result = findClass(name);
                } catch (Throwable e) {
                    //ignore, we should look into the parents classpath
                }
                if (result != null) {
                    return result;
                }
                return super.loadClass(name, resolve);
            }

            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                return loadClass(name, false);
            }
        };

    }

    protected void prepare() throws Exception {
        dynamicParams.put("classLoader", resolveClasspath());
        if (properties != null) {
            String[] parts = properties.split("\\=| ");
            if (parts.length % 2 == 0) {
                for (int i = 0; i < parts.length - 1; i += 2) {
                    dynamicParams.put(parts[i].trim(), parts[i + 1].trim());
                }
            }
        }
    }

    private boolean isParentPom() {
        return project != null && "pom".equals(project.getPackaging());
    }

    private boolean isWalkModDeclaredAsPlugin() {
        List plugins = project.getBuildPlugins();
        boolean found = false;

        if (plugins != null) {
            Iterator it = plugins.iterator();
            while (it.hasNext() && !found) {
                Plugin plugin = (Plugin) it.next();
                found = "walkmod-maven-plugin".equals(plugin.getArtifactId());
            }
        }
        return found;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    protected abstract void run(WalkModFacade facade, String[] chainList) throws MojoExecutionException;

}
