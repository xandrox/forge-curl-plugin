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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.jboss.forge.shell.PromptType;
import org.jboss.forge.shell.ShellColor;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.DefaultCommand;
import org.jboss.forge.shell.plugins.Help;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;

@Alias("curl")
@Help("Usage: curl [options...] <url>")
public class CurlPlugin implements Plugin {
	
	private SingleClientConnManager singleClientConnManager;
	private DefaultHttpClient defaultHttpClient;

	public CurlPlugin() {
	}
	
	@PostConstruct
	public void init(){
		singleClientConnManager = new SingleClientConnManager();
		defaultHttpClient = new DefaultHttpClient(singleClientConnManager);
	}
	
	@PreDestroy
	public void destroy() {
		singleClientConnManager.shutdown();
	}

	@DefaultCommand
	public void run( 
			@Option(shortName="i", name = "include", description="Include protocol headers in the output (H/F)", flagOnly=true) final boolean include,
			@Option(shortName="X", name = "request", description="<command> Specify request command to use", defaultValue="GET", type=PromptType.JAVA_VARIABLE_NAME) final RequestMethod command,
			@Option(shortName="d", name = "data", description="HTTP POST data (H)") final String data,
			@Option(shortName="H", name = "header", description="", completer=HeaderCommandCompleter.class) final String headers,
			@Option(help="<url>", completer=URLCommandCompleater.class)	final String url,
			PipeOut out
			) throws IOException {
		
		HttpUriRequest request = createRequest(url, command, headers);
		HttpResponse response = defaultHttpClient.execute(request);
		
		if (include) {
			printStatus(response, out);
			printResponseHeaders(response, out);
		}
		
		printEntity(out, response);
	}

	private void printEntity(PipeOut out, HttpResponse response)
			throws IOException {
		HttpEntity entity = response.getEntity();
		if (entity !=  null) {
			InputStream content = entity.getContent();
			String string = IOUtils.toString(content, "us-ascii");
			out.println(string);
		}
	}

	private HttpUriRequest createRequest(final String url, RequestMethod command, String headers) {
		HttpUriRequest request;
		
		switch (command) {

		case HEAD:
			request = new HttpHead(url);
			break;
		
		case OPTIONS:
			request = new HttpOptions(url);
			break;
			
		case POST:
			request = new HttpPost(url);
			break;

		case PUT:
			request = new HttpPut(url);
			break;

		default:
			request = new HttpGet(url);
			break;
		}
		
		setHeaders(request, headers);
		
		return request;
	}

	private void setHeaders(HttpUriRequest request, String headers) {
		Map<String, String> parseHeader = HeaderCommandCompleter.parseHeader(headers);
		Set<Entry<String, String>> entrySet = parseHeader.entrySet();
		for (Entry<String, String> header : entrySet) {
			request.addHeader(header.getKey(), header.getValue());
		}
	}

	private void printStatus(HttpResponse response, PipeOut out) {
		StatusLine sl = response.getStatusLine();
		out.print(ShellColor.BOLD, sl.getProtocolVersion() + " ");
		
		ShellColor status;
		if (sl.getStatusCode() < 300) {
			status = ShellColor.GREEN;
		} else if (sl.getStatusCode() < 500) {
			status = ShellColor.YELLOW;
		} else {
			status = ShellColor.RED;
		}
		
		out.print(status, sl.getStatusCode() + " ");
		out.println(ShellColor.BOLD, sl.getReasonPhrase());
		
	}

	private void printResponseHeaders(HttpResponse response, PipeOut out) {
		Header[] allHeaders = response.getAllHeaders();
		for (Header header : allHeaders) {
			out.print(ShellColor.CYAN, org.apache.commons.lang3.StringUtils.rightPad(header.getName(), 20, ".")  + ": ");
			out.print(ShellColor.WHITE, header.getValue());
			out.println();
		}
		
	}

}
