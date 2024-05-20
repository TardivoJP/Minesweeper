import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Random;

public class MinesweeperGUI extends JFrame {
    public static final int size = 16;
    public static final int mines = 40;
    private int minesInfo = mines;
    private int trueMines = mines;
    private boolean gameOver = false;
    private final int[][] board = new int[size][size];
    private final char[][] dangerBoard = new char[size][size];
    private final boolean[][] visible = new boolean[size][size];
    private final boolean[][] flags = new boolean[size][size];
    private final JButton[][] buttons = new JButton[size][size];
    private boolean firstMove = false;

    private JLabel mineCounterLabel;
    private JLabel timerLabel;
    private Timer timer;
    private int elapsedTime = 0;

    public MinesweeperGUI() {
        setTitle("Minesweeper");
        setSize(800, 900);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(1, 2));

        mineCounterLabel = new JLabel("Mines: " + minesInfo);
        timerLabel = new JLabel("Time: 0");

        topPanel.add(mineCounterLabel);
        topPanel.add(timerLabel);

        add(topPanel, BorderLayout.NORTH);

        JPanel boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(size, size));

        initializeButtons(boardPanel);
        add(boardPanel, BorderLayout.CENTER);

        timer = new Timer(1000, e -> updateTimer());
        timer.start();

        setVisible(true);
    }

    private void initializeButtons(JPanel boardPanel) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                buttons[i][j] = new JButton();
                buttons[i][j].setPreferredSize(new Dimension(50, 50));
                buttons[i][j].setBackground(Color.GRAY);
                final int x = i;
                final int y = j;
                buttons[i][j].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (gameOver) return;
                        if (SwingUtilities.isRightMouseButton(e)) {
                            handleRightClick(x, y);
                        } else if (SwingUtilities.isLeftMouseButton(e)) {
                            handleLeftClick(x, y);
                        }
                    }
                });
                boardPanel.add(buttons[i][j]);
            }
        }
    }

    private void handleLeftClick(int x, int y) {
        if (flags[x][y]) return;

        if (!firstMove) {
            generateMines(x, y, mines);
            firstMove = true;
        }

        if (board[x][y] == 1) {
            gameOver = true;
            setAllVisible();
            updateButtons();
            timer.stop();
            JOptionPane.showMessageDialog(this, "BOOOOOOM! Game Over!");
        } else {
            if (dangerBoard[x][y] != '0') {
                visible[x][y] = true;
            } else {
                bfs(x, y);
            }
            updateButtons();
        }
    }

    private void handleRightClick(int x, int y) {
        if (flags[x][y]) {
            flags[x][y] = false;
            minesInfo++;
            if (board[x][y] == 1) trueMines++;
        } else {
            flags[x][y] = true;
            minesInfo--;
            if (board[x][y] == 1) trueMines--;
        }

        if (trueMines == 0 && minesInfo == 0) {
            gameOver = true;
            setAllVisible();
            updateButtons();
            timer.stop();
            JOptionPane.showMessageDialog(this, "Congratulations! You win!");
        } else {
            mineCounterLabel.setText("Mines: " + (minesInfo < 0 ? 0 : minesInfo));
            updateButtons();
        }
    }

    private void generateMines(int firstX, int firstY, int amount) {
        int count = amount;
        HashSet<String> coords = new HashSet<>();
        coords.add(firstX + "," + firstY);

        Random rand = new Random();
        while (count > 0) {
            int row = rand.nextInt(size);
            int col = rand.nextInt(size);
            String coord = row + "," + col;

            if (!coords.contains(coord)) {
                board[row][col] = 1;
                coords.add(coord);
                count--;
            }
        }

        initializeDangerBoard();
    }

    private void initializeDangerBoard() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                dangerBoard[i][j] = '0';
            }
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == 1) {
                    dangerBoard[i][j] = '*';

                    for (int[] adj : adjacencies) {
                        int x = i + adj[0];
                        int y = j + adj[1];

                        if (isValid(x, y) && dangerBoard[x][y] != '*') {
                            dangerBoard[x][y]++;
                        }
                    }
                }
            }
        }
    }

    private void bfs(int x, int y) {
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        queue.offer(new int[]{x, y});

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            visible[cur[0]][cur[1]] = true;

            if (dangerBoard[cur[0]][cur[1]] != '0') continue;

            for (int[] adj : adjacencies) {
                int nextX = cur[0] + adj[0];
                int nextY = cur[1] + adj[1];

                if (isValid(nextX, nextY) && !visible[nextX][nextY]) {
                    queue.offer(new int[]{nextX, nextY});
                }
            }
        }
    }

    private void updateButtons() {
        Font font = new Font("Arial", Font.BOLD, 16);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                buttons[i][j].setFont(font);

                if (flags[i][j]) {
                    buttons[i][j].setText("F");
                    if(gameOver) buttons[i][j].setText(dangerBoard[i][j] == '*' ? "F" : ""+dangerBoard[i][j]);
                    buttons[i][j].setBackground(Color.RED);
                    if(gameOver) buttons[i][j].setBackground(dangerBoard[i][j] == '*' ? Color.RED : Color.ORANGE);
                    buttons[i][j].setForeground(Color.BLACK);
                } else if (visible[i][j]) {
                    char value = dangerBoard[i][j];
                    buttons[i][j].setText(dangerBoard[i][j] == '0' ? "" : String.valueOf(value));
                    buttons[i][j].setBackground(dangerBoard[i][j] == '*' ? Color.RED : Color.LIGHT_GRAY);
                    buttons[i][j].setForeground(getColorForNumber(value));
                } else {
                    buttons[i][j].setText("");
                    buttons[i][j].setBackground(Color.GRAY);
                }
            }
        }
    }

    
    private Color getColorForNumber(char value) {
        switch (value) {
            case '1':
                return Color.BLUE;
            case '2':
                return new Color(0, 128, 0); // Green
            case '3':
                return Color.RED;
            case '4':
                return new Color(0, 0, 128); // Dark Blue
            case '5':
                return new Color(139, 69, 19); // Brown
            case '6':
                return Color.CYAN;
            case '7':
                return Color.BLACK;
            case '8':
                return Color.DARK_GRAY;
            default:
                return Color.BLACK;
        }
    }

    private void setAllVisible() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                visible[i][j] = true;
            }
        }
    }

    private void updateTimer() {
        elapsedTime++;
        timerLabel.setText("Time: " + elapsedTime);
    }

    private boolean isValid(int x, int y) {
        return x >= 0 && y >= 0 && x < size && y < size;
    }

    private static final int[][] adjacencies = {{0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}};

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MinesweeperGUI::new);
    }
}
