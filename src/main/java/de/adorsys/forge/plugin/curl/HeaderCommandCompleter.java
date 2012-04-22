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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.jboss.forge.shell.completer.CommandCompleter;
import org.jboss.forge.shell.completer.CommandCompleterState;

public class HeaderCommandCompleter implements CommandCompleter {
	
	private class MultivalueCommandCompleter implements CommandCompleter {
		private final String lastToken;
		private final Collection<String> suggestions;
		
		public MultivalueCommandCompleter(String lastToken,
				Collection<String> suggestions) {
			super();
			this.lastToken = lastToken;
			this.suggestions = suggestions;
		}


		@Override
		public void complete(CommandCompleterState state) {
			for (String header : suggestions) {
				if (header.startsWith(lastToken)) {
					state.getCandidates().add(header + ":");
					state.setIndex(state.getOriginalIndex() - (lastToken.length()));
				}
			}
			
		}
		
	}
	private static final List<String> KNOWN_HEADERS = Arrays.asList("Accept",
			"Accept-Language", "Authorization", "Cache-Control",
			"Content-Type", "Date", "ETag", "Expires", "Host", "Last-Modified");

	@Override
	public void complete(CommandCompleterState state) {
		Queue<String> tokens = state.getTokens();
		String peek = tokens.peek();
		if (peek == null) {
			return;
		}

		Map<String, String> headers = parseHeader(peek);
		HashSet<String> avalibleHeaders = new HashSet<String>(KNOWN_HEADERS);
		avalibleHeaders.removeAll(headers.keySet());

		int lastIndexOf = peek.lastIndexOf(',');
		String lastHeader = peek.substring(lastIndexOf + 1);
		
		MultivalueCommandCompleter completer = new MultivalueCommandCompleter(lastHeader, avalibleHeaders);

		completer.complete(state);
	}

	public static Map<String, String> parseHeader(String headerString) {
		HashMap<String, String> parsed = new HashMap<String, String>();
		String[] headers = headerString.split(",");
		for (String header : headers) {
			String[] kv = header.split(":");
			if (kv.length == 2) {
				parsed.put(kv[0], kv[1]);
			}
		}
		return parsed;
	}

}
