package nl.nn.adapterframework.http;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import nl.nn.adapterframework.core.IPipeLineSession;
import nl.nn.adapterframework.core.PipeLineSessionBase;
import nl.nn.adapterframework.parameters.ParameterResolutionContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class HttpSenderTest extends Mockito {

	HttpSender sender = null;

	@Before
	public void initHttpClient() throws IOException {
		CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
		CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
		StatusLine statusLine = mock(StatusLine.class);
		HttpEntity httpEntity = mock(HttpEntity.class);

		when(statusLine.getStatusCode()).thenReturn(200);
		when(httpResponse.getStatusLine()).thenReturn(statusLine);

		//Mock response
//		URL url = ClassUtils.getResourceURL(this, "file.ext");
//		InputStream content = url.openStream();
		InputStream content = new ByteArrayInputStream("<dummy result/>".getBytes());
		when(httpEntity.getContent()).thenReturn(content);
		when(httpResponse.getEntity()).thenReturn(httpEntity);

		//Mock all requests
		when(httpClient.execute(any(HttpHost.class), any(HttpRequestBase.class), any(HttpContext.class))).thenReturn(httpResponse);

		sender = spy(new HttpSender());
		when(sender.getHttpClient()).thenReturn(httpClient);

		//Some default settings, url will be mocked.
		sender.setUrl("http://127.0.0.1/");
		sender.setIgnoreRedirects(true);
		sender.setVerifyHostname(false);
		sender.setAllowSelfSignedCertificates(true);
	}

	@Test
	public void simpleMockedHttpGet() throws ClientProtocolException, IOException {

		try {
			IPipeLineSession pls = new PipeLineSessionBase();
			ParameterResolutionContext prc = new ParameterResolutionContext("dummy prc", pls);

			sender.setMethodType("GET");

			sender.configure();
			sender.open();

			//Use InputStream 'content' as result.
			String result = sender.sendMessage(null, "", prc);
			assertEquals("<dummy result/>", result);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (sender != null) {
				sender.close();
			}
		}
	}

	@Test
	public void simpleBase64MockedHttpGet() throws ClientProtocolException, IOException {

		try {
			IPipeLineSession pls = new PipeLineSessionBase();
			ParameterResolutionContext prc = new ParameterResolutionContext("dummy prc", pls);

			sender.setMethodType("GET");
			sender.setBase64(true);

			sender.configure();
			sender.open();

			//Use InputStream 'content' as result.
			String result = sender.sendMessage(null, "", prc);
			assertEquals("PGR1bW15IHJlc3VsdC8+", result);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (sender != null) {
				sender.close();
			}
		}
	}

	@Test
	public void simpleBase64MockedHttpPost() throws ClientProtocolException, IOException {

		try {
			IPipeLineSession pls = new PipeLineSessionBase();
			ParameterResolutionContext prc = new ParameterResolutionContext("dummy prc", pls);

			sender.setParamsInUrl(false);
			sender.setInputMessageParam("inputMessageParam");
			sender.setMethodType("POST");

			sender.configure();
			sender.open();

			//Use InputStream 'content' as result.
			String result = sender.sendMessage(null, "tralala", prc);
			assertEquals("PGR1bW15IHJlc3VsdC8+", result);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (sender != null) {
				sender.close();
			}
		}
	}
}
