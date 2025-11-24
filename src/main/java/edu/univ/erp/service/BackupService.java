package edu.univ.erp.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Windows-safe BackupService with quote-safe paths and password handling.
 * Uses mysqldump.exe and mysql.exe directly through ProcessBuilder.
 */
public class BackupService {

    private final String mysqlBinFolder;
    private final String dbUser;
    private final String dbPassword;
    private final String dbName;

    public BackupService(String mysqlBinFolder, String dbUser, String dbPassword, String dbName) {
        this.mysqlBinFolder = mysqlBinFolder;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.dbName = dbName;
    }

    // -------------------------------
    // BACKUP
    // -------------------------------
    public Result backup(File outFile) {

        try {
            String dumpExe = mysqlBinFolder + "\\mysqldump.exe";

            List<String> cmd = new ArrayList<>();
            cmd.add(dumpExe);                     // path to mysqldump
            cmd.add("-u");
            cmd.add(dbUser);
            cmd.add("--password=" + dbPassword);  // supports @ and special chars
            cmd.add("--databases");
            cmd.add(dbName);

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectOutput(outFile);           // write SQL to file
            pb.redirectErrorStream(true);         // merge stderr

            Process process = pb.start();
            String log = read(process.getInputStream());
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return new Result(true,
                        "Backup completed successfully.\nFile: " + outFile.getAbsolutePath());
            } else {
                return new Result(false,
                        "Backup failed with exit code " + exitCode + ".\nLog:\n" + log);
            }

        } catch (Exception e) {
            return new Result(false,
                    "Exception while backing up: " + e.getMessage());
        }
    }

    // -------------------------------
    // RESTORE
    // -------------------------------
    public Result restore(File sqlFile) {

        try {
            if (!sqlFile.exists()) {
                return new Result(false, "Restore file not found:\n" + sqlFile.getAbsolutePath());
            }

            String mysqlExe = mysqlBinFolder + "\\mysql.exe";

            List<String> cmd = new ArrayList<>();
            cmd.add(mysqlExe);
            cmd.add("-u");
            cmd.add(dbUser);
            cmd.add("--password=" + dbPassword);
            cmd.add(dbName);

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectInput(sqlFile);            // read SQL from file
            pb.redirectErrorStream(true);

            Process process = pb.start();
            String log = read(process.getInputStream());
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return new Result(true,
                        "Restore completed successfully.\nRestored from: " + sqlFile.getAbsolutePath());
            } else {
                return new Result(false,
                        "Restore failed with exit code " + exitCode + ".\nLog:\n" + log);
            }

        } catch (Exception e) {
            return new Result(false,
                    "Exception while restoring: " + e.getMessage());
        }
    }

    // Read text from process stream
    private static String read(InputStream in) {
        try (Scanner sc = new Scanner(in, StandardCharsets.UTF_8)) {
            sc.useDelimiter("\\A");
            return sc.hasNext() ? sc.next() : "";
        }
    }

    // Result container
    public static class Result {
        public final boolean success;
        public final String message;

        public Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}
