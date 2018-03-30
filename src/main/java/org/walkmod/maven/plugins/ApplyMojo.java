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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.walkmod.WalkModFacade;
import org.walkmod.exceptions.InvalidConfigurationException;

/**
 * Modifies the source code according the applied coding style conventions
 */
@Mojo(name = "apply", defaultPhase = LifecyclePhase.PROCESS_CLASSES,
        requiresDependencyResolution = ResolutionScope.TEST)
public class ApplyMojo extends AbstractWalkmodMojo {

    @Override
    protected void run(WalkModFacade facade, String[] chainList) throws MojoExecutionException {
        try {
            facade.apply(chainList);
        } catch (InvalidConfigurationException e) {
            getLog().error("Error executing walkmod apply. " +
                    "Enable \"verbose\" and/or \"printError\" to get more information. " +
                    "More information at: http://walkmod.github.io/maven-walkmod-plugin/apply-mojo.html", e);
            throw new MojoExecutionException("Error executing walkmod apply", e);
        }
        getLog().info("walkmod apply finished");
    }
}
