import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class GomokuGame extends JFrame {
    private JButton[][] boardButtons;
    private int currentPlayer;
    private boolean isGameEnded;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public GomokuGame(boolean isServer) {
        setTitle("GomokuGame");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        setLayout(new GridLayout(9, 9));

        boardButtons = new JButton[9][9];
        currentPlayer = 1;
        isGameEnded = false;

        initializeBoard();

        if (isServer) {
            setupServer();
        } else {
            setupClient();
        }

        setVisible(true);
    }

    private void initializeBoard() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(50, 50));
                button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
                button.addActionListener(new ButtonClickListener(i, j));
                add(button);
                boardButtons[i][j] = button;
            }
        }
    }

    private void setupServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(5000);
            socket = serverSocket.accept();

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            JOptionPane.showMessageDialog(null, "Player 1 is Black, Player 2 is White. Let's begin!");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread receivingThread = new Thread(new ReceivingThread());
        receivingThread.start();
    }

    private void setupClient() {
        try {
            socket = new Socket("127.0.0.1", 5000);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            JOptionPane.showMessageDialog(null, "Player 1 is Black, Player 2 is White. Let's begin!");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread receivingThread = new Thread(new ReceivingThread());
        receivingThread.start();
    }

    private boolean checkWin(int row, int col) {
        char[][] board = new char[9][9];

        
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (!boardButtons[i][j].getText().isEmpty()) {
                    board[i][j] = boardButtons[i][j].getText().charAt(0);
                } else {
                    board[i][j] = ' ';
                }
            }
        }

        char currentPlayerSymbol = board[row][col];

       
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 5; j++) {
                if (board[i][j] == currentPlayerSymbol &&
                        board[i][j + 1] == currentPlayerSymbol &&
                        board[i][j + 2] == currentPlayerSymbol &&
                        board[i][j + 3] == currentPlayerSymbol &&
                        board[i][j + 4] == currentPlayerSymbol) {
                    return true;
                }
            }
        }

        
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 5; j++) {
                if (board[j][i] == currentPlayerSymbol &&
                        board[j + 1][i] == currentPlayerSymbol &&
                        board[j + 2][i] == currentPlayerSymbol &&
                        board[j + 3][i] == currentPlayerSymbol &&
                        board[j + 4][i] == currentPlayerSymbol) {
                    return true;
                }
            }
        }

      
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (board[i][j] == currentPlayerSymbol &&
                        board[i + 1][j + 1] == currentPlayerSymbol &&
                        board[i + 2][j + 2] == currentPlayerSymbol &&
                        board[i + 3][j + 3] == currentPlayerSymbol &&
                        board[i + 4][j + 4] == currentPlayerSymbol) {
                    return true;
                }
            }
        }

       
        for (int i = 0; i < 5; i++) {
            for (int j = 8; j >= 4; j--) {
                if (board[i][j] == currentPlayerSymbol &&
                        board[i + 1][j - 1] == currentPlayerSymbol &&
                        board[i + 2][j - 2] == currentPlayerSymbol &&
                        board[i + 3][j - 3] == currentPlayerSymbol &&
                        board[i + 4][j - 4] == currentPlayerSymbol) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isBoardFull() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (boardButtons[i][j].getText().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void resetGame() {
        
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                boardButtons[i][j].setText("");
                boardButtons[i][j].setForeground(Color.BLACK); 
                boardButtons[i][j].setBackground(Color.WHITE); 
            }
        }
        currentPlayer = 1;
        isGameEnded = false;
    }

    private class ButtonClickListener implements ActionListener {
        private int row;
        private int col;

        public ButtonClickListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isGameEnded && boardButtons[row][col].getText().isEmpty()) {
                if (currentPlayer == 1) {
                    boardButtons[row][col].setText("●");
                    boardButtons[row][col].setForeground(Color.BLACK);
                    boardButtons[row][col].setBackground(Color.BLACK); 
                    out.println(row + "," + col);
                } else {
                    boardButtons[row][col].setText("○");
                    boardButtons[row][col].setForeground(Color.WHITE);
                    boardButtons[row][col].setBackground(Color.WHITE); 
                    out.println(row + "," + col);
                }

                if (checkWin(row, col)) {
                    JOptionPane.showMessageDialog(null, "Player " + currentPlayer + " wins!");
                    isGameEnded = true;
                    resetGame();
                } else if (isBoardFull()) {
                    JOptionPane.showMessageDialog(null, "It's a draw!");
                    isGameEnded = true;
                    resetGame();
                } else {
                    currentPlayer = (currentPlayer == 1) ? 2 : 1;
                }
            }
        }
    }

    private class ReceivingThread implements Runnable {
        @Override
        public void run() {
            try {
                String input;
                while ((input = in.readLine()) != null) {
                    String[] position = input.split(",");
                    int row = Integer.parseInt(position[0]);
                    int col = Integer.parseInt(position[1]);

                    if (!boardButtons[row][col].getText().isEmpty()) {
                        continue;
                    }

                    SwingUtilities.invokeLater(() -> {
                        if (currentPlayer == 1) {
                            boardButtons[row][col].setText("○");
                            boardButtons[row][col].setForeground(Color.WHITE);
                            boardButtons[row][col].setBackground(Color.BLACK);
                        } else {
                            boardButtons[row][col].setText("●");
                            boardButtons[row][col].setForeground(Color.BLACK);
                            boardButtons[row][col].setBackground(Color.WHITE);
                        }

                        if (checkWin(row, col)) {
                            JOptionPane.showMessageDialog(null, "Player " + currentPlayer + " wins!");
                            isGameEnded = true;
                            resetGame();
                        } else if (isBoardFull()) {
                            JOptionPane.showMessageDialog(null, "It's a draw!");
                            isGameEnded = true;
                            resetGame();
                        } else {
                            currentPlayer = (currentPlayer == 1) ? 2 : 1;
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        String[] options = {"Start as Server", "Start as Client"};
        int choice = JOptionPane.showOptionDialog(null, "Please choose an option", "GomokuGame",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            SwingUtilities.invokeLater(() -> {
                new GomokuGame(true);
            });
        } else if (choice == 1) {
            SwingUtilities.invokeLater(() -> {
                new GomokuGame(false);
            });
        }
    }
}

