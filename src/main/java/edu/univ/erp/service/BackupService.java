package edu.univ.erp.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

  
    public Result backup(File outFile) {
        try {
            String dumpExe = mysqlBinFolder + "\\mysqldump.exe";

            List<String> cmd = new ArrayList<>();
            cmd.add(dumpExe);
            cmd.add("-u");
            cmd.add(dbUser);
            cmd.add("--password=" + dbPassword);
            cmd.add("--databases");
            cmd.add(dbName);

            ProcessBuilder pb = new ProcessBuilder(cmd);

            
            pb.redirectErrorStream(false);

            Process p = pb.start();

            try (InputStream stdout = p.getInputStream();
                 FileOutputStream fos = new FileOutputStream(outFile)) {

                stdout.transferTo(fos);
            }

            String stderrLog = read(p.getErrorStream());

            int exit = p.waitFor();

            if (exit == 0) {
                if (!stderrLog.isBlank()) {
                    return new Result(true,
                            "Backup successful (with warnings):\n" + stderrLog);
                }
                return new Result(true,
                        "Backup successful!\nSaved to: " + outFile.getAbsolutePath());
            }

            return new Result(false,
                    "Backup failed (exit " + exit + "):\n" + stderrLog);

        } catch (Exception ex) {
            return new Result(false, "Exception: " + ex.getMessage());
        }
    }

  
    public Result restore(File sqlFile) {
        try {
            if (!sqlFile.exists()) {
                return new Result(false, "Restore file does not exist:\n" + sqlFile.getAbsolutePath());
            }

            String mysqlExe = mysqlBinFolder + "\\mysql.exe";

            List<String> cmd = new ArrayList<>();
            cmd.add(mysqlExe);
            cmd.add("-u");
            cmd.add(dbUser);
            cmd.add("--password=" + dbPassword);
            cmd.add(dbName);

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);

            Process p = pb.start();

           
            try (OutputStream os = p.getOutputStream();
                 FileInputStream fis = new FileInputStream(sqlFile)) {

                fis.transferTo(os);
            }

            String log = read(p.getInputStream());
            int exit = p.waitFor();

            if (exit == 0) {
                return new Result(true,
                        "Restore successful from:\n" + sqlFile.getAbsolutePath());
            }

            return new Result(false,
                    "Restore failed (exit " + exit + "):\n" + log);

        } catch (Exception ex) {
            return new Result(false, "Exception: " + ex.getMessage());
        }
    }

    private static String read(InputStream is) {
        try (Scanner sc = new Scanner(is, StandardCharsets.UTF_8)) {
            sc.useDelimiter("\\A");
            return sc.hasNext() ? sc.next() : "";
        }
    }

    public static class Result {
        public final boolean success;
        public final String message;

        public Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}
