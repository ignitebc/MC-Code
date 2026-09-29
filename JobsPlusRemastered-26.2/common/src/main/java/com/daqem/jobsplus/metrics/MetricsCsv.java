package com.daqem.jobsplus.metrics;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 메트릭 CSV 파일 쓰기 도구.
 * 기존 파일의 헤더가 현재 형식과 다르면 그 파일을 이름 뒤에 시각을 붙여 보관하고 새 파일을 시작한다.
 * 열 순서가 다른 행이 한 파일에 섞이지 않게 하기 위함이다.
 */
final class MetricsCsv
{
    private static final Set<Path> VERIFIED_FILES = new HashSet<>();

    private MetricsCsv()
    {
    }

    /** 서버가 새로 시작되면 헤더 검사를 다시 한다. */
    static void resetVerifiedFiles()
    {
        VERIFIED_FILES.clear();
    }

    static void append(Path file, String header, List<String> lines) throws IOException
    {
        if (lines.isEmpty())
        {
            return;
        }

        Files.createDirectories(file.getParent());
        if (!VERIFIED_FILES.contains(file))
        {
            archiveIfHeaderChanged(file, header);
            VERIFIED_FILES.add(file);
        }

        StringBuilder output = new StringBuilder(lines.size() * 128 + header.length() + 1);
        if (!Files.exists(file) || Files.size(file) == 0L)
        {
            output.append(header).append('\n');
        }
        for (String line : lines)
        {
            output.append(line).append('\n');
        }

        Files.writeString(
                file,
                output,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.APPEND
        );
    }

    private static void archiveIfHeaderChanged(Path file, String header) throws IOException
    {
        if (!Files.exists(file) || Files.size(file) == 0L)
        {
            return;
        }

        String firstLine;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8))
        {
            firstLine = reader.readLine();
        }
        if (header.equals(firstLine))
        {
            return;
        }

        String fileName = file.getFileName().toString();
        int extensionIndex = fileName.lastIndexOf('.');
        String stem = extensionIndex < 0 ? fileName : fileName.substring(0, extensionIndex);
        String extension = extensionIndex < 0 ? "" : fileName.substring(extensionIndex);
        Path archived = file.resolveSibling(stem + "-old-" + System.currentTimeMillis() + extension);
        Files.move(file, archived);
    }

    static String text(String value)
    {
        if (value == null)
        {
            return "";
        }
        if (value.indexOf(',') < 0 && value.indexOf('"') < 0 && value.indexOf('\n') < 0 && value.indexOf('\r') < 0)
        {
            return value;
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }

    static String number(double value)
    {
        return String.format(Locale.ROOT, "%.6f", value);
    }
}
