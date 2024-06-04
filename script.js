document.addEventListener('DOMContentLoaded', () => {
    const board = document.getElementById('board');
    const restartButton = document.getElementById('restart');
    const playerTurnDisplay = document.getElementById('player-turn');
    const playerForm = document.getElementById('player-form');
    const startGameButton = document.getElementById('start-game');
    const playerInputDiv = document.getElementById('player-input');
    const playerDisplayDiv = document.getElementById('player-display');
    const menuDiv = document.getElementById('menu');

    let currentPlayer = 'X';
    let gameActive = true;
    let gameState = ['', '', '', '', '', '', '', '', ''];
    let player1Name = '';
    let player2Name = '';

    const winningConditions = [
        [0, 1, 2],
        [3, 4, 5],
        [6, 7, 8],
        [0, 3, 6],
        [1, 4, 7],
        [2, 5, 8],
        [0, 4, 8],
        [2, 4, 6]
    ];

    function handleCellClick(event) {
        const clickedCell = event.target;
        const clickedCellIndex = parseInt(clickedCell.getAttribute('data-cell-index'));

        if (gameState[clickedCellIndex] !== '' || !gameActive) {
            return;
        }

        gameState[clickedCellIndex] = currentPlayer;
        clickedCell.textContent = currentPlayer;
        handleResultValidation();
    }

    function handleResultValidation() {
        let roundWon = false;
        for (let i = 0; i < winningConditions.length; i++) {
            const [a, b, c] = winningConditions[i];
            if (gameState[a] === '' || gameState[b] === '' || gameState[c] === '') {
                continue;
            }
            if (gameState[a] === gameState[b] && gameState[b] === gameState[c]) {
                roundWon = true;
                break;
            }
        }

        if (roundWon) {
            playerTurnDisplay.textContent = `${currentPlayer === 'X' ? player1Name : player2Name} has won!`;
            gameActive = false;
            return;
        }

        const roundDraw = !gameState.includes('');
        if (roundDraw) {
            playerTurnDisplay.textContent = 'Game ended in a draw!';
            gameActive = false;
            return;
        }

        currentPlayer = currentPlayer === 'X' ? 'O' : 'X';
        playerTurnDisplay.textContent = `${currentPlayer === 'X' ? player1Name : player2Name}'s turn`;
    }

    function handleRestartGame() {
        gameActive = true;
        currentPlayer = 'X';
        gameState = ['', '', '', '', '', '', '', '', ''];
        playerTurnDisplay.textContent = `${player1Name}'s turn`;
        document.querySelectorAll('.cell').forEach(cell => cell.textContent = '');
    }

    function createBoard() {
        board.innerHTML = '';
        for (let i = 0; i < 9; i++) {
            const cell = document.createElement('div');
            cell.classList.add('cell');
            cell.setAttribute('data-cell-index', i);
            cell.addEventListener('click', handleCellClick);
            board.appendChild(cell);
        }
    }

    function startGame() {
        player1Name = document.getElementById('player1-name').value || 'Player 1';
        player2Name = document.getElementById('player2-name').value || 'Player 2';

        playerInputDiv.classList.add('hidden');
        playerDisplayDiv.classList.remove('hidden');
        board.classList.remove('hidden');
        menuDiv.classList.remove('hidden');

        playerTurnDisplay.textContent = `${player1Name}'s turn`;

        createBoard();
    }

    startGameButton.addEventListener('click', startGame);
    restartButton.addEventListener('click', handleRestartGame);

});
