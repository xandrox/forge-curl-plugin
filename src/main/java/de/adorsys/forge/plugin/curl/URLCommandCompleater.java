/**
 * Copyright (C) 2012 Sandro Sonntag sso@adorsys.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.adorsys.forge.plugin.curl;

import java.util.Arrays;

import javax.inject.Inject;

import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.Project;
import org.jboss.forge.shell.completer.CommandCompleter;
import org.jboss.forge.shell.completer.CommandCompleterState;

public class URLCommandCompleater implements CommandCompleter {

	@Inject
	private Project project;

	private Iterable<?> getCompletionTokens() {
		MavenCoreFacet mcf = project.getFacet(MavenCoreFacet.class);

		return Arrays.asList("http://", "https://", "http://localhost:8080/",
				"http://localhost:8080/"
						+ mcf.getMavenProject().getArtifactId(),
				"http://localhost:8080/"
						+ mcf.getMavenProject().getArtifactId() + "/rest");
	}

	@Override
	public void complete(final CommandCompleterState state) {
		Iterable<?> values;
		try {
			values = getCompletionTokens();
			String peek = state.getTokens().peek();

			if ((state.getTokens().size() <= 1) && values != null) {
				for (Object val : values) {
					if (val != null) {
						String prop = val.toString();
						if (prop.startsWith(peek == null ? "" : peek)) {
							state.getCandidates().add(prop);
							state.setIndex(state.getOriginalIndex()
									- (peek == null ? 0 : peek.length()));
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO could not get options. this should eventually be logged
		}
	}

}
