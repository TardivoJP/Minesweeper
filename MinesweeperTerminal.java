import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Scanner;

class Minesweeper {
    public static final int size = 16;
    public static int mines = 40;
    public static int minesInfo = 40;
    public static int totalFlags = 0;
    public static int trueMines = 40;
    public static boolean gameOver = false;

    public static void main(String args[]){
        int[][] board = new int[size][size];
        char[][] dangerBoard = new char[size][size];
        boolean[][] visible = new boolean[size][size];
        boolean[][] flags = new boolean[size][size];
        
        boolean firstMove = false;
        Scanner s = new Scanner(System.in);

        while(!gameOver){
            if(minesInfo <= 0){
                System.out.println("Mines: 0");
            }else{
                System.out.println("Mines: "+minesInfo);
            }
            
            System.out.println("Row: ");
            int row = s.nextInt();
            System.out.println("Column: ");
            int column = s.nextInt();
            System.out.println("1 = click | 2 = flag");
            int move = s.nextInt();

            if(!firstMove){
                generateMines(board, dangerBoard, row, column, mines);
                firstMove = true;
            }

            displayBoard(board, dangerBoard, visible, flags, row, column, move);
        }

        s.close();
    }

    public static void displayBoard(int[][] board, char[][] dangerBoard, boolean[][] visible, boolean[][] flags, int x, int y, int move){
        if(move == 2){
            if(flags[x][y]){
                if(board[x][y] == 1){
                    trueMines++;
                }
                totalFlags--;

                minesInfo++;
            }else{
                if(board[x][y] == 1){
                    trueMines--;
                }
                totalFlags++;

                minesInfo--;
            }
            flags[x][y] = !flags[x][y];

            if(trueMines == 0 && minesInfo == 0){
                setAllVisible(visible);
                paintBoard(dangerBoard, visible, flags);
                System.out.println("YOU WIN!");
                gameOver = true;
                return;
            }

            paintBoard(dangerBoard, visible, flags);
            return;
        }

        if(visible[x][y] || flags[x][y]){
            paintBoard(dangerBoard, visible, flags);
            return;
        }

        if(board[x][y] == 1){
            setAllVisible(visible);
            paintBoard(dangerBoard, visible, flags);
            System.out.println("BOOOOOOM!");
            gameOver = true;
            return;
        }

        if(dangerBoard[x][y] != '0'){
            visible[x][y] = true;
            paintBoard(dangerBoard, visible, flags);
            return;
        }

        if(dangerBoard[x][y] == '0'){
            bfs(board, dangerBoard, visible, x, y);
            paintBoard(dangerBoard, visible, flags);
            return;
        }
    }

    public static void bfs(int[][] board, char[][] dangerBoard, boolean[][] visible, int x, int y){
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        queue.offer(new int[]{x, y});

        while(!queue.isEmpty()){
            int[] cur = queue.poll();
            visible[cur[0]][cur[1]] = true;

            if(dangerBoard[cur[0]][cur[1]] != '0'){
                continue;
            }

            for(int[] adjecency : adjacencies){
                int nextX = cur[0] + adjecency[0];
                int nextY = cur[1] + adjecency[1];
    
                if(isValid(board, nextX, nextY)){
                    if(!visible[nextX][nextY]){
                        queue.offer(new int[]{nextX, nextY});
                    }
                }
            }
        }
    }

    public static void generateMines(int[][] board, char[][] dangerBoard, int firstX, int firstY, int amount){
        int count = amount;
        HashSet<String> coords = new HashSet<>();
        coords.add((firstX+","+firstY));

        while(count > 0){
            int row = randomNumber(0, size-1);
            int col = randomNumber(0, size-1);
            String coord = row+","+col;

            if(!coords.contains(coord)){
                board[row][col] = 1;
                coords.add(coord);
                count--;
            }
        }

        initializeDangerBoard(board, dangerBoard);
    }

    public static int[][] adjacencies = {{0,-1},{-1,-1},{-1,0},{-1,1},{0,1},{1,1},{1,0},{1,-1}};

    public static boolean isValid(int[][] board, int x, int y){
        if(x < 0 || y < 0 || x >= board.length || y >= board[0].length){
            return false;
        }

        return true;
    }

    public static void initializeDangerBoard(int[][] board, char[][] dangerBoard){
        for(int i=0; i<dangerBoard.length; i++){
            for(int j=0; j<dangerBoard.length; j++){
                dangerBoard[i][j] = '0';
            }
        }

        for(int i=0; i<board.length; i++){
            for(int j=0; j<board.length; j++){
                if(board[i][j] == 1){
                    dangerBoard[i][j] = '*';

                    for(int[] adjecency : adjacencies){
                        int x = i + adjecency[0];
                        int y = j + adjecency[1];
            
                        if(isValid(board, x, y)){
                            dangerBoard[x][y]++;
                        }
                    }
                }
            }
        }
    }

    public static void paintBoard(char[][] dangerBoard, boolean[][] visibleBoard, boolean[][] flags){
        for(int i=0; i<dangerBoard.length; i++){
            for(int j=0; j<dangerBoard.length; j++){
                if(flags[i][j]){
                    System.out.print("F" + "  ");
                    continue;
                }

                if(visibleBoard[i][j]){
                    System.out.print(dangerBoard[i][j] + "  ");
                }else{
                    System.out.print("x" + "  ");
                }
            }
            System.out.println();
        }
    }

    public static void setAllVisible(boolean[][] visible){
        for(int i=0; i<visible.length; i++){
            for(int j=0; j<visible.length; j++){
                visible[i][j] = true;
            }
        }
    }

    public static int randomNumber(int min, int max){
        return (int)(Math.random() * (max - min + 1)) + min;
    }
}
