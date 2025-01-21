import javax.swing.*;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class RealisticFireworks extends JPanel implements ActionListener {
    private final ArrayList<Firework> fireworks = new ArrayList<>();
    private final Timer timer = new Timer(16, this); // 60 FPS
    private final Random random = new Random();
    private float imageScale = 1.0f; // T? l? ph�ng to/thu nh? c?a ?nh
    private Image image; // H?nh ?nh �? �?p theo nh?c

    public RealisticFireworks() {
        timer.start(); // B?t �?u hi?u ?ng
        // T?i ?nh t? t?p
        image = new ImageIcon("tuan.png").getImage();
        // Ph�t nh?c n?n
        PlayMusic.playMusic("nhac2025.wav", this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        setBackground(Color.BLACK);

        // V? ?nh �?p theo nh?c
        int imageWidth = (int) (image.getWidth(null) * imageScale);
        int imageHeight = (int) (image.getHeight(null) * imageScale);
        g2d.drawImage(image, getWidth() / 2 - imageWidth / 2, getHeight() / 2 - imageHeight / 2, imageWidth, imageHeight, null);

        // V? ch? "Happy New Year 2025!"
        g2d.setColor(Color.RED);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 36));
        String message = "Happy New Year 2025!";
        int messageWidth = g2d.getFontMetrics().stringWidth(message);
        g2d.drawString(message, getWidth() / 2 - messageWidth / 2, 70);

        // V? c�c ph�o hoa
        for (Firework firework : fireworks) {
            firework.draw(g2d);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Th�m ph�o hoa m?i ng?u nhi�n
        if (fireworks.size() < 10 && random.nextInt(100) < 10) {
            int x = random.nextInt(getWidth());
            int y = random.nextInt(getHeight() / 2);
            fireworks.add(new Firework(x, y, random));
        }

        // C?p nh?t v� x�a ph�o hoa �? ho�n th�nh
        fireworks.removeIf(firework -> !firework.update());
        repaint();
    }

    // H�m c?p nh?t t? l? ?nh theo c�?ng �? nh?c
    public void updateImageScale(float scale) {
        this.imageScale = 1.0f + scale * 0.5f; // �i?u ch?nh �? ph�ng to theo c�?ng �?
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Ph�o Hoa 2025");
        RealisticFireworks panel = new RealisticFireworks();

        frame.add(panel);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

// L?p ph�t nh?c v� ph�n t�ch c�?ng �? �m thanh
class PlayMusic {
    public static void playMusic(String filePath, RealisticFireworks panel) {
        new Thread(() -> {
            try {
                File musicFile = new File(filePath);
                if (musicFile.exists()) {
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioStream);

                    // L?y d? li?u �m thanh �? ph�n t�ch c�?ng �?
                    FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    byte[] buffer = new byte[1024];
                    while (clip.isRunning()) {
                        int level = audioStream.read(buffer); // �?c d? li?u �m thanh
                        if (level > 0) {
                            float rms = calculateRMS(buffer); // T�nh RMS �? x�c �?nh c�?ng �?
                            panel.updateImageScale(rms); // C?p nh?t t? l? ?nh
                        }
                        Thread.sleep(16); // Gi?m t?i cho CPU
                    }
                    clip.start();
                    clip.loop(Clip.LOOP_CONTINUOUSLY); // L?p nh?c li�n t?c
                } else {
                    System.out.println("T?p nh?c kh�ng t?n t?i: " + filePath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // H�m t�nh gi� tr? RMS (Root Mean Square) c?a d? li?u �m thanh
    private static float calculateRMS(byte[] audioData) {
        long sum = 0;
        for (int i = 0; i < audioData.length; i++) {
            sum += audioData[i] * audioData[i];
        }
        return (float) Math.sqrt(sum / audioData.length);
    }
}

// L?p ph�o hoa (kh�ng thay �?i)
class Firework {
    private final ArrayList<Particle> particles = new ArrayList<>();
    private final Random random;

    public Firework(int x, int y, Random random) {
        this.random = random;
        for (int i = 0; i < 100; i++) { // T?o 100 h?t ph�o
            particles.add(new Particle(x, y, random));
        }
    }

    public boolean update() {
        boolean isAlive = false;
        for (Particle particle : particles) {
            if (particle.update()) {
                isAlive = true;
            }
        }
        return isAlive;
    }

    public void draw(Graphics2D g2d) {
        for (Particle particle : particles) {
            particle.draw(g2d);
        }
    }
}

// L?p h?t ph�o (kh�ng thay �?i)
class Particle {
    private double x, y;
    private double velocityX, velocityY;
    private int life;
    private final Color color;
    private float alpha; // �? trong su?t

    public Particle(int x, int y, Random random) {
        this.x = x;
        this.y = y;

        // V?n t?c ng?u nhi�n theo h�?ng 360 �?
        double angle = random.nextDouble() * 2 * Math.PI;
        double speed = random.nextDouble() * 3 + 2;
        this.velocityX = Math.cos(angle) * speed;
        this.velocityY = Math.sin(angle) * speed;

        // V?ng �?i v� �? trong su?t
        this.life = random.nextInt(40) + 60;
        this.alpha = 1.0f;

        // M�u s?c ng?u nhi�n
        this.color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    public boolean update() {
        x += velocityX;
        y += velocityY;
        velocityY += 0.05; // Tr?ng l?c nh?

        alpha -= 0.01f; // Gi?m �? s�ng d?n
        life--;

        return life > 0 && alpha > 0;
    }

    public void draw(Graphics2D g2d) {
        if (life > 0 && alpha > 0) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.setColor(color);
            g2d.fillOval((int) x, (int) y, 5, 5);
        }
    }
}
