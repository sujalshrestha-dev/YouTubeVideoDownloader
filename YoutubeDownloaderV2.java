import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeDownloaderV2 extends JFrame {

    private JTextArea urlArea;
    private JComboBox<String> qualityBox, namingBox, fpsBox, formatBox;
    private JCheckBox openFolder;
    private JLabel folderLabel;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JButton downloadBtn, stopBtn;
    private DefaultTableModel tableModel;
    private String saveFolder = System.getProperty("user.home") + "\\Downloads";

    private ExecutorService threadPool;
    private volatile boolean isRunning = false;
    private final List<Process> runningProcesses = Collections.synchronizedList(new ArrayList<>());
    private boolean environmentWarningShown = false;

    // --- COLORS (Hexadecimal Palette) ---
    Color bgBackground = new Color(0x1E1E2F);
    Color bgPanel = new Color(0x2A2A3D);
    Color bgAltRow = new Color(0x262637);
    Color bgBorder = new Color(0x3A3A4D);
    Color fgHeader = new Color(0x61DAFB);
    Color accentBlue = new Color(0x4FC3F7);
    Color fgTextMain = Color.WHITE;
    Color fgTextSmall = new Color(0xCCCCCC);

    // Button Colors
    Color btnGreen = new Color(0x00C853);
    Color btnGreenHover = new Color(0x00E676);
    Color btnRed = new Color(0xD50000);
    Color btnRedHover = new Color(0xFF1744);

    public YoutubeDownloaderV2() {
        setTitle("Trigger's Professional Kit");
        setSize(1150, 850);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(bgBackground);
        setLayout(new BorderLayout(15, 15));

        try {
            setIconImage(new ImageIcon("C:\\yt-dle\\image.jpg").getImage());
        } catch (Exception e) {}

        // --- TOP: HEADER & INPUT ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(bgBackground);
        topPanel.setBorder(new EmptyBorder(20, 20, 10, 20));

        JLabel title = new JLabel("TRIGGER'S PROFESSIONAL DOWNLOADING KIT");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(fgHeader);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        urlArea = new JTextArea(7, 40);
        urlArea.setBackground(bgPanel);
        urlArea.setForeground(fgTextMain);
        urlArea.setCaretColor(accentBlue);
        urlArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        urlArea.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(bgBorder), "URLs (ONE PER LINE)",
                0, 0, new Font("Segoe UI", Font.BOLD, 12), fgTextSmall));

        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(new JScrollPane(urlArea), BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // --- CENTER: SETTINGS & TABLE ---
        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setBackground(bgBackground);
        centerPanel.setBorder(new EmptyBorder(0, 20, 0, 20));

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setBackground(bgPanel);
        settingsPanel.setPreferredSize(new Dimension(300, 0));
        settingsPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(bgBorder, 1), new EmptyBorder(15, 15, 15, 15)));

        formatBox = createStyledCombo(new String[]{"Video (MP4)", "Video (WebM)", "Audio (MP3)", "Audio (WAV)"});
        qualityBox = createStyledCombo(new String[]{"360p", "480p", "720p", "1080p", "Best"});
        qualityBox.setSelectedIndex(4);
        fpsBox = createStyledCombo(new String[]{"30 FPS", "60 FPS", "Best FPS"});
        fpsBox.setSelectedIndex(2);
        namingBox = createStyledCombo(new String[]{"Default", "1. Title", "01. Title"});

        openFolder = new JCheckBox("Open Folder on Finish", true);
        openFolder.setBackground(bgPanel);
        openFolder.setForeground(fgTextMain);
        openFolder.setFocusPainted(false);

        JButton folderBtn = new JButton("SET SAVE PATH");
        styleButton(folderBtn, bgBorder, bgBorder.brighter(), Color.WHITE);

        folderLabel = new JLabel("<html><body style='width: 220px'><b>PATH:</b><br>" + saveFolder + "</body></html>");
        folderLabel.setForeground(fgTextSmall);
        folderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        addSettingLabel(settingsPanel, "FILE FORMAT:");
        settingsPanel.add(formatBox);
        addSettingLabel(settingsPanel, "MAX QUALITY:");
        settingsPanel.add(qualityBox);
        addSettingLabel(settingsPanel, "FRAME RATE:");
        settingsPanel.add(fpsBox);
        addSettingLabel(settingsPanel, "NAMING STYLE:");
        settingsPanel.add(namingBox);
        settingsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        settingsPanel.add(openFolder);
        settingsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        settingsPanel.add(folderBtn);
        settingsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        settingsPanel.add(folderLabel);

        // --- TABLE ---
        String[] cols = {"ID", "LINK", "STATUS", "SPEED", "PROGRESS"};
        tableModel = new DefaultTableModel(cols, 0);
        JTable historyTable = new JTable(tableModel);
        historyTable.setBackground(bgPanel);
        historyTable.setForeground(fgTextMain);
        historyTable.setGridColor(bgBorder);
        historyTable.setRowHeight(30);
        historyTable.getTableHeader().setBackground(bgBorder);
        historyTable.getTableHeader().setForeground(fgHeader);
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        historyTable.setSelectionBackground(accentBlue);
        historyTable.setSelectionForeground(Color.WHITE);

        historyTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? bgPanel : bgAltRow);
                }
                return c;
            }
        });

        JScrollPane tableScroll = new JScrollPane(historyTable);
        tableScroll.setBorder(new LineBorder(bgBorder));
        tableScroll.getViewport().setBackground(bgBackground);

        centerPanel.add(settingsPanel, BorderLayout.WEST);
        centerPanel.add(tableScroll, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // --- BOTTOM: CONTROLS ---
        JPanel bottomPanel = new JPanel(new BorderLayout(15, 15));
        bottomPanel.setBackground(bgBackground);
        bottomPanel.setBorder(new EmptyBorder(10, 20, 25, 20));

        statusLabel = new JLabel("SYSTEM READY");
        statusLabel.setForeground(fgTextSmall);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setForeground(accentBlue);
        progressBar.setBackground(bgBorder);
        progressBar.setPreferredSize(new Dimension(0, 25));

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        btnPanel.setOpaque(false);

        downloadBtn = new JButton("START ENGINE");
        styleButton(downloadBtn, btnGreen, btnGreenHover, Color.WHITE);

        stopBtn = new JButton("KILL ALL PROCESSES");
        styleButton(stopBtn, btnRed, btnRedHover, Color.WHITE);
        stopBtn.setEnabled(false);

        btnPanel.add(downloadBtn);
        btnPanel.add(stopBtn);

        bottomPanel.add(statusLabel, BorderLayout.NORTH);
        bottomPanel.add(progressBar, BorderLayout.CENTER);
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        folderBtn.addActionListener(e -> chooseFolder());
        downloadBtn.addActionListener(e -> startEngine());
        stopBtn.addActionListener(e -> stopEngine());
    }

    private void styleButton(JButton b, Color normal, Color hover, Color text) {
        b.setBackground(normal);
        b.setForeground(text);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));

        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if(b.isEnabled()) b.setBackground(hover);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if(b.isEnabled()) b.setBackground(normal);
            }
        });
    }

    private void addSettingLabel(JPanel p, String text) {
        JLabel l = new JLabel(text);
        l.setForeground(fgHeader);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        p.add(l);
    }

    private JComboBox<String> createStyledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setBackground(bgPanel);
        cb.setForeground(Color.WHITE);
        cb.setBorder(new LineBorder(bgBorder));
        return cb;
    }

    private void chooseFolder() {
        JFileChooser fc = new JFileChooser(saveFolder);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            saveFolder = fc.getSelectedFile().getAbsolutePath();
            folderLabel.setText("<html><body style='width: 220px'><b>PATH:</b><br>" + saveFolder + "</body></html>");
        }
    }

    private void stopEngine() {
        isRunning = false;
        if (threadPool != null) {
            threadPool.shutdownNow();
        }

        synchronized (runningProcesses) {
            for (Process process : runningProcesses) {
                destroyProcess(process);
            }
            runningProcesses.clear();
        }

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String status = tableModel.getValueAt(i, 2).toString();
            if (status.equals("Starting...") || status.equals("Downloading")) {
                tableModel.setValueAt("Stopped", i, 2);
                tableModel.setValueAt("---", i, 3);
            }
        }

        statusLabel.setText("ENGINE TERMINATED");
        downloadBtn.setEnabled(true);
        stopBtn.setEnabled(false);
    }

    private void startEngine() {
        String[] lines = urlArea.getText().split("\\R");
        List<String> urls = new ArrayList<>();
        for (String line : lines) {
            String url = line.trim();
            if (!url.isEmpty()) {
                urls.add(url);
            }
        }
        if (urls.isEmpty()) {
            statusLabel.setText("ENTER AT LEAST ONE URL");
            return;
        }

        EnvironmentStatus environmentStatus = inspectEnvironment();
        if (!environmentStatus.ytDlpAvailable) {
            statusLabel.setText("yt-dlp NOT FOUND");
            JOptionPane.showMessageDialog(this,
                    "yt-dlp is not installed or not on PATH.\nInstall yt-dlp first, then retry.",
                    "Missing yt-dlp",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean hasYouTubeUrls = urls.stream().anyMatch(this::isYouTubeUrl);
        if (hasYouTubeUrls) {
            showEnvironmentWarning(environmentStatus);
        }

        tableModel.setRowCount(0);
        progressBar.setValue(0);
        isRunning = true;
        statusLabel.setText("DOWNLOADING...");
        downloadBtn.setEnabled(false);
        stopBtn.setEnabled(true);
        DownloadSettings settings = captureSettings(environmentStatus);
        threadPool = Executors.newFixedThreadPool(Math.min(3, urls.size()));
        for (int i = 0; i < urls.size(); i++) {
            String url = urls.get(i);
            int rowIdx = i;
            tableModel.addRow(new Object[]{rowIdx + 1, url, "Starting...", "0 B/s", "0%"});
            threadPool.submit(() -> downloadTask(url, rowIdx, settings));
        }
        threadPool.shutdown();
    }

    private void downloadTask(String url, int rowIdx, DownloadSettings settings) {
        Process process = null;
        try {
            List<String> command = new ArrayList<>();
            command.add("yt-dlp");
            command.add("--newline");
            command.add("--progress");
            command.add("--no-update");
            if (settings.cookiesFile != null) {
                command.add("--cookies");
                command.add(settings.cookiesFile.getAbsolutePath());
            }
            if (isYouTubeUrl(url) && settings.jsRuntimeArg != null) {
                command.add("--js-runtimes");
                command.add(settings.jsRuntimeArg);
            }
            command.add("-o");
            command.add(new File(settings.saveFolder, settings.nameTemplate).getPath());
            if (settings.formatIndex >= 2) {
                command.add("-x");
                command.add("--audio-format");
                command.add(settings.formatIndex == 2 ? "mp3" : "wav");
            } else {
                String filter = settings.resolution.equals("best")
                        ? "bestvideo" + settings.fpsLimit + "+bestaudio/best"
                        : "bestvideo[height<=" + settings.resolution + "]" + settings.fpsLimit + "+bestaudio/best";
                command.add("-f");
                command.add(filter);
                command.add("--merge-output-format");
                command.add(settings.formatIndex == 0 ? "mp4" : "webm");
            }
            command.add(url);

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            process = pb.start();
            runningProcesses.add(process);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            Pattern pPercent = Pattern.compile("(\\d+(?:\\.\\d+)?)%");
            Pattern pSpeed = Pattern.compile("at\\s+([^\\s]+(?:b|B)/s)");
            final String[] lastIssue = {null};
            while ((line = reader.readLine()) != null && isRunning) {
                Matcher mPct = pPercent.matcher(line);
                Matcher mSpd = pSpeed.matcher(line);
                String pct = mPct.find() ? mPct.group(1) + "%" : null;
                String spd = mSpd.find() ? mSpd.group(1) : null;
                String issue = parseIssue(line);
                if (issue != null) {
                    lastIssue[0] = issue;
                }
                SwingUtilities.invokeLater(() -> {
                    if (pct != null) tableModel.setValueAt(pct, rowIdx, 4);
                    if (spd != null) tableModel.setValueAt(spd, rowIdx, 3);
                    if (pct != null || spd != null) {
                        tableModel.setValueAt("Downloading", rowIdx, 2);
                    } else if (lastIssue[0] != null) {
                        tableModel.setValueAt(classifyIssue(lastIssue[0]), rowIdx, 2);
                        statusLabel.setText(lastIssue[0]);
                    } else {
                        tableModel.setValueAt("Downloading", rowIdx, 2);
                    }
                    updateGlobalProgress();
                });
            }
            if (!isRunning || Thread.currentThread().isInterrupted()) {
                destroyProcess(process);
                return;
            }

            int exitCode = process.waitFor();
            String issue = lastIssue[0];
            SwingUtilities.invokeLater(() -> {
                tableModel.setValueAt(exitCode == 0 ? "Finished" : classifyIssue(issue), rowIdx, 2);
                tableModel.setValueAt("---", rowIdx, 3);
                if (exitCode == 0) {
                    tableModel.setValueAt("100%", rowIdx, 4);
                } else if (issue != null) {
                    statusLabel.setText(issue);
                }
                updateGlobalProgress();
                checkAllDone();
            });
        } catch (Exception ex) {
            String issue = ex.getMessage() != null ? ex.getMessage() : "Download failed";
            SwingUtilities.invokeLater(() -> {
                tableModel.setValueAt(isRunning ? classifyIssue(issue) : "Stopped", rowIdx, 2);
                tableModel.setValueAt("---", rowIdx, 3);
                if (isRunning) {
                    statusLabel.setText(issue);
                }
                checkAllDone();
            });
        } finally {
            if (process != null) {
                runningProcesses.remove(process);
            }
        }
    }

    private void updateGlobalProgress() {
        double total = 0;
        int rows = tableModel.getRowCount();
        if(rows == 0) return;
        for(int i=0; i<rows; i++) {
            try {
                String val = tableModel.getValueAt(i, 4).toString().replace("%", "");
                total += Double.parseDouble(val);
            } catch(Exception e) {}
        }
        progressBar.setValue((int)(total / rows));
    }

    private void checkAllDone() {
        boolean running = false;
        boolean hasErrors = false;
        for(int i=0; i<tableModel.getRowCount(); i++) {
            String status = tableModel.getValueAt(i, 2).toString();
            if(status.equals("Downloading") || status.equals("Starting...")) running = true;
            if(!status.equals("Finished") && !status.equals("Stopped")
                    && !status.equals("Downloading") && !status.equals("Starting...")) {
                hasErrors = true;
            }
        }
        if(!running && isRunning) {
            isRunning = false;
            statusLabel.setText(hasErrors ? "COMPLETED WITH ERRORS" : "ALL TASKS COMPLETED");
            downloadBtn.setEnabled(true);
            stopBtn.setEnabled(false);
            if(!hasErrors && openFolder.isSelected()) {
                try { Desktop.getDesktop().open(new File(saveFolder)); } catch (Exception ignore) {}
            }
        }
    }

    private DownloadSettings captureSettings(EnvironmentStatus environmentStatus) {
        String nameTemplate = switch (namingBox.getSelectedIndex()) {
            case 1 -> "%(playlist_index)s. %(title)s.%(ext)s";
            case 2 -> "%(playlist_index)02d. %(title)s.%(ext)s";
            default -> "%(title)s.%(ext)s";
        };

        String fpsLimit = switch (fpsBox.getSelectedIndex()) {
            case 0 -> "[fps<=30]";
            case 1 -> "[fps<=60]";
            default -> "";
        };

        String resolution = switch (qualityBox.getSelectedIndex()) {
            case 0 -> "360";
            case 1 -> "480";
            case 2 -> "720";
            case 3 -> "1080";
            default -> "best";
        };

        return new DownloadSettings(
                nameTemplate,
                fpsLimit,
                formatBox.getSelectedIndex(),
                resolution,
                saveFolder,
                environmentStatus.jsRuntimeArg,
                environmentStatus.cookiesFile
        );
    }

    private void destroyProcess(Process process) {
        if (process == null || !process.isAlive()) {
            return;
        }

        process.destroy();
        try {
            process.waitFor(2, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        if (process.isAlive()) {
            process.destroyForcibly();
        }
    }

    private EnvironmentStatus inspectEnvironment() {
        String version = readFirstLine("yt-dlp", "--version");
        if (version == null || version.isBlank()) {
            return new EnvironmentStatus(false, null, null, null, findCookiesFile());
        }

        String jsRuntimeArg = findJsRuntimeArg();
        LocalDate versionDate = parseYtDlpDate(version.trim());
        return new EnvironmentStatus(true, version.trim(), versionDate, jsRuntimeArg, findCookiesFile());
    }

    private void showEnvironmentWarning(EnvironmentStatus environmentStatus) {
        List<String> warnings = new ArrayList<>();
        if (environmentStatus.versionDate != null && environmentStatus.versionDate.isBefore(LocalDate.now().minusDays(60))) {
            warnings.add("yt-dlp " + environmentStatus.ytDlpVersion + " is old. Run `yt-dlp -U` if YouTube fails.");
        }
        if (environmentStatus.jsRuntimeArg == null) {
            warnings.add("No JS runtime found. Install Deno 2+ for full YouTube compatibility.");
        }
        if (environmentStatus.cookiesFile == null) {
            warnings.add("No cookies.txt found. Some YouTube videos may require cookies.");
        }

        if (warnings.isEmpty()) {
            return;
        }

        statusLabel.setText(warnings.get(0).toUpperCase());
        if (!environmentWarningShown) {
            JOptionPane.showMessageDialog(this,
                    String.join("\n", warnings),
                    "YouTube Compatibility",
                    JOptionPane.WARNING_MESSAGE);
            environmentWarningShown = true;
        }
    }

    private String findJsRuntimeArg() {
        if (isCommandAvailable("deno")) {
            return "deno";
        }
        if (isCommandAvailable("node")) {
            return "node";
        }
        if (isCommandAvailable("bun")) {
            return "bun";
        }
        if (isCommandAvailable("qjs")) {
            return "quickjs:qjs";
        }
        if (isCommandAvailable("quickjs")) {
            return "quickjs";
        }
        return null;
    }

    private File findCookiesFile() {
        List<File> candidates = new ArrayList<>();

        try {
            File codeSource = new File(YoutubeDownloaderV2.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());
            if (codeSource.isDirectory()) {
                candidates.add(new File(codeSource, "cookies.txt"));
            } else {
                File parent = codeSource.getParentFile();
                if (parent != null) {
                    candidates.add(new File(parent, "cookies.txt"));
                }
            }
        } catch (Exception ignore) {}

        candidates.add(new File("cookies.txt"));
        candidates.add(new File(System.getProperty("user.dir"), "cookies.txt"));

        for (File candidate : candidates) {
            if (candidate != null && candidate.isFile()) {
                return candidate.getAbsoluteFile();
            }
        }
        return null;
    }

    private boolean isCommandAvailable(String command) {
        try {
            Process process = new ProcessBuilder("where", command).redirectErrorStream(true).start();
            return process.waitFor() == 0;
        } catch (Exception ex) {
            return false;
        }
    }

    private String readFirstLine(String... command) {
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                process.waitFor();
                if (process.exitValue() == 0) {
                    return line;
                }
            }
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
        return null;
    }

    private LocalDate parseYtDlpDate(String version) {
        try {
            String[] parts = version.split("\\.");
            if (parts.length == 3) {
                return LocalDate.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            }
        } catch (Exception ignore) {}
        return null;
    }

    private boolean isYouTubeUrl(String url) {
        String normalized = url.toLowerCase();
        return normalized.contains("youtube.com") || normalized.contains("youtu.be");
    }

    private String parseIssue(String line) {
        if (line == null) {
            return null;
        }

        String normalized = line.toLowerCase();
        if (normalized.contains("no supported javascript runtime could be found")) {
            return "Install Deno 2+ or Node 20+ for full YouTube support";
        }
        if (normalized.contains("sign in to confirm you're not a bot") || normalized.contains("sign in to confirm you are not a bot")) {
            return "This video needs browser cookies to pass YouTube's bot check";
        }
        if (normalized.contains("po token")) {
            return "This video requires a YouTube PO token";
        }
        if (normalized.startsWith("error:")) {
            return line.substring("ERROR:".length()).trim();
        }
        return null;
    }

    private String classifyIssue(String issue) {
        if (issue == null || issue.isBlank()) {
            return "Error";
        }
        String normalized = issue.toLowerCase();
        if (normalized.contains("deno") || normalized.contains("node 20+") || normalized.contains("javascript runtime")) {
            return "Needs Deno";
        }
        if (normalized.contains("bot check") || normalized.contains("cookies")) {
            return "Needs Cookies";
        }
        if (normalized.contains("po token")) {
            return "PO Token";
        }
        return "Error";
    }

    private static final class DownloadSettings {
        private final String nameTemplate;
        private final String fpsLimit;
        private final int formatIndex;
        private final String resolution;
        private final String saveFolder;
        private final String jsRuntimeArg;
        private final File cookiesFile;

        private DownloadSettings(String nameTemplate, String fpsLimit, int formatIndex, String resolution, String saveFolder, String jsRuntimeArg, File cookiesFile) {
            this.nameTemplate = nameTemplate;
            this.fpsLimit = fpsLimit;
            this.formatIndex = formatIndex;
            this.resolution = resolution;
            this.saveFolder = saveFolder;
            this.jsRuntimeArg = jsRuntimeArg;
            this.cookiesFile = cookiesFile;
        }
    }

    private static final class EnvironmentStatus {
        private final boolean ytDlpAvailable;
        private final String ytDlpVersion;
        private final LocalDate versionDate;
        private final String jsRuntimeArg;
        private final File cookiesFile;

        private EnvironmentStatus(boolean ytDlpAvailable, String ytDlpVersion, LocalDate versionDate, String jsRuntimeArg, File cookiesFile) {
            this.ytDlpAvailable = ytDlpAvailable;
            this.ytDlpVersion = ytDlpVersion;
            this.versionDate = versionDate;
            this.jsRuntimeArg = jsRuntimeArg;
            this.cookiesFile = cookiesFile;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new YoutubeDownloaderV2().setVisible(true));
    }
}
