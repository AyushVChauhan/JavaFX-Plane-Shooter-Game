
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

@SuppressWarnings("unchecked")
public class App extends Application {
    Random rand = new Random();
    FileInputStream fsi;
    FileOutputStream fso;
    ObjectInputStream osi;
    ObjectOutputStream oso;

    ArrayList<Bullet> bullets = new ArrayList<Bullet>();
    ArrayList<Enemy> enemies = new ArrayList<Enemy>();
    ArrayList<Explosion> explosions = new ArrayList<Explosion>();
    ArrayList<EnemyBullet> enemyBullets = new ArrayList<EnemyBullet>();
    ArrayList<Hitmark> hitmarks = new ArrayList<Hitmark>();
    ArrayList<User> users = new ArrayList<>();
    BossEnemy bossEnemy;

    Image img = new Image("./back1.png", 720, 2160, false, false);
    Timeline tc;

    Boolean createBullet = false;
    static int playerheight = 54, playerwidth = 78;
    Image myPlane = new Image("./myPlane.png", playerwidth, playerheight, true, false);
    int speed = 0;
    int playerx, playery = 660;

    boolean menu = true, fullScreen = true, pause = false, gameover = false, difficulty = true;

    boolean bossIsComing = false, bossHasCome = false;
    Menu menu2;
    GameOver gameOver2;
    Stage myStage;
    Scene myScene;
    Canvas myCanvas;
    AnchorPane myAnchorPane;
    AnimationTimer ac;

    int x = 0, y = -1440, score = 0;

    Media bgsound;
    MediaPlayer bgMediaPlayer;
    AudioClip explosionAudioClip;
    AudioClip hitAudioClip;
    AudioClip shootClip;

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        fsi = new FileInputStream(
                new File("C:/Users/ayush/Downloads/STUDY/JavaPracticals/Graphical/second/scores.txt"));
        try {
            osi = new ObjectInputStream(fsi);
            users = (ArrayList<User>) osi.readObject();
        } catch (Exception e) {
        }

        myStage = primaryStage;
        bgsound = new Media(this.getClass().getResource("jojo.wav").toURI().toString());
        bgMediaPlayer = new MediaPlayer(bgsound);
        bgMediaPlayer.setVolume(0.3);
        explosionAudioClip = new AudioClip(this.getClass().getResource("explosion.wav").toURI().toString());
        hitAudioClip = new AudioClip(this.getClass().getResource("hitsound.wav").toURI().toString());
        shootClip = new AudioClip(this.getClass().getResource("shoot.wav").toURI().toString());

        myCanvas = new Canvas(720, 720);
        GraphicsContext gc = myCanvas.getGraphicsContext2D();

        // tc = new Timeline(new KeyFrame(Duration.millis(15), e -> game(gc)));
        // tc.setCycleCount(Timeline.INDEFINITE);
        myAnchorPane = new AnchorPane(myCanvas);
        myScene = new Scene(myAnchorPane);
        myScene.setFill(Color.BLACK);
        primaryStage.setScene(myScene);
        primaryStage.setTitle("BOOMER PLANE");
        primaryStage.show();
        primaryStage.setResizable(false);
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.valueOf("Ctrl+B"));
        primaryStage.setFullScreenExitHint("SPACE to SHOOT");
        primaryStage.setFullScreen(fullScreen);
        myCanvas.setLayoutX(primaryStage.getWidth() / 2.0 - 360);
        myCanvas.setLayoutY(primaryStage.getHeight() / 2.0 - 360);
        bgMediaPlayer.play();
        bgMediaPlayer.setAutoPlay(true);
        menu2 = new Menu();
        // tc.play();
        // Duration total = bgMediaPlayer.getTotalDuration();
        // bgMediaPlayer.seek(total.subtract(Duration.seconds(5)));
        ac = new AnimationTimer() {
            @Override
            public void handle(long arg0) {
                game(gc);
            }

        };
        ac.start();
        myCanvas.requestFocus();
        myCanvas.setOnKeyPressed(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent e) {
                if (!pause) {
                    if (e.getCode() == KeyCode.LEFT && speed == 0) {
                        speed = -5;
                    }
                    if (e.getCode() == KeyCode.RIGHT && speed == 0) {
                        speed = 5;
                    }
                    if (e.getCode() == KeyCode.SPACE) {
                        createBullet = true;
                    }
                }
                if (e.getCode() == KeyCode.ESCAPE) {
                    if (!menu && !gameover) {
                        pause = !pause;
                        if (pause) {
                            bgMediaPlayer.pause();
                        } else {
                            bgMediaPlayer.play();
                        }
                    }
                }
            }
        });
        myCanvas.setOnKeyReleased(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent e) {
                if (e.getCode() == KeyCode.LEFT) {
                    speed = 0;
                }
                if (e.getCode() == KeyCode.RIGHT) {
                    speed = 0;
                }
                if (e.getCode() == KeyCode.SPACE) {
                    createBullet = false;
                }
            };
        });
    }

    void game(GraphicsContext gc) {
        if (bgMediaPlayer.getCurrentTime().toMillis() >= 222000)
            bgMediaPlayer.setVolume(1.0);
        gc.drawImage(img, x, y);
        if (playerx + speed < 680 && playerx + speed > 0)
            playerx += speed;
        gc.drawImage(myPlane, playerx, playery);

        // Create Bullet
        if (createBullet && !pause) {
            if (bullets.size() == 0) {
                bullets.add(new Bullet(playerx + playerwidth / 2 - Bullet.width, playery - Bullet.height * 2));
            } else {
                long lastBullettime = bullets.get(bullets.size() - 1).time;
                if (System.currentTimeMillis() - lastBullettime > 200) {
                    bullets.add(new Bullet(playerx + playerwidth / 2 - Bullet.width + 3, playery - Bullet.height * 2 + 20));
                }
            }
        }
        // update bullet pos
        for (int i = 0; i < bullets.size(); i++) {
            Bullet bullet = bullets.get(i);
            bullet.posY -= Bullet.speed;
            if (bullet.posY < -10) {
                bullets.remove(i);
            } else {
                gc.setFill(Color.RED);
                gc.fillRect(bullet.posX, bullet.posY, Bullet.width, Bullet.height);
            }
        }
        if (menu) {
            if (menu2 == null) {
                menu2 = new Menu();
            }
            mainMenu(gc);
        } else {
            menu2 = null;
            if (pause) {
                gc.setGlobalAlpha(0.3);
                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, 720, 720);
                gc.setGlobalAlpha(1);
                gc.setFill(Color.WHITE);
                gc.setFont(new Font("Press Start 2P", 24));
                gc.fillText("PAUSED", 289, 370);
            } else if (gameover) {
                gameover(gc);
            } else {
                run(gc);
            }
        }
        if (pause == false && gameover == false || menu == true) {
            y++;
        }
        if (y >= -36) {
            y = -1440;
        }
    }

    void mainMenu(GraphicsContext gc) {
        gc.setFill(Color.RED);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.0);
        gc.setFont(new Font("Press Start 2P", 100));
        gc.fillText("BOOMER", App.Menu.boomx, menu2.boomy);// 589 width
        gc.fillText("PLANE", App.Menu.planx, menu2.plany);// 488 width
        menu2.plany += 4;
        menu2.boomy += 4;

        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Press Start 2P", 22));
        if (!menu2.play) {
            gc.fillText("PLAY", App.Menu.playposx, menu2.playEnemy.posY - 15);
            gc.drawImage(menu2.playEnemy.eImage, menu2.playEnemy.posX, menu2.playEnemy.posY);
            menu2.playEnemy.posY += menu2.playEnemy.eSpeed;
        }
        if (!menu2.scores) {
            gc.fillText("SCORES", App.Menu.scoresposx, menu2.scoreEnemy.posY - 15);
            gc.drawImage(menu2.scoreEnemy.eImage, menu2.scoreEnemy.posX, menu2.scoreEnemy.posY);
            menu2.scoreEnemy.posY += menu2.scoreEnemy.eSpeed;
        }
        if (!menu2.quit) {
            gc.fillText("QUIT", App.Menu.quitposx, menu2.quitEnemy.posY - 15);
            gc.drawImage(menu2.quitEnemy.eImage, menu2.quitEnemy.posX, menu2.quitEnemy.posY);
            menu2.quitEnemy.posY += menu2.quitEnemy.eSpeed;
        }
        if (menu2.p1) {
            if (menu2.plany >= App.Menu.plan1y) {
                menu2.boomy = App.Menu.boom1y;
                menu2.plany = App.Menu.plan1y;
            }
            if (menu2.playEnemy.posY >= Menu.enemyP1Pos) {
                menu2.playEnemy.posY = menu2.scoreEnemy.posY = menu2.quitEnemy.posY = Menu.enemyP1Pos;
            }
            for (Bullet bullet : bullets) {
                if (bullet.intersect(menu2.playEnemy)) {
                    menu2.play = true;
                    menu2.p1 = false;
                    menu2.p2 = true;
                    break;
                }
                if (bullet.intersect(menu2.scoreEnemy)) {
                    menu2.scores = true;
                    menu2.p1 = false;
                    menu2.p2 = true;
                    break;
                }
                if (bullet.intersect(menu2.quitEnemy)) {
                    menu2.quit = true;
                    menu2.p1 = false;
                    menu2.p2 = true;
                }
            }
        } else if (menu2.p2) {
            if (menu2.boomy >= 850) {
                if (menu2.play) {
                    menu2 = null;
                    menu = false;
                } else if (menu2.quit) {
                    System.exit(0);
                } else {
                    gc.drawImage(menu2.backEnemy.eImage, menu2.backEnemy.posX, menu2.backEnemy.posY);
                    if (menu2.backEnemy.posY < 100) {
                        menu2.backEnemy.posY += 5;
                    }
                    gc.setFill(Color.BLACK);
                    gc.setFont(new Font("Press Start 2P", 22));
                    gc.fillText("BACK", 27, menu2.backEnemy.posY - 10);
                    int i = 1;
                    for (int j = 0; j < 7 && j < users.size(); j++) {
                        User user = users.get(j);
                        String toFill = i + "";
                        toFill += "......";
                        toFill += user.name;
                        for (int abc = 0; abc < 12 - user.name.length(); abc++)
                            toFill += ".";
                        toFill += user.score;
                        gc.fillText(toFill, 130, j * 60 + 230);
                        i++;
                    }
                    // show scores
                    for (Bullet bullet : bullets) {
                        if (bullet.intersect(menu2.backEnemy)) {
                            menu2 = null;
                            bullets.remove(bullet);
                            break;
                        }
                    }
                }
            }
        }
    }

    void run(GraphicsContext gc) {
        gc.setFill(Color.BLUE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.0);
        gc.setFont(new Font("Press Start 2P", 25));
        Text theText = new Text("SCORE:" + score);
        theText.setFont(new Font("Press Start 2P", 25));
        double width = theText.getBoundsInLocal().getWidth();
        gc.strokeText("SCORE:" + score, 360 - width / 2.0, 25);
        gc.fillText("SCORE:" + score, 360 - width / 2.0, 25);

        // difficulty increase
        if (!difficulty && score % 30 == 0) {
            Enemy.respawnTime -= 50;
            EnemyBullet.respawnTime -= 50;
            if (Enemy.respawnTime < 500)
                Enemy.respawnTime = 500;
            if (EnemyBullet.respawnTime < 500)
                EnemyBullet.respawnTime = 500;
            difficulty = true;
        }
        
        if (((score + 10) % 100) == 0) {
            bossIsComing = true;
        }

        

        if (bossHasCome) {
            bossIsComing = false;
            if (bossEnemy.posY < 69) {
                bossEnemy.posY += 4;
            } else {
                // bossEnemy.posX += bossEnemy.speed;
                // if(bossEnemy.posX < 10 || bossEnemy.posX > 346)
                // {
                // System.out.println("works");
                // bossEnemy.speed *= -1;
                // }
            }
            bossEnemy.Ea.posX = bossEnemy.posX + 67;
            bossEnemy.Eb.posX = bossEnemy.posX + 165;
            bossEnemy.Ec.posX = bossEnemy.posX + 264;

            bossEnemy.Ea.posY = bossEnemy.Ec.posY = bossEnemy.posY + 142;
            bossEnemy.Eb.posY = bossEnemy.posY + 190;

            bossEnemy.Ea.shot = false;
            bossEnemy.Eb.shot = false;
            bossEnemy.Ec.shot = false;

            if(bossEnemy.Ea.health <= 0)
            {
                if(bossEnemy.Eb.health <= 0)
                {
                    gc.drawImage(BossEnemy.eImage[4], bossEnemy.posX, bossEnemy.posY);
                }
                else if(bossEnemy.Ec.health <= 0)
                {
                    gc.drawImage(BossEnemy.eImage[5], bossEnemy.posX, bossEnemy.posY);
                }
                else{
                    gc.drawImage(BossEnemy.eImage[1], bossEnemy.posX, bossEnemy.posY);
                }
            }
            else if(bossEnemy.Eb.health < 0)
            {
                if(bossEnemy.Ec.health < 0)
                {
                    gc.drawImage(BossEnemy.eImage[6], bossEnemy.posX, bossEnemy.posY);
                }
                else
                {
                    gc.drawImage(BossEnemy.eImage[2], bossEnemy.posX, bossEnemy.posY);
                }
            }
            else if(bossEnemy.Ec.health < 0)
            {
                gc.drawImage(BossEnemy.eImage[3], bossEnemy.posX, bossEnemy.posY);
            }
            else {
                gc.drawImage(BossEnemy.eImage[0], bossEnemy.posX, bossEnemy.posY);
            }
            // gc.setFill(Color.BLUE);
            // gc.fillRect(bossEnemy.Ea.posX, bossEnemy.Ea.posY, bossEnemy.Ea.width,
            // bossEnemy.Ea.height);
            // gc.fillRect(bossEnemy.Eb.posX, bossEnemy.Eb.posY, bossEnemy.Eb.width,
            // bossEnemy.Eb.height);
            // gc.fillRect(bossEnemy.Ec.posX, bossEnemy.Ec.posY, bossEnemy.Ec.width,
            // bossEnemy.Ec.height);

            if (bossEnemy.Ea.health < 0 && bossEnemy.Eb.health < 0 && bossEnemy.Ec.health < 0) {
                for(int i = 0;i<10;i++)
                {
                    Explosion expl = new Explosion(rand.nextInt(310) + 180, rand.nextInt(140) + 110);
                    expl.counter = rand.nextInt(3);
                    explosions.add(expl);
                }
                bossHasCome = false;
                bossIsComing = false;
                enemies.add(new Enemy());
                score+=80;
            }
        }

        if (bossIsComing && enemies.size() == 0) {
            bossIsComing = false;
            bossEnemy = new BossEnemy();
            bossHasCome = true;
        }

        if (bossIsComing == false && bossHasCome == false) {
            // create enemy
            createEnemy();
        }

        if (!bossHasCome) {
            // update enemy pos
            updateEnemy(gc);
        }

        // create Enemy Bullet
        for (Enemy enemy : enemies) {
            createEnemyBullet(enemy);
        }

        // Enemy intersect with player
        for (Enemy enemy : enemies) {
            if (enemy.intersect()) {
                gameover = true;
                explosionAudioClip.play();
                gameOver2 = new GameOver();
                explosions.add(new Explosion(playerx, playery));
                playery = 900;
            }
        }

        // update enemy bullet pos
        updateEnemyBullet(gc);

        // hit bullet and enemy
        for (int i = 0; i < bullets.size(); i++) {
            Bullet bullet = bullets.get(i);
            for (int j = 0; j < enemies.size(); j++) {
                Enemy enemy = enemies.get(j);
                if (bullet.intersect(enemy)) {
                    hitAudioClip.play();
                    bullets.remove(i);
                    i--;
                    break;
                }
            }
        }

        // show explosion
        for (int i = 0; i < explosions.size(); i++) {
            Explosion explosion = explosions.get(i);
            if (System.currentTimeMillis() - explosion.lasttime >= 150) {
                explosion.lasttime = System.currentTimeMillis();
                explosion.counter += 1;
                if (explosion.counter >= 3) {
                    explosions.remove(i);
                    i--;
                    continue;
                }
            }
            gc.drawImage(explosion.images[explosion.counter], explosion.posX, explosion.posY);
        }

        // show hitmark
        for (int i = 0; i < hitmarks.size(); i++) {
            Hitmark hitmark = hitmarks.get(i);
            if (System.currentTimeMillis() - hitmark.sttime > 200) {
                hitmarks.remove(i);
                i--;
                continue;
            }
            gc.drawImage(Hitmark.image, hitmark.posX, hitmark.posY);
            hitmark.posY += hitmark.ySpeed;
        }
    }

    public void gameover(GraphicsContext gc) {
        gc.setFill(Color.BLUE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.0);
        gc.setFont(new Font("Press Start 2P", 25));
        Text theText = new Text("SCORE:" + score);
        theText.setFont(new Font("Press Start 2P", 25));
        double width = theText.getBoundsInLocal().getWidth();
        gc.strokeText("SCORE:" + score, 360 - width / 2.0, 25);
        gc.fillText("SCORE:" + score, 360 - width / 2.0, 25);
        if (gameOver2.gmy < 231)
            gameOver2.gmy += 4;
        else if (!gameOver2.newWindow && explosions.size() == 0) {
            gameOver2.newWindow = true;
            ac.stop();
            TextField txtName = new TextField();
            txtName.setFont(new Font("Press start 2P", 22));
            txtName.setStyle("-fx-background-color: transparent; -fx-text-fill: red");
            Button btnOk = new Button("OK");
            btnOk.setFont(new Font("Press start 2P", 22));
            Label lblName = new Label("ENTER NAME");
            btnOk.setStyle("-fx-background-color: transparent; -fx-text-fill: #6600d4");
            btnOk.setDefaultButton(true);
            lblName.setFont(new Font("Press start 2P", 22));
            myAnchorPane.getChildren().add(lblName);
            myAnchorPane.getChildren().add(txtName);
            myAnchorPane.getChildren().add(btnOk);
            myAnchorPane.setStyle("-fx-background-color:black");
            txtName.textProperty().addListener((obv, oldval, newval) -> {
                String abc = newval.toUpperCase();
                txtName.setText(abc);
                try {
                    if (abc.charAt(abc.length() - 1) == ' ') {
                        txtName.setText(abc.substring(0, abc.length() - 1));
                    }
                } catch (Exception e) {
                }
                if (abc.length() > 10)
                    txtName.setText(abc.substring(0, 10));
            });
            btnOk.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    if (txtName.getText() != "") {
                        try {
                            fso = new FileOutputStream(new File(
                                    "C:/Users/ayush/Downloads/STUDY/JavaPracticals/Graphical/second/scores.txt"),
                                    false);
                            oso = new ObjectOutputStream(fso);
                        } catch (Exception exception) {
                            System.out.println(exception.getMessage());
                        }
                        users.add(new User(score, txtName.getText()));
                        users.sort(new Comparator<User>() {
                            public int compare(User o1, User o2) {
                                return o2.score - o1.score;
                            };
                        });
                        while (users.size() > 7) {
                            users.remove(7);
                        }
                        try {
                            oso.writeObject(users);
                        } catch (Exception exception) {
                            System.out.println(exception.getMessage());
                        }
                        ac.start();
                        myAnchorPane.getChildren().remove(lblName);
                        myAnchorPane.getChildren().remove(txtName);
                        myAnchorPane.getChildren().remove(btnOk);
                        myAnchorPane.setStyle("-fx-background-color:black");
                        myCanvas.requestFocus();
                    }
                }
            });
            txtName.requestFocus();
            txtName.setLayoutY(myStage.getHeight() / 2.0 - 360 + 390);
            txtName.setLayoutX(myStage.getWidth() / 2.0 - 140);
            txtName.setAlignment(Pos.CENTER);
            lblName.setLayoutX(myStage.getWidth() / 2.0 - 360 + 251);
            lblName.setLayoutY(myStage.getHeight() / 2.0 - 360 + 350);
            btnOk.setLayoutX(myStage.getWidth() / 2.0 - 25);
            btnOk.setLayoutY(myStage.getHeight() / 2.0 + 80);
            lblName.setTextFill(Color.web("6600d4"));

        }
        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Press Start 2P", 50));
        gc.fillText("GAME OVER", 138, gameOver2.gmy);
        for (int i = 0; i < explosions.size(); i++) {
            Explosion explosion = explosions.get(i);
            if (System.currentTimeMillis() - explosion.lasttime >= 150) {
                explosion.lasttime = System.currentTimeMillis();
                explosion.counter += 1;
                if (explosion.counter >= 3) {
                    explosions.remove(i);
                    i--;
                    continue;
                }
            }
            gc.drawImage(explosion.images[explosion.counter], explosion.posX, explosion.posY);
        }
        if (playery > 660) {
            playery -= 1;
        } else {
            enemies.removeAll(enemies);
            bullets.removeAll(bullets);
            enemyBullets.removeAll(enemyBullets);
            Enemy.respawnTime = 1500;
            EnemyBullet.respawnTime = 1000;
            score = 0;
            gameover = false;
            menu = true;
            bossIsComing = false;
            bossHasCome = false;
            difficulty = true;
        }
    }

    public class Bullet {
        int posX, posY;
        static int speed = 10, width = 5, height = 10;
        long time;

        Bullet(int posX, int posY) {
            this.posX = posX;
            this.posY = posY;
            time = System.currentTimeMillis();
        }

        boolean intersect(Enemy enemy) {
            boolean b = false;
            if (this.posX - enemy.posX > -Bullet.width && this.posX - enemy.posX < enemy.width) {
                if (this.posY - enemy.posY < enemy.height && this.posY - enemy.posY > Bullet.height) {
                    b = true;
                    enemy.health -= 15;
                    if (enemy.health <= 0) {
                        enemies.remove(enemy);
                        score += 10;
                        difficulty = false;
                        explosions.add(new Explosion(enemy.posX, enemy.posY));
                        explosionAudioClip.play();
                    } else {
                        hitmarks.add(new Hitmark(this.posX, this.posY - 20, enemy.eSpeed));
                    }
                }
            }
            return b;
        }
    }

    public class Enemy {
        int posX, posY, width, height;
        int eSpeed;
        static long respawnTime = 1500;
        int type;
        int health = 50;
        long time;
        long lastBullet;
        Image eImage;
        boolean shot = false;

        Enemy() {
            posX = rand.nextInt(650) + 10;
            posY = -60;
            time = System.currentTimeMillis();
            eSpeed = rand.nextInt(5) + 2;
            int rint = (rand.nextInt(2) + 1);
            height = 60;
            width = rint == 1 ? 24 * 3 : 26 * 3;
            eImage = new Image("./enemy" + rint + ".png", rint == 1 ? 24 * 3 : 26 * 3, 60, true, false);
        }

        boolean intersect() {
            boolean b = false;
            if (this.posX - playerx > -this.width + 5 && this.posX - playerx < playerwidth - 5) {
                if (this.posY - playery > -this.height && this.posY - playery < playerheight / 2) {
                    b = true;
                    hitmarks.add(new Hitmark(playerx + playerwidth / 2 - 10, playery + playerheight / 2 - 10, 0));
                }
            }
            return b;
        }
    }

    public class BossEnemy {
        int width, height, posX, posY;
        int speed = -2;
        static int toPosY = 69;
        static Image eImage[] = new Image[7];
        Enemy Ea, Eb, Ec;

        BossEnemy() {
            eImage[0] = new Image("./bossImage.png", 364, 247, true, false);
            eImage[1] = new Image("./bossImageNoA.png", 364, 247, true, false);
            eImage[2] = new Image("./bossImageNoB.png", 364, 247, true, false);
            eImage[3] = new Image("./bossImageNoC.png", 364, 247, true, false);
            eImage[4] = new Image("./bossImageNoAB.png", 364, 247, true, false);
            eImage[5] = new Image("./bossImageNoAC.png", 364, 247, true, false);
            eImage[6] = new Image("./bossImageNoBC.png", 364, 247, true, false);
            posX = 178;
            posY = -247;
            width = 364;
            height = 247;
            Ea = new Enemy();
            Eb = new Enemy();
            Ec = new Enemy();
            Ea.health = Eb.health = Ec.health = 100;
            Ea.width = Eb.width = Ec.width = 33;
            Ea.height = Eb.height = Ec.height = 51;
            Ea.posX = 245;
            Eb.posX = 343;
            Ec.posX = 442;
            Ea.posY = Ec.posY = -247 + 142;
            Ec.posY = -247 + 190;
            enemies.add(Ea);
            enemies.add(Eb);
            enemies.add(Ec);
            Ea.eSpeed = Eb.eSpeed = Ec.eSpeed = 0;
        }
    }

    public class Explosion {
        int posX, posY, counter;
        long lasttime;
        Image[] images;

        Explosion(int posX, int posY) {
            this.posX = posX;
            this.posY = posY;
            images = new Image[3];
            images[0] = new Image("./g1.png", 50, 50, true, false);
            images[1] = new Image("./g2.png", 50, 50, true, false);
            images[2] = new Image("./g3.png", 50, 50, true, false);
            lasttime = System.currentTimeMillis();
            counter = 0;
        }
    }

    public class EnemyBullet {
        static int respawnTime = 1000;
        int posX, posY;
        long lastBullet;
        int toX, toY;
        static int speed = 7;
        double theta;
        double xSpeed, ySpeed;
        static int width = 10, height = 10;
        static Image enemyBullet = new Image("./enemyBullet.png", 10, 10, false, false);

        EnemyBullet(int posX, int posY) {
            lastBullet = System.currentTimeMillis();
            toX = playerx + playerwidth / 2;
            toY = playery + 10;
            this.posX = posX;
            this.posY = posY;
            theta = Math.atan2(toY - posY, toX - posX);
            theta = 90 - Math.toDegrees(theta);
            xSpeed = speed * Math.sin(Math.toRadians(theta));
            ySpeed = speed * Math.cos(Math.toRadians(theta));
        }

        boolean intersect() {
            boolean b = false;
            if (this.posX - playerx > -EnemyBullet.width + 5 && this.posX - playerx < playerwidth - 5) {
                if (this.posY - playery < playerheight - 5 && this.posY - playery > EnemyBullet.height) {
                    b = true;
                    hitmarks.add(new Hitmark(this.posX, this.posY, 0));
                }
            }
            return b;
        }
    }

    public class Hitmark {
        int posX, posY, ySpeed;
        long sttime;
        static Image image = new Image("./hitmark.png", 20, 20, true, false);

        Hitmark(int posX, int posY, int ySpeed) {
            this.posX = posX;
            this.posY = posY;
            this.ySpeed = ySpeed;
            sttime = System.currentTimeMillis();
        }
    }

    public class Menu {
        int boomy, plany;
        static int boom1y = 170, boomx = 65, plan1y = 290, planx = 112;
        Enemy playEnemy, scoreEnemy, quitEnemy, backEnemy;
        static int enemyP1Pos = 383;
        static int labelP1Pos = 368;
        int playpos, scorespos, quitpos;
        static int playposx = 60, scoresposx = 295, quitposx = 576;
        boolean p1, p2;
        boolean play, quit, scores;

        Menu() {
            plany = 0;
            boomy = -120;
            playpos = scorespos = quitpos = -35;
            p1 = true;
            p2 = false;
            play = quit = scores = false;
            playEnemy = new Enemy();
            scoreEnemy = new Enemy();
            quitEnemy = new Enemy();
            backEnemy = new Enemy();
            backEnemy.posX = 30;
            backEnemy.eSpeed = 0;
            backEnemy.posY = -300;
            backEnemy.health = 100;
            playEnemy.posX = 67;
            scoreEnemy.posX = 324;
            quitEnemy.posX = 581;
            playEnemy.posY = scoreEnemy.posY = quitEnemy.posY = -300;
            playEnemy.eSpeed = scoreEnemy.eSpeed = quitEnemy.eSpeed = 4;
            playEnemy.health = scoreEnemy.health = quitEnemy.health = 100;
        }
    }

    public class GameOver {
        int gmy;
        long sttime;
        boolean newWindow;

        GameOver() {
            sttime = System.currentTimeMillis();
            gmy = -100;
            newWindow = false;
        }
    }

    void createEnemy() {
        if (enemies.size() == 0
                || System.currentTimeMillis() - enemies.get(enemies.size() - 1).time >= Enemy.respawnTime) {
            enemies.add(new Enemy());
        }
    }

    void updateEnemy(GraphicsContext gc) {
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            enemy.posY += enemy.eSpeed;
            // enemy.posX = enemy.posX + rand.nextInt(8) - 4;
            if (enemy.posX < 10)
                enemy.posX = 10;
            if (enemy.posX > 680)
                enemy.posX = 680;
            if (enemy.posY >= 750 || enemy.health <= 0) {
                enemies.remove(i);
            } else {
                gc.drawImage(enemy.eImage, enemy.posX, enemy.posY);
            }
        }
    }

    void createEnemyBullet(Enemy enemy) {
        if (enemy.posY < 400) {
            if (enemyBullets.isEmpty() || System.currentTimeMillis()
                    - enemyBullets.get(enemyBullets.size() - 1).lastBullet >= rand.nextInt(1000)
                            + EnemyBullet.respawnTime
                    && enemy.posY < 400 && enemy.shot == false) {
                enemy.shot = true;
                enemyBullets.add(new EnemyBullet(enemy.posX + enemy.width / 2, enemy.posY + enemy.height));
                if(bossHasCome)
                hitmarks.add(new Hitmark(enemy.posX + enemy.width / 2 - 10, enemy.posY + enemy.height, 0));
            }
        }
    }

    void updateEnemyBullet(GraphicsContext gc) {
        for (int i = 0; i < enemyBullets.size(); i++) {
            EnemyBullet bullet = enemyBullets.get(i);
            bullet.posY += bullet.ySpeed;
            bullet.posX += bullet.xSpeed;
            if (bullet.posY < -10 || bullet.posX > 720 || bullet.posX < 0) {
                enemyBullets.remove(i);
                i--;
            } else {
                gc.drawImage(EnemyBullet.enemyBullet, bullet.posX, bullet.posY);
            }
            if (bullet.intersect()) {
                gameover = true;
                explosionAudioClip.play();
                gameOver2 = new GameOver();
                explosions.add(new Explosion(playerx, playery));
                playery = 900;
            }
        }
    }
}

class User implements Serializable {
    int score;
    String name;

    User(int score, String name) {
        this.score = score;
        this.name = name;
    }
}