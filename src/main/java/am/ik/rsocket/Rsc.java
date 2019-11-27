/*
 * Copyright (C) 2019 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.rsocket;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import io.rsocket.RSocketFactory;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.ClientTransport;
import joptsimple.OptionException;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

public class Rsc {

	public static void main(String[] a) throws Exception {
		final Args args = new Args(a);
		try {
			if (args.help()) {
				args.printHelp(System.out);
				return;
			}
			if (args.version()) {
				printVersion();
				return;
			}
			if (!args.hasUri()) {
				System.err.println("Uri is required.");
				System.err.println();
				args.printHelp(System.out);
				return;
			}
			run(args).blockLast();
		} catch (OptionException | IllegalArgumentException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	static Flux<?> run(Args args) {
		if (args.debug()) {
			LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
			Logger rootLogger = loggerContext.getLogger("io.rsocket.FrameLogger");
			rootLogger.setLevel(Level.DEBUG);
		}
		final ClientTransport clientTransport = args.clientTransport();
		return RSocketFactory.connect() //
				.frameDecoder(PayloadDecoder.ZERO_COPY) //
				.metadataMimeType(args.metadataMimeType()) //
				.dataMimeType(args.dataMimeType()) //
				.transport(clientTransport) //
				.start() //
				.flatMapMany(rsocket -> args.interactionModel().request(rsocket, args));
	}

	static void printVersion() {
		// Version class will be generated during Maven's generated-sources phase
		System.out.println(Version.getVersion());
	}
}
