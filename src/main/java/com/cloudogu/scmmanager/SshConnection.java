package com.cloudogu.scmmanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

public class SshConnection implements AutoCloseable {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final Connection connection;
    private final NamespaceAndName repository;

    SshConnection(Connection connection, NamespaceAndName repository) {
        this.connection = connection;
        this.repository = repository;
    }

    public Connection getConnection() {
        return connection;
    }

    public NamespaceAndName mustGetRepository() {
        if (repository == null) {
            throw new IllegalStateException("ssh connection is not bound to a repository");
        }
        return repository;
    }

    public Optional<NamespaceAndName> getRepository() {
        return Optional.ofNullable(repository);
    }

    public void connect(SSHAuthentication authentication) {
        try {
            // accept any host
            connection.connect((s, i, s1, bytes) -> true);
            authentication.authenticate(connection);
        } catch (IOException ex) {
            throw new SshConnectionFailedException("ssh connection failed", ex);
        }
    }

    public Command command(String command) {
        return new Command(connection, command);
    }

    @Override
    public void close() {
        connection.close();
    }

    public static class Command {

        private final Connection connection;
        private final String command;
        private Input input;

        private Command(Connection connection, String command) {
            this.connection = connection;
            this.command = command;
        }

        public Input withInput(Object inputObject) {
            input = new Input(this, inputObject);
            return input;
        }

        public <T> Output<T> withOutput(Class<T> type) {
            return new Output<>(this, type);
        }

        public void exec() throws IOException {
            exec(null);
        }

        private <T> T exec(Unmarshaller<T> unmarshaller) throws IOException {
            Session session = null;
            try {
                session = connection.openSession();
                session.execCommand(command);

                if (input != null) {
                    input.handle(session);
                }

                T output = null;
                if (unmarshaller != null) {
                    try (InputStream stdout = session.getStdout()) {
                        output = unmarshaller.unmarshal(stdout);
                    }
                }

                return output;
            } finally {
                if (session != null) {
                    session.close();
                }

            }
        }
    }

    public static class Input {

        private final Command command;
        private final Object object;
        private Marshaller marshaller;


        private Input(Command command, Object object) {
            this.command = command;
            this.object = object;
        }

        public Command json() {
            marshaller = (out -> mapper.writeValue(out, object));
            return command;
        }

        public Command xml() {
            marshaller = (out -> JAXB.marshal(object, out));
            return command;
        }

        public void handle(Session session) throws IOException {
            try (OutputStream stdin = session.getStdin()) {
                marshaller.marshal(stdin);
            }
        }
    }

    public static class Output<T> {

        private final Command command;
        private final Class<T> type;

        private Output(Command command, Class<T> type) {
            this.command = command;
            this.type = type;
        }

        public T json() throws IOException {
            return command.exec(stdout -> mapper.readValue(stdout, type));
        }

        public T xml() throws IOException {
            return command.exec(stdout -> JAXB.unmarshal(stdout, type));
        }
    }

    @FunctionalInterface
    private interface Marshaller {
        void marshal(OutputStream out) throws IOException;
    }

    @FunctionalInterface
    private interface Unmarshaller<T> {
        T unmarshal(InputStream in) throws IOException;
    }
}
