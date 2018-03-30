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
 * Reports which files violate the applied coding style conventions
 */
@Mojo(name = "check", defaultPhase = LifecyclePhase.PROCESS_CLASSES,
        requiresDependencyResolution = ResolutionScope.TEST)
public class CheckMojo extends AbstractWalkmodMojo {
    
    @Override
    protected void run(WalkModFacade facade, String[] chainList) throws MojoExecutionException {
        try {
            facade.check(chainList);
        } catch (InvalidConfigurationException e) {
            throw new MojoExecutionException("Error executing walkmod check", e);
        }
        getLog().info("walkmod check finished");
    }
}
