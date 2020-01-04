package io.nbrs.dynamicql.controller;

import io.nbrs.dynamicql.model.QueryParameter;
import io.nbrs.ui.UIListViewCell;
import io.nbrs.ui.UIView;
import io.nbrs.ui.UIViewController;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Antonio Zaitoun on 2020-01-04.
 */
public class MainViewController extends UIViewController {

    @FXML
    protected AnchorPane rightMenu;

    @FXML
    protected AnchorPane leftMenu;

    @FXML
    private Button export;

    private CodeArea codeArea;

    private ListView<MenuItem> listView;

    private List<QueryParameter> parameters = new ArrayList<>();

    public MainViewController() {
        super("/view/xml/controller_master.fxml");
    }

    @Override
    public void viewWillLoad(ResourceBundle bundle) {
        super.viewWillLoad(bundle);

        this.codeArea = new CodeArea();
        setUpCodeArea(codeArea, leftMenu);

        setUpListView();

        export.setOnAction(event -> {
            ExportViewController controller = new ExportViewController();
            Stage stage = new Stage();
            stage.setTitle("Query Export");
            stage.setScene(new Scene(controller.view));
            controller.setParameterList(this.parameters);
            controller.setQuery(this.codeArea.getText().replaceAll(PARAM_PATTERN, "?"));
            stage.show();
        });

    }

    private void setUpListView() {
        listView = new ListView<>();
        listView.setMouseTransparent(true);
        listView.setFocusTraversable(false);
        listView.setCellFactory(param -> new Cell());
        setViewOn(rightMenu, listView);
    }

    private static final String[] KEYWORDS = new String[]{
            "ACCESSIBLE", "ACCOUNT", "ACTION", "ADD", "ADMIN", "AFTER", "AGAINST", "AGGREGATE", "ALGORITHM", "ALL",
            "ALTER", "ALWAYS", "ANALYSE", "ANALYZE", "AND", "ANY", "AS", "ASC", "ASCII", "ASENSITIVE", "AT", "AUTHORS",
            "AUTOEXTEND_SIZE", "AUTO_INCREMENT", "AVG", "AVG_ROW_LENGTH", "BACKUP", "BEFORE", "BEGIN", "BETWEEN",
            "BIGINT", "BINARY", "BINLOG", "BIT", "BLOB", "BLOCK", "BOOL", "BOOLEAN", "BOTH", "BTREE", "BUCKETS", "BY",
            "BYTE", "CACHE", "CALL", "CASCADE", "CASCADED", "CASE", "CATALOG_NAME", "CHAIN", "CHANGE", "CHANGED",
            "CHANNEL", "CHAR", "CHARACTER", "CHARSET", "CHECK", "CHECKSUM", "CIPHER", "CLASS_ORIGIN", "CLIENT",
            "CLONE", "CLOSE", "COALESCE", "CODE", "COLLATE", "COLLATION", "COLUMN", "COLUMNS", "COLUMN_FORMAT",
            "COLUMN_NAME", "COMMENT", "COMMIT", "COMMITTED", "COMPACT", "COMPLETION", "COMPONENT", "COMPRESSED",
            "COMPRESSION", "CONCURRENT", "CONDITION", "CONNECTION", "CONSISTENT", "CONSTRAINT", "CONSTRAINT_CATALOG",
            "CONSTRAINT_NAME", "CONSTRAINT_SCHEMA", "CONTAINS", "CONTEXT", "CONTINUE", "CONTRIBUTORS", "CONVERT", "CPU",
            "CREATE", "CROSS", "CUBE", "CUME_DIST", "CURRENT", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP",
            "CURRENT_USER", "CURSOR", "CURSOR_NAME", "DATA", "DATABASE", "DATABASES", "DATAFILE", "DATE", "DATETIME",
            "DAY", "DAY_HOUR", "DAY_MICROSECOND", "DAY_MINUTE", "DAY_SECOND", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE",
            "DEFAULT", "DEFAULT_AUTH", "DEFINER", "DEFINITION", "DELAYED", "DELAY_KEY_WRITE", "DELETE", "DENSE_RANK",
            "DESC", "DESCRIBE", "DESCRIPTION", "DES_KEY_FILE", "DETERMINISTIC", "DIAGNOSTICS", "DIRECTORY", "DISABLE",
            "DISCARD", "DISK", "DISTINCT", "DISTINCTROW", "DIV", "DO", "DOUBLE", "DROP", "DUAL", "DUMPFILE", "DUPLICATE",
            "DYNAMIC", "EACH", "ELSE", "ELSEIF", "EMPTY", "ENABLE", "ENCLOSED", "ENCRYPTION", "END", "ENDS", "ENGINE",
            "ENGINES", "ENUM", "ERROR", "ERRORS", "ESCAPE", "ESCAPED", "EVENT", "EVENTS", "EVERY", "EXCEPT", "EXCHANGE",
            "EXCLUDE", "EXECUTE", "EXISTS", "EXIT", "EXPANSION", "EXPIRE", "EXPLAIN", "EXPORT", "EXTENDED", "EXTENT_SIZE",
            "FALSE", "FAST", "FAULTS", "FETCH", "FIELDS", "FILE", "FILE_BLOCK_SIZE", "FILTER", "FIRST", "FIRST_VALUE",
            "FIXED", "FLOAT", "FLOAT4", "FLOAT8", "FLUSH", "FOLLOWING", "FOLLOWS", "FOR", "FORCE", "FOREIGN", "FORMAT",
            "FOUND", "FRAC_SECOND", "FROM", "FULL", "FULLTEXT", "FUNCTION", "GENERAL", "GENERATED", "GEOMCOLLECTION",
            "GEOMETRY", "GEOMETRYCOLLECTION", "GET", "GET_FORMAT", "GET_MASTER_PUBLIC_KEY", "GLOBAL", "GRANT", "GRANTS",
            "GROUP", "GROUPING", "GROUPS", "GROUP_REPLICATION", "HANDLER", "HASH", "HAVING", "HELP", "HIGH_PRIORITY",
            "HISTOGRAM", "HISTORY", "HOST", "HOSTS", "HOUR", "HOUR_MICROSECOND", "HOUR_MINUTE", "HOUR_SECOND",
            "IDENTIFIED", "IF", "IGNORE", "IGNORE_SERVER_IDS", "IMPORT", "IN", "INDEX", "INDEXES", "INFILE",
            "INITIAL_SIZE", "INNER", "INNOBASE", "INNODB", "INOUT", "INSENSITIVE", "INSERT", "INSERT_METHOD",
            "INSTALL", "INSTANCE", "INT", "INT1", "INT2", "INT3", "INT4", "INT8", "INTEGER", "INTERVAL",
            "INTO", "INVISIBLE", "INVOKER", "IO", "IO_AFTER_GTIDS", "IO_BEFORE_GTIDS", "IO_THREAD",
            "IPC", "IS", "ISOLATION", "ISSUER", "ITERATE", "JOIN", "JSON", "JSON_TABLE", "KEY", "KEYS",
            "KEY_BLOCK_SIZE", "KILL", "LAG", "LANGUAGE", "LAST", "LAST_VALUE", "LEAD", "LEADING", "LEAVE", "LEAVES",
            "LEFT", "LESS", "LEVEL", "LIKE", "LIMIT", "LINEAR", "LINES", "LINESTRING", "LIST", "LOAD", "LOCAL",
            "LOCALTIME", "LOCALTIMESTAMP", "LOCK", "LOCKED", "LOCKS", "LOGFILE", "LOGS", "LONG", "LONGBLOB", "LONGTEXT",
            "LOOP", "LOW_PRIORITY", "MASTER", "MASTER_AUTO_POSITION", "MASTER_BIND", "MASTER_CONNECT_RETRY",
            "MASTER_DELAY", "MASTER_HEARTBEAT_PERIOD", "MASTER_HOST", "MASTER_LOG_FILE", "MASTER_LOG_POS",
            "MASTER_PASSWORD", "MASTER_PORT", "MASTER_PUBLIC_KEY_PATH", "MASTER_RETRY_COUNT", "MASTER_SERVER_ID",
            "MASTER_SSL", "MASTER_SSL_CA", "MASTER_SSL_CAPATH", "MASTER_SSL_CERT", "MASTER_SSL_CIPHER",
            "MASTER_SSL_CRL", "MASTER_SSL_CRLPATH", "MASTER_SSL_KEY", "MASTER_SSL_VERIFY_SERVER_CERT",
            "MASTER_TLS_VERSION", "MASTER_USER", "MATCH", "MAXVALUE", "MAX_CONNECTIONS_PER_HOUR",
            "MAX_QUERIES_PER_HOUR", "MAX_ROWS", "MAX_SIZE", "MAX_UPDATES_PER_HOUR", "MAX_USER_CONNECTIONS", "MEDIUM",
            "MEDIUMBLOB", "MEDIUMINT", "MEDIUMTEXT", "MEMORY", "MERGE", "MESSAGE_TEXT", "MICROSECOND", "MIDDLEINT",
            "MIGRATE", "MINUTE", "MINUTE_MICROSECOND", "MINUTE_SECOND", "MIN_ROWS", "MOD", "MODE", "MODIFIES", "MODIFY",
            "MONTH", "MULTILINESTRING", "MULTIPOINT", "MULTIPOLYGON", "MUTEX", "MYSQL_ERRNO", "NAME", "NAMES",
            "NATIONAL", "NATURAL", "NCHAR", "NDB", "NDBCLUSTER", "NESTED", "NEVER", "NEW", "NEXT", "NO", "NODEGROUP",
            "NONE", "NOT", "NOWAIT", "NO_WAIT", "NO_WRITE_TO_BINLOG", "NTH_VALUE", "NTILE", "NULL", "NULLS", "NUMBER",
            "NUMERIC", "NVARCHAR", "OF", "OFFSET", "OLD_PASSWORD", "ON", "ONE", "ONE_SHOT", "ONLY", "OPEN", "OPTIMIZE",
            "OPTIMIZER_COSTS", "OPTION", "OPTIONALLY", "OPTIONS", "OR", "ORDER", "ORDINALITY", "OTHERS", "OUT", "OUTER",
            "OUTFILE", "OVER", "OWNER", "PACK_KEYS", "PAGE", "PARSER", "PARSE_GCOL_EXPR", "PARTIAL", "PARTITION",
            "PARTITIONING", "PARTITIONS", "PASSWORD", "PATH", "PERCENT_RANK", "PERSIST", "PERSIST_ONLY", "PHASE",
            "PLUGIN", "PLUGINS", "PLUGIN_DIR", "POINT", "POLYGON", "PORT", "PRECEDES", "PRECEDING", "PRECISION",
            "PREPARE", "PRESERVE", "PREV", "PRIMARY", "PRIVILEGES", "PROCEDURE", "PROCESS", "PROCESSLIST", "PROFILE",
            "PROFILES", "PROXY", "PURGE", "QUARTER", "QUERY", "QUICK", "RANGE", "RANK", "READ", "READS", "READ_ONLY",
            "READ_WRITE", "REAL", "REBUILD", "RECOVER", "RECURSIVE", "REDOFILE", "REDO_BUFFER_SIZE", "REDUNDANT",
            "REFERENCE", "REFERENCES", "REGEXP", "RELAY", "RELAYLOG", "RELAY_LOG_FILE", "RELAY_LOG_POS", "RELAY_THREAD",
            "RELEASE", "RELOAD", "REMOTE", "REMOVE", "RENAME", "REORGANIZE", "REPAIR", "REPEAT", "REPEATABLE", "REPLACE",
            "REPLICATE_DO_DB", "REPLICATE_DO_TABLE", "REPLICATE_IGNORE_DB", "REPLICATE_IGNORE_TABLE", "REPLICATE_REWRITE_DB",
            "REPLICATE_WILD_DO_TABLE", "REPLICATE_WILD_IGNORE_TABLE", "REPLICATION", "REQUIRE", "RESET", "RESIGNAL", "RESOURCE",
            "RESPECT", "RESTART", "RESTORE", "RESTRICT", "RESUME", "RETURN", "RETURNED_SQLSTATE", "RETURNS", "REUSE", "REVERSE",
            "REVOKE", "RIGHT", "RLIKE", "ROLE", "ROLLBACK", "ROLLUP", "ROTATE", "ROUTINE", "ROW", "ROWS", "ROW_COUNT", "ROW_FORMAT",
            "ROW_NUMBER", "RTREE", "SAVEPOINT", "SCHEDULE", "SCHEMA", "SCHEMAS", "SCHEMA_NAME", "SECOND", "SECOND_MICROSECOND",
            "SECURITY", "SELECT", "SENSITIVE", "SEPARATOR", "SERIAL", "SERIALIZABLE", "SERVER", "SESSION", "SET", "SHARE", "SHOW",
            "SHUTDOWN", "SIGNAL", "SIGNED", "SIMPLE", "SKIP", "SLAVE", "SLOW", "SMALLINT", "SNAPSHOT", "SOCKET", "SOME", "SONAME",
            "SOUNDS", "SOURCE", "SPATIAL", "SPECIFIC", "SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING", "SQL_AFTER_GTIDS",
            "SQL_AFTER_MTS_GAPS", "SQL_BEFORE_GTIDS", "SQL_BIG_RESULT", "SQL_BUFFER_RESULT", "SQL_CACHE", "SQL_CALC_FOUND_ROWS",
            "SQL_NO_CACHE", "SQL_SMALL_RESULT", "SQL_THREAD", "SQL_TSI_DAY", "SQL_TSI_FRAC_SECOND", "SQL_TSI_HOUR", "SQL_TSI_MINUTE",
            "SQL_TSI_MONTH", "SQL_TSI_QUARTER", "SQL_TSI_SECOND", "SQL_TSI_WEEK", "SQL_TSI_YEAR", "SRID", "SSL", "STACKED",
            "START", "STARTING", "STARTS", "STATS_AUTO_RECALC", "STATS_PERSISTENT", "STATS_SAMPLE_PAGES", "STATUS", "STOP",
            "STORAGE", "STORED", "STRAIGHT_JOIN", "STRING", "SUBCLASS_ORIGIN", "SUBJECT", "SUBPARTITION", "SUBPARTITIONS",
            "SUPER", "SUSPEND", "SWAPS", "SWITCHES", "SYSTEM", "TABLE", "TABLES", "TABLESPACE", "TABLE_CHECKSUM", "TABLE_NAME",
            "TEMPORARY", "TEMPTABLE", "TERMINATED", "TEXT", "THAN", "THEN", "THREAD_PRIORITY", "TIES", "TIME", "TIMESTAMP",
            "TIMESTAMPADD", "TIMESTAMPDIFF", "TINYBLOB", "TINYINT", "TINYTEXT", "TO", "TRAILING", "TRANSACTION", "TRIGGER",
            "TRIGGERS", "TRUE", "TRUNCATE", "TYPE", "TYPES", "UNBOUNDED", "UNCOMMITTED", "UNDEFINED", "UNDO", "UNDOFILE",
            "UNDO_BUFFER_SIZE", "UNICODE", "UNINSTALL", "UNION", "UNIQUE", "UNKNOWN", "UNLOCK", "UNSIGNED", "UNTIL", "UPDATE",
            "UPGRADE", "USAGE", "USE", "USER", "USER_RESOURCES", "USE_FRM", "USING", "UTC_DATE", "UTC_TIME", "VALIDATION",
            "VALUES", "VARBINARY", "VARCHAR", "VARCHARACTER", "VARIABLES", "VARYING", "VCPU", "VIEW", "VIRTUAL", "VISIBLE",
            "WAIT", "WARNINGS", "WEEK", "WEIGHT_STRING", "WHEN", "WHERE", "WHILE", "WINDOW", "WITH", "WITHOUT", "WORK",
            "WRAPPER", "WRITE", "X509", "XA", "XID", "XML", "XOR", "YEAR", "YEAR_MONTH", "ZEROFILL"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String SINGLE_QUOTE_STRING = "'([^'\\\\]|\\\\.)*'";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";
    private static final String PARAM_PATTERN = "\\$\\{(.*?)}";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<STRINGG>" + SINGLE_QUOTE_STRING + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
                    + "|(?<PARAMETER>" + PARAM_PATTERN + ")"
    );

    protected void setUpCodeArea(CodeArea codeArea, AnchorPane view) {
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        Subscription cleanupWhenNoLongerNeedIt = codeArea

                // plain changes = ignore style changes that are emitted when syntax highlighting is reapplied
                // multi plain changes = save computation by not rerunning the code multiple times
                //   when making multiple changes (e.g. renaming a method at multiple parts in file)
                .multiPlainChanges()

                // do not emit an event until 500 ms have passed since the last emission of previous stream
                .successionEnds(Duration.ofMillis(500))

                // run the following code block when previous stream emits an event
                .subscribe(ignore -> {
                    String text = codeArea.getText();
                    codeArea.setStyleSpans(0, computeHighlighting(text));
                    computeParameters(text);
                });

        // when no longer need syntax highlighting and wish to clean up memory leaks
        // run: `cleanupWhenNoLongerNeedIt.unsubscribe();`


        // auto-indent: insert previous line's indents on enter
        final Pattern whiteSpace = Pattern.compile("^\\s+");
        codeArea.addEventHandler(KeyEvent.KEY_PRESSED, KE ->
        {
            if (KE.getCode() == KeyCode.ENTER) {
                int caretPosition = codeArea.getCaretPosition();
                int currentParagraph = codeArea.getCurrentParagraph();
                Matcher m0 = whiteSpace.matcher(codeArea.getParagraph(currentParagraph - 1).getSegments().get(0));
                if (m0.find()) Platform.runLater(() -> codeArea.insertText(caretPosition, m0.group()));
            }
        });


        codeArea.replaceText(0, 0, "");

        this.setViewOn(view, new StackPane(new VirtualizedScrollPane<>(codeArea)));
    }

    protected static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                            matcher.group("PAREN") != null ? "paren" :
                                    matcher.group("BRACE") != null ? "brace" :
                                            matcher.group("BRACKET") != null ? "bracket" :
                                                    matcher.group("SEMICOLON") != null ? "semicolon" :
                                                            matcher.group("STRING") != null ? "string" :
                                                                    matcher.group("STRINGG") != null ? "stringg" :
                                                                            matcher.group("COMMENT") != null ? "comment" :
                                                                                    matcher.group("PARAMETER") != null ? "parameter" :
                                                                                            null; /* never happens */
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    private void computeParameters(String text) {
        Matcher m = Pattern.compile("\\$\\{(.*?)}").matcher(text);
        Map<String, List<Integer>> parameters = new HashMap<>();

        int position_counter = 0;
        while (m.find()) {
            String param = m.group(1);

            List<Integer> s = parameters.getOrDefault(param, new ArrayList<>());
            s.add(position_counter);
            parameters.put(param, s);
            ++position_counter;
        }
        System.out.println(parameters);


        // convert parameters to query parameters
        this.parameters.clear();
        parameters.forEach((s, integers) -> {
            QueryParameter param = new QueryParameter(s, integers.stream().mapToInt(Integer::intValue).toArray());
            this.parameters.add(param);
        });

        if (listView != null) {
            List<MenuItem> menuItems = this.parameters.stream()
                    .map(q ->
                            new MenuItem(String.format("%s %s", q.getName(),
                                    Arrays.toString(q.getPositions())), ""))
                    .collect(Collectors.toList());
            listView.setItems(FXCollections.observableArrayList(menuItems));
        }
    }

    protected void setViewOn(AnchorPane pane, Node view) {
        pane.getChildren().clear();
        if (view != null) {
            AnchorPane.setTopAnchor(view, 8.0);
            AnchorPane.setBottomAnchor(view, 8.0);
            AnchorPane.setRightAnchor(view, 8.0);
            AnchorPane.setLeftAnchor(view, 8.0);
            pane.getChildren().add(view);
        }
    }


    public static class MenuItem {
        private String title;
        private String image;

        public MenuItem(String title, String image) {
            this.title = title;
            this.image = image;
        }
    }

    static class Cell extends UIListViewCell<MenuItem, UIView> {


        private String defStyle;
        private String highlighted;

        @Override
        public UIView load(MenuItem item) {

            defStyle = getStyle();
            highlighted = defStyle;
            //setStyle(":selected{-fx-background-color:  #97ff8e !important;}");
            changeBackgroundOnHoverUsingBinding(this);
            return new CellView().setData(item.title, item.image);
        }

        void changeBackgroundOnHoverUsingBinding(Node node) {
            node.styleProperty().bind(
                    Bindings
                            .when(Bindings.or(node.hoverProperty(), node.focusedProperty()))
                            .then(
                                    new SimpleStringProperty(highlighted)
                            )
                            .otherwise(
                                    new SimpleStringProperty(defStyle)
                            )
            );

        }
    }

    static class CellView extends UIView {

        @FXML
        private Label menu;

        @FXML
        private ImageView imageView;

        @FXML
        private HBox vbox;

        public CellView setData(String str, String image) {
            this.menu.setText(str);
            if (image != null && !image.isEmpty()) {
                this.imageView.setImage(new Image(image));
            } else {
                vbox.getChildren().remove(imageView);
            }
            return this;
        }


        @Override
        public void layoutSubviews(ResourceBundle bundle) {
            menu.setTextFill(Color.BLACK);
        }

        @Override
        public void layoutBundle(ResourceBundle bundle) {

        }

        @Override
        public String resource() {
            return "/io/nbrs/dynamicql/view/xml/list_item.fxml";
        }
    }
}
