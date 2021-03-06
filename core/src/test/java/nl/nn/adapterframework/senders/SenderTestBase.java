package nl.nn.adapterframework.senders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;

import nl.nn.adapterframework.core.IPipeLineSession;
import nl.nn.adapterframework.core.ISender;
import nl.nn.adapterframework.core.SenderException;
import nl.nn.adapterframework.pipes.XmlValidator;

public abstract class SenderTestBase<S extends ISender> {

	protected Log log = LogFactory.getLog(this.getClass());
	protected S sender;

	@Mock
	protected IPipeLineSession session;

	public abstract S createSender() throws Exception;

	@Before
	public void setup() throws Exception {
		sender = createSender();
		//		sender.open();
	}

	@After
	public void setdown() throws SenderException {
		if (sender != null) {
			sender.close();
		}
	}

	protected String readLines(Reader reader) throws IOException {
		BufferedReader buf = new BufferedReader(reader);
		StringBuilder string = new StringBuilder();
		String line = buf.readLine();
		while (line != null) {
			string.append(line);
			line = buf.readLine();
			if (line != null) {
				string.append("\n");
			}
		}
		return string.toString();
	}

	protected String getFile(String file) throws IOException {
		return readLines(new InputStreamReader(XmlValidator.class.getResourceAsStream(file)));
	}
}
