package ml.papers.services;

import io.micronaut.context.annotation.Property;
import jakarta.inject.Singleton;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Singleton
public class LoggerService {
    private static final Integer CAPACITY = 1000;
    private final BlockingQueue<String> queue;

    public LoggerService(@Property(name = "logger.path") String path) throws FileNotFoundException {
        this.queue = new LinkedBlockingQueue<>(CAPACITY);
        new LoggerThread(new PrintStream(new FileOutputStream(path, true))).start();
    }

    public void log(String text) {
        try {
            queue.put(String.format("logWriter %s:    %s", LocalTime.from(Instant.now().atZone(ZoneId.of("GMT+3"))), text));
        } catch (InterruptedException ignored) {
        }
    }

    private class LoggerThread extends Thread {
        private final PrintStream printWriter;

        private LoggerThread(PrintStream printWriter) {
            this.printWriter = printWriter;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    printWriter.println(queue.take());
                }
            } catch (InterruptedException ignored) {
            } finally {
                printWriter.close();
            }
        }
    }
}
