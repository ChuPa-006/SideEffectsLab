package org.gradle.wrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Minimal project bootstrap launcher. It downloads the Gradle distribution
 * named in gradle-wrapper.properties and starts its Gradle executable.
 * This is intentionally small; use the official wrapper in production repos.
 */
public final class GradleWrapperMain {
    private GradleWrapperMain() {}

    public static void main(String[] args) throws Exception {
        File projectDir = findProjectDirectory(new File(System.getProperty("user.dir")));
        File propertiesFile = new File(projectDir, "gradle/wrapper/gradle-wrapper.properties");
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(propertiesFile)) {
            properties.load(input);
        }

        String distributionUrl = properties.getProperty("distributionUrl");
        if (distributionUrl == null || distributionUrl.trim().isEmpty()) {
            throw new IllegalStateException("distributionUrl is missing in " + propertiesFile);
        }
        distributionUrl = distributionUrl.replace("\\:", ":");
        URI uri = URI.create(distributionUrl);
        String zipName = new File(uri.getPath()).getName();
        String installName = zipName.replaceFirst("-bin\\.zip$", "").replaceFirst("-all\\.zip$", "");

        String gradleUserHome = System.getenv("GRADLE_USER_HOME");
        File gradleHome = gradleUserHome == null || gradleUserHome.isEmpty()
                ? new File(System.getProperty("user.home"), ".gradle")
                : new File(gradleUserHome);
        File cacheDir = new File(gradleHome, "wrapper/dists/" + zipName + "/sideeffects-bootstrap");
        File distributionDir = new File(cacheDir, installName);
        File executable = new File(distributionDir,
                isWindows() ? "bin/gradle.bat" : "bin/gradle");

        if (!executable.isFile()) {
            cacheDir.mkdirs();
            File zip = new File(cacheDir, zipName);
            if (!zip.isFile()) {
                System.out.println("Downloading Gradle from " + distributionUrl);
                download(uri.toURL(), zip);
            }
            extract(zip.toPath(), cacheDir.toPath());
        }
        if (!executable.isFile()) {
            throw new IllegalStateException("Gradle executable not found after extraction: " + executable);
        }

        java.util.List<String> command = new java.util.ArrayList<>();
        if (isWindows()) {
            command.add("cmd.exe");
            command.add("/c");
        }
        command.add(executable.getAbsolutePath());
        for (String arg : args) command.add(arg);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(projectDir);
        processBuilder.inheritIO();
        if (System.getenv("GRADLE_USER_HOME") == null) {
            processBuilder.environment().put("GRADLE_USER_HOME", gradleHome.getAbsolutePath());
        }
        int exitCode = processBuilder.start().waitFor();
        System.exit(exitCode);
    }

    private static File findProjectDirectory(File start) {
        File current = start;
        while (current != null) {
            if (new File(current, "gradle/wrapper/gradle-wrapper.properties").isFile()) return current;
            current = current.getParentFile();
        }
        throw new IllegalStateException("Could not find gradle/wrapper/gradle-wrapper.properties from " + start);
    }

    private static void download(URL url, File destination) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(60000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", "SideEffectsLab-Gradle-Bootstrap/1.0");
        int code = connection.getResponseCode();
        if (code < 200 || code >= 300) throw new IllegalStateException("Gradle download failed with HTTP " + code);
        File temporary = new File(destination.getAbsolutePath() + ".part");
        try (InputStream input = connection.getInputStream()) {
            Files.copy(input, temporary.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } finally {
            connection.disconnect();
        }
        Files.move(temporary.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private static void extract(Path zipFile, Path destination) throws Exception {
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                Path output = destination.resolve(entry.getName()).normalize();
                if (!output.startsWith(destination.normalize())) {
                    throw new IllegalStateException("Unsafe ZIP entry: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(output);
                } else {
                    Files.createDirectories(output.getParent());
                    Files.copy(zip, output, StandardCopyOption.REPLACE_EXISTING);
                }
                zip.closeEntry();
            }
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
