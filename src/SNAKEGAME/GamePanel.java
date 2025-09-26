package SNAKEGAME;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {

    static final int SCREEN_WIDTH = 1300;
    static final int SCREEN_HEIGHT = 750;
    static final int UNIT_SIZE = 50;
    static final int GAME_UNITS = (SCREEN_WIDTH*SCREEN_HEIGHT)/(UNIT_SIZE*UNIT_SIZE);
    static final int DELAY = 175;
    final int x[] = new int[GAME_UNITS];
    final int y[] = new int[GAME_UNITS];
    int bodyParts = 6;
    int applesEaten;
    int appleX;
    int appleY;
    char direction = 'R';
    boolean running = false;
    Timer timer;
    Random random;

    // New UI variables
    private GradientPaint backgroundGradient;
    private Color[] snakeColors;
    private boolean appleGlow = true;
    private int glowCounter = 0;

    GamePanel(){
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH,SCREEN_HEIGHT));
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());

        // Initialize enhanced UI elements
        initializeUI();
        startGame();
    }

    private void initializeUI() {
        // Create gradient background colors
        backgroundGradient = new GradientPaint(0, 0, new Color(10, 10, 40), SCREEN_WIDTH, SCREEN_HEIGHT, new Color(30, 10, 60));

        // Create colorful snake gradient
        snakeColors = new Color[] {
                new Color(0, 255, 127),    // Spring Green - Head
                new Color(50, 205, 50),    // Lime Green
                new Color(34, 139, 34),    // Forest Green
                new Color(0, 100, 0)       // Dark Green
        };
    }

    public void startGame() {
        newApple();
        running = true;
        timer = new Timer(DELAY,this);
        timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Enable anti-aliasing for smoother graphics
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        draw(g2d);
    }

    public void draw(Graphics2D g2d) {
        // Draw gradient background
        g2d.setPaint(backgroundGradient);
        g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        if(running) {
            drawGridPattern(g2d);
            drawApple(g2d);
            drawSnake(g2d);
            drawScore(g2d);
            drawGameInfo(g2d);
        } else {
            gameOver(g2d);
        }
    }

    private void drawGridPattern(Graphics2D g2d) {
        // Subtle grid pattern
        g2d.setColor(new Color(255, 255, 255, 20));
        for(int i = 0; i < SCREEN_WIDTH; i += UNIT_SIZE) {
            g2d.drawLine(i, 0, i, SCREEN_HEIGHT);
        }
        for(int i = 0; i < SCREEN_HEIGHT; i += UNIT_SIZE) {
            g2d.drawLine(0, i, SCREEN_WIDTH, i);
        }
    }

    private void drawApple(Graphics2D g2d) {
        // Animated glowing apple
        int glowSize = appleGlow ? 5 : 0;
        Color glowColor = new Color(255, 100, 100, 100);

        // Glow effect
        g2d.setColor(glowColor);
        g2d.fillOval(appleX - glowSize/2, appleY - glowSize/2, UNIT_SIZE + glowSize, UNIT_SIZE + glowSize);

        // Main apple with gradient
        GradientPaint appleGradient = new GradientPaint(
                appleX, appleY, new Color(255, 50, 50),
                appleX + UNIT_SIZE, appleY + UNIT_SIZE, new Color(200, 0, 0)
        );
        g2d.setPaint(appleGradient);
        g2d.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

        // Apple highlight
        g2d.setColor(new Color(255, 200, 200, 150));
        g2d.fillOval(appleX + 10, appleY + 10, 15, 15);

        // Apple stem
        g2d.setColor(new Color(139, 69, 19));
        g2d.fillRect(appleX + UNIT_SIZE/2 - 2, appleY - 8, 4, 10);
    }

    private void drawSnake(Graphics2D g2d) {
        for(int i = 0; i < bodyParts; i++) {
            if(i == 0) {
                // Snake head with special styling
                drawSnakeHead(g2d, x[i], y[i]);
            } else {
                // Snake body with gradient coloring
                drawSnakeBodySegment(g2d, x[i], y[i], i);
            }
        }
    }

    private void drawSnakeHead(Graphics2D g2d, int x, int y) {
        // Head gradient
        GradientPaint headGradient = new GradientPaint(
                x, y, new Color(0, 255, 127),
                x + UNIT_SIZE, y + UNIT_SIZE, new Color(0, 200, 100)
        );
        g2d.setPaint(headGradient);
        g2d.fillRoundRect(x, y, UNIT_SIZE, UNIT_SIZE, 20, 20);

        // Head border
        g2d.setColor(new Color(0, 150, 80));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(x, y, UNIT_SIZE, UNIT_SIZE, 20, 20);

        // Eyes
        g2d.setColor(Color.WHITE);
        g2d.fillOval(x + 10, y + 15, 8, 8);
        g2d.fillOval(x + UNIT_SIZE - 18, y + 15, 8, 8);

        g2d.setColor(Color.BLACK);
        g2d.fillOval(x + 12, y + 17, 4, 4);
        g2d.fillOval(x + UNIT_SIZE - 16, y + 17, 4, 4);
    }

    private void drawSnakeBodySegment(Graphics2D g2d, int x, int y, int segmentIndex) {
        // Color variation based on segment position
        Color segmentColor = snakeColors[segmentIndex % snakeColors.length];
        GradientPaint bodyGradient = new GradientPaint(
                x, y, segmentColor.brighter(),
                x + UNIT_SIZE, y + UNIT_SIZE, segmentColor.darker()
        );

        g2d.setPaint(bodyGradient);
        g2d.fillRoundRect(x, y, UNIT_SIZE, UNIT_SIZE, 15, 15);

        // Segment border
        g2d.setColor(segmentColor.darker());
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x, y, UNIT_SIZE, UNIT_SIZE, 15, 15);
    }

    private void drawScore(Graphics2D g2d) {
        // Modern score display
        g2d.setColor(new Color(255, 215, 0)); // Gold color
        g2d.setFont(new Font("Arial", Font.BOLD, 36));

        String scoreText = "SCORE: " + applesEaten;
        FontMetrics metrics = getFontMetrics(g2d.getFont());

        // Score background
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(20, 20, metrics.stringWidth(scoreText) + 40, 50, 25, 25);

        // Score text
        g2d.setColor(new Color(255, 215, 0));
        g2d.drawString(scoreText, 40, 55);
    }

    private void drawGameInfo(Graphics2D g2d) {
        // Game instructions
        g2d.setColor(new Color(255, 255, 255, 180));
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));

        String instructions = "Use ARROW KEYS to control the snake";
        FontMetrics metrics = getFontMetrics(g2d.getFont());

        g2d.drawString(instructions, (SCREEN_WIDTH - metrics.stringWidth(instructions))/2, SCREEN_HEIGHT - 30);
    }

    public void newApple(){
        appleX = random.nextInt((int)(SCREEN_WIDTH/UNIT_SIZE))*UNIT_SIZE;
        appleY = random.nextInt((int)(SCREEN_HEIGHT/UNIT_SIZE))*UNIT_SIZE;
    }

    public void move(){
        for(int i = bodyParts;i>0;i--) {
            x[i] = x[i-1];
            y[i] = y[i-1];
        }

        switch(direction) {
            case 'U':
                y[0] = y[0] - UNIT_SIZE;
                break;
            case 'D':
                y[0] = y[0] + UNIT_SIZE;
                break;
            case 'L':
                x[0] = x[0] - UNIT_SIZE;
                break;
            case 'R':
                x[0] = x[0] + UNIT_SIZE;
                break;
        }

        // Update apple glow animation
        glowCounter++;
        if(glowCounter >= 10) {
            appleGlow = !appleGlow;
            glowCounter = 0;
        }
    }

    public void checkApple() {
        if((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            newApple();
        }
    }

    public void checkCollisions() {
        //checks if head collides with body
        for(int i = bodyParts;i>0;i--) {
            if((x[0] == x[i])&& (y[0] == y[i])) {
                running = false;
            }
        }
        //check if head touches left border
        if(x[0] < 0) {
            running = false;
        }
        //check if head touches right border
        if(x[0] > SCREEN_WIDTH) {
            running = false;
        }
        //check if head touches top border
        if(y[0] < 0) {
            running = false;
        }
        //check if head touches bottom border
        if(y[0] > SCREEN_HEIGHT) {
            running = false;
        }

        if(!running) {
            timer.stop();
        }
    }

    public void gameOver(Graphics2D g2d) {
        // Dark overlay
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        // Final score display
        g2d.setColor(new Color(255, 215, 0));
        g2d.setFont(new Font("Arial", Font.BOLD, 40));
        FontMetrics metrics1 = getFontMetrics(g2d.getFont());
        String scoreText = "Final Score: " + applesEaten;
        g2d.drawString(scoreText, (SCREEN_WIDTH - metrics1.stringWidth(scoreText))/2, SCREEN_HEIGHT/2 - 60);

        // Game Over text with shadow effect
        g2d.setFont(new Font("Arial", Font.BOLD, 75));
        FontMetrics metrics2 = getFontMetrics(g2d.getFont());

        // Shadow
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.drawString("GAME OVER", (SCREEN_WIDTH - metrics2.stringWidth("GAME OVER"))/2 + 4, SCREEN_HEIGHT/2 + 4);

        // Main text
        g2d.setColor(new Color(255, 50, 50));
        g2d.drawString("GAME OVER", (SCREEN_WIDTH - metrics2.stringWidth("GAME OVER"))/2, SCREEN_HEIGHT/2);

        // Restart instruction
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 24));
        String restartText = "Press SPACE to restart";
        FontMetrics metrics3 = getFontMetrics(g2d.getFont());
        g2d.drawString(restartText, (SCREEN_WIDTH - metrics3.stringWidth(restartText))/2, SCREEN_HEIGHT/2 + 80);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(running) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter{
        @Override
        public void keyPressed(KeyEvent e) {
            switch(e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if(direction != 'R') {
                        direction = 'L';
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if(direction != 'L') {
                        direction = 'R';
                    }
                    break;
                case KeyEvent.VK_UP:
                    if(direction != 'D') {
                        direction = 'U';
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if(direction != 'U') {
                        direction = 'D';
                    }
                    break;
                case KeyEvent.VK_SPACE:
                    if(!running) {
                        // Restart game
                        bodyParts = 6;
                        applesEaten = 0;
                        direction = 'R';
                        running = true;
                        for(int i = 0; i < bodyParts; i++) {
                            x[i] = 0;
                            y[i] = 0;
                        }
                        startGame();
                    }
                    break;
            }
        }
    }
}