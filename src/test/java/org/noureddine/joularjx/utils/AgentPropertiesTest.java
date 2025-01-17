package org.noureddine.joularjx.utils;

import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentPropertiesTest {

    @Test
    @ExpectSystemExitWithStatus(1)
    void loadNonExistentFile() throws IOException {
        try (final FileSystem fs = MemoryFileSystemBuilder.newEmpty().build()) {
            new AgentProperties(fs);
        }
    }

    @Test
    void loadEmptyFile() throws IOException {
        try (final FileSystem fs = MemoryFileSystemBuilder.newEmpty().build()) {
            Files.createFile(fs.getPath("config.properties"));

            AgentProperties properties = new AgentProperties(fs);

            assertAll(
                    () -> assertTrue(properties.getFilterMethodNames().isEmpty()),
                    () -> assertNull(properties.getPowerMonitorPath()),
                    () -> assertFalse(properties.getOverwriteRuntimeData()),
                    () -> assertFalse(properties.getSaveRuntimeData()),
                    () -> assertEquals(Level.INFO, properties.getLoggerLevel())
            );
        }
    }

    @Test
    void fullConfiguration() throws IOException {
        try (final FileSystem fs = MemoryFileSystemBuilder.newEmpty().build()) {
            Files.write(fs.getPath("config.properties"), ("filter-method-names=org.noureddine.joularjx\n" +
                    "powermonitor-path=C:\\\\joularjx\\\\PowerMonitor.exe\n" +
                    "save-runtime-data=true\noverwrite-runtime-data=true").getBytes(StandardCharsets.UTF_8));

            AgentProperties properties = new AgentProperties(fs);

            assertAll(
                    () -> assertEquals(List.of("org.noureddine.joularjx"), properties.getFilterMethodNames()),
                    () -> assertEquals("C:\\joularjx\\PowerMonitor.exe", properties.getPowerMonitorPath()),
                    () -> assertTrue(properties.getSaveRuntimeData()),
                    () -> assertTrue(properties.getOverwriteRuntimeData())
            );
        }
    }

    @Test
    void multipleFilterMethods() throws IOException {
        try (final FileSystem fs = MemoryFileSystemBuilder.newEmpty().build()) {
            Files.write(fs.getPath("config.properties"),
                    "filter-method-names=org.noureddine.joularjx,org.noureddine.joularjx2".getBytes(StandardCharsets.UTF_8));

            AgentProperties properties = new AgentProperties(fs);

            assertAll(
                    () -> assertEquals(List.of("org.noureddine.joularjx", "org.noureddine.joularjx2"), properties.getFilterMethodNames()),
                    () -> assertNull(properties.getPowerMonitorPath())
            );
        }
    }

    @Test
    void capsBoolean() throws IOException {
        try (final FileSystem fs = MemoryFileSystemBuilder.newEmpty().build()) {
            Files.write(fs.getPath("config.properties"),
                    "save-runtime-data=TrUe\noverwrite-runtime-data=FaLse".getBytes(StandardCharsets.UTF_8));

            AgentProperties properties = new AgentProperties(fs);

            assertAll(
                    () -> assertTrue(properties.getSaveRuntimeData()),
                    () -> assertFalse(properties.getOverwriteRuntimeData())
            );
        }
    }

    static Stream<Arguments> getLogLevels() {
        return Stream.of(
                Arguments.of("INFO", Level.INFO),
                Arguments.of("OFF", Level.OFF),
                Arguments.of("SEVERE", Level.SEVERE),
                Arguments.of("WARNING", Level.WARNING),
                Arguments.of("FINE", Level.INFO),
                Arguments.of("CONFIG", Level.INFO),
                Arguments.of("ALL", Level.INFO),
                Arguments.of("FINER", Level.INFO),
                Arguments.of("FINEST", Level.INFO)
        );
    }

    @ParameterizedTest
    @MethodSource("getLogLevels")
    void logLevel(final String level, final Level expected) throws IOException {
        try (final FileSystem fs = MemoryFileSystemBuilder.newEmpty().build()) {
            Files.write(fs.getPath("config.properties"),
                    ("logger-level=" + level).getBytes(StandardCharsets.UTF_8));

            AgentProperties properties = new AgentProperties(fs);

            assertEquals(expected, properties.getLoggerLevel());
        }
    }
}