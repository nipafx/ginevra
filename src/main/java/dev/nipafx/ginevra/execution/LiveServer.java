package dev.nipafx.ginevra.execution;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Function;

class LiveServer {

	private static final String REFRESH_SLUG = "/_ginevra/refresh";
	static final String REFRESH_JS_CODE = """
			const eventSource = new EventSource("%s");
			eventSource.addEventListener('refresh', () => {
				eventSource.close();
				window.location.reload();
			});
			""".formatted(REFRESH_SLUG);

	// only access within `synchronized (sseConnections)` block to prevent race conditions
	// when reading from or writing to the list or when writing to the exchanges it contains
	private final List<HttpExchange> sseConnections;

	LiveServer() {
		this.sseConnections = new ArrayList<>();
	}

	void launch(int port, Function<Path, byte[]> fetchResponse) {
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
			server.createContext(REFRESH_SLUG, this::handleSseRefreshRequest);
			server.createContext("/", exchange -> handlePageRequest(exchange, fetchResponse));
			server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
			server.start();

			System.out.printf("Visit http://127.0.0.1:%s to see your site%n", port);
		} catch (IOException ex) {
			// TODO: handle error
			throw new UncheckedIOException(ex);
		}
	}

	private void handlePageRequest(HttpExchange exchange, Function<Path, byte[]> fetchResponse) throws IOException {
		var path = Path.of(exchange.getRequestURI().toString());
		var response = fetchResponse.apply(path);

		exchange.sendResponseHeaders(200, response.length);
		try (var out = exchange.getResponseBody()) {
			out.write(response);
		}
	}

	private void handleSseRefreshRequest(HttpExchange connection) throws IOException {
		connection.getResponseHeaders().add("Connection", "keep-alive");
		connection.getResponseHeaders().add("Content-Type", "text/event-stream");
		connection.getResponseHeaders().add("Cache-Control", "no-cache");
		connection.sendResponseHeaders(200, 0);
		connection.getResponseBody().flush();

		synchronized (sseConnections) {
			sseConnections.add(connection);
		}
	}

	void refresh() {
		synchronized (sseConnections) {
			var connections = sseConnections.listIterator();
			while (connections.hasNext()) {
				var connection = connections.next();
				try {
					// server-sent events apparently need a data field or browsers
					// (at least Firefox and Chrome) don't process the event
					connection.getResponseBody().write("""
							event: refresh
							data:

							""".getBytes());
					connection.getResponseBody().flush();
				} catch (IOException ex) {
					// TODO: handle error
					if (!ex.getMessage().startsWith("Broken pipe"))
						ex.printStackTrace();
					connection.close();
					connections.remove();
				}
			}
		}
	}

}
