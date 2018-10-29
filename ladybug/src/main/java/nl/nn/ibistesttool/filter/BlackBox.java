/*
   Copyright 2018 Nationale-Nederlanden

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package nl.nn.ibistesttool.filter;

import java.util.List;

import nl.nn.testtool.Checkpoint;
import nl.nn.testtool.Report;
import nl.nn.testtool.filter.CheckpointMatcher;

/**
 * @author Jaco de Groot
 */
public class BlackBox implements CheckpointMatcher {
	
	public boolean match(Report report, Checkpoint checkpoint) {
		if (
				checkpoint.getName() != null
				&&
				(
					checkpoint.getName().startsWith("Sender ")
					// Also in stub4testtool.xsl
					&& !"nl.nn.adapterframework.jdbc.DirectQuerySender".equals(checkpoint.getSourceClassName())
					&& !"nl.nn.adapterframework.jdbc.FixedQuerySender".equals(checkpoint.getSourceClassName())
					&& !"nl.nn.adapterframework.senders.DelaySender".equals(checkpoint.getSourceClassName())
					&& !"nl.nn.adapterframework.senders.EchoSender".equals(checkpoint.getSourceClassName())
					&& !"nl.nn.adapterframework.senders.IbisLocalSender".equals(checkpoint.getSourceClassName())
					&& !"nl.nn.adapterframework.senders.LogSender".equals(checkpoint.getSourceClassName())
					&& !"nl.nn.adapterframework.senders.ParallelSenders".equals(checkpoint.getSourceClassName())
					&& !"nl.nn.adapterframework.senders.SenderSeries".equals(checkpoint.getSourceClassName())
					&& !"nl.nn.adapterframework.senders.SenderWrapper".equals(checkpoint.getSourceClassName())
					&& !"nl.nn.adapterframework.senders.XsltSender".equals(checkpoint.getSourceClassName())
					// Not in stub4testtool.xsl
					&& !"nl.nn.adapterframework.senders.FixedResultSender".equals(checkpoint.getSourceClassName())
					&& !"nl.nn.adapterframework.senders.XmlValidatorSender".equals(checkpoint.getSourceClassName())
				)
			) {
			return true;
		} else {
			List checkpoints = report.getCheckpoints();
			if (checkpoints.size() > 0) {
				Checkpoint firstCheckpoint = (Checkpoint)checkpoints.get(0);
				if (checkpoint.equals(firstCheckpoint)) {
					return true;
				} else {
					Checkpoint lastCheckpoint = (Checkpoint)checkpoints.get(checkpoints.size() - 1);
					if (checkpoint.equals(lastCheckpoint)) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
