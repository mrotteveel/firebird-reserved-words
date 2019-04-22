package nl.lawinegevaar.firebird.reservedwords.sql;

import lombok.extern.slf4j.Slf4j;
import nl.lawinegevaar.firebird.reservedwords.database.DatabaseInfo;
import nl.lawinegevaar.firebird.reservedwords.database.DatabaseInitializer;
import org.apache.commons.cli.*;

import javax.sql.DataSource;

@Slf4j
public class PopulateSqlKeywords {

    private static final Options OPTIONS = buildCommandLineOptions();

    public static void main(String[] args) {
        CommandLine commandLine = getCommandLine(args);
        DatabaseInfo databaseInfo = DatabaseInfo.createDatabaseInfo();
        initialize(databaseInfo);
        if (commandLine.hasOption("init-only")) {
            String message = "Initialization only requested, exiting...";
            System.out.println(message);
            log.info(message);
            return;
        }

        if (!commandLine.hasOption("v")) {
            System.err.println("Invalid command line: option -v or --version is required");
            printUsage();
            System.exit(-1);
        }
        final int sqlVersion = Integer.parseInt(commandLine.getOptionValue("v"));

        DataSource dataSource = databaseInfo.getDataSource();
        if (commandLine.hasOption("delete-all")) {
            new ClearSqlKeywords(sqlVersion)
                    .clearKeywords(dataSource);
        }
        if (commandLine.hasOption("non-reserved")) {
            new SqlKeywordsFromSource(sqlVersion, commandLine.getOptionValue("non-reserved"), false)
                    .loadKeywords(dataSource);
        }
        if (commandLine.hasOption("reserved")) {
            for (String path : commandLine.getOptionValues("reserved")) {
                new SqlKeywordsFromSource(sqlVersion, path, true)
                        .loadKeywords(dataSource);
            }
        }
        if (commandLine.hasOption("delete-keywords")) {
            for (String path : commandLine.getOptionValues("delete-keywords")) {
                new DeleteSqlKeywords(sqlVersion, path)
                        .deleteKeywords(dataSource);
            }
        }
    }

    private static void initialize(DatabaseInfo databaseInfo) {
        var initializer = new DatabaseInitializer(databaseInfo);
        initializer.initializeDatabase();
    }

    private static CommandLine getCommandLine(String[] args) {
        CommandLineParser clParser = new DefaultParser();
        try {
            CommandLine commandLine = clParser.parse(OPTIONS, args);

            if (commandLine.hasOption("h")) {
                printUsage();
                System.exit(0);
            }
            return commandLine;
        } catch (ParseException e) {
            System.err.println("Invalid command line: " + e.getMessage());
            printUsage();
            System.exit(-1);
        }
        throw new AssertionError("should not get here");
    }

    private static void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("PopulateSqlKeywords", OPTIONS);
    }

    private static Options buildCommandLineOptions() {
        return new Options()
                .addOption("h", "help", false, "Prints usage instructions and exits")
                .addOption(Option.builder()
                        .longOpt("init-only")
                        .desc("Initialize database only and exit")
                        .build())
                .addOption("v", "version", true, "SQL standard version (xxxx, eg 2003); required")
                .addOption(Option.builder()
                        .longOpt("delete-all")
                        .desc("Deletes all existing keywords for version")
                        .build())
                .addOption(Option.builder()
                        .longOpt("non-reserved")
                        .hasArg().argName("FILENAME")
                        .desc("File with keyword per line that needs to be marked as non-reserved for this version")
                        .build())
                .addOption(Option.builder()
                        .longOpt("reserved")
                        .hasArg().argName("FILENAME")
                        .desc("File with keyword per line that needs to be marked as reserved for this version")
                        .build())
                .addOption(Option.builder()
                        .longOpt("delete-keywords")
                        .hasArg().argName("FILENAME")
                        .desc("File with keyword per line that need to be removed for this version")
                        .build());
    }

}
