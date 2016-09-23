package hackthemarket;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.atan;
import static java.lang.Math.random;
import static java.lang.StrictMath.tan;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;
import static hackthemarket.Allele.Bias;
import static hackthemarket.Allele.Input;
import static hackthemarket.Allele.Output;
import static hackthemarket.Simulator.SCREEN;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.Game;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Point;

public class Simulator extends BasicGame {

    public static final Dimension SCREEN = new Dimension(1600, 800);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Game game;
        game = new Simulator();
        try {
            AppGameContainer appgc = new AppGameContainer(game);
            appgc.setDisplayMode(SCREEN.width, SCREEN.height, false);
            appgc.setTargetFrameRate(1000);
            appgc.setShowFPS(false);
            appgc.start();
        } catch (SlickException ex) {
        }
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static void print(Object s) {
        System.out.println(s);
    }

    private static Point2D randSpawn() {
        double r = random();
        double x;
        double y;
        if (r < 0.50) {
            x = new Bound(50, 1550).rand();
            if (r < 0.25) {
                y = 50;
            } else {
                y = 850;
            }
        } else {
            y = new Bound(50, 850).rand();
            if (r < 0.75) {
                x = 50;
            } else {
                x = 1550;
            }
        }
        return new Point2D.Double(x, y);
    }

    Target target;
    Seeker seeker;

    Population p;
    Genome g;
    Iterator<Genome> genomes;

    private final double centerX, centerY;

    private int t;

    int i = 0;

    public Simulator() {
        super("Simulator");
        centerX = SCREEN.width / 2;
        centerY = SCREEN.height / 2;

        Node in1 = new Node(0, Input);
        Node in2 = new Node(1, Input);
        Node in3 = new Node(2, Input);
        Node in4 = new Node(3, Input);
        Node out1 = new Node(4, Output);
        Node out2 = new Node(5, Output);
        Node out3 = new Node(6, Output);
        Node bias1 = new Node(7, Bias);

        List<Node> nodes = Util.newList();
        nodes.add(in1);
        nodes.add(in2);
        nodes.add(in3);
        nodes.add(in4);
        nodes.add(out1);
        nodes.add(out2);
        nodes.add(out3);
        nodes.add(bias1);

        List<Link> links = Util.newList();
        Bound W = new Bound(-2.0, 2.0);
        links.add(new Link(7, 4, 0.5));
        links.add(new Link(0, 6, W.rand()));
        links.add(new Link(1, 5, W.rand()));
        links.add(new Link(2, 5, W.rand()));
        links.add(new Link(3, 4, W.rand()));

        Genome seed = new Genome(nodes, links);

        GeneticAlgorithm GA = new GeneticAlgorithm(W);
        GA.innovate(links);

        p = new Population(50, seed, GA);
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File("population.pop")));
            p = (Population) ois.readObject();
        }catch(IOException | ClassNotFoundException ex) {
        }
        genomes = p.getGenomes();
    }

    @Override
    public void init(GameContainer gc) throws SlickException {
        target = new Target();
        seeker = null;
    }

    @Override
    public void update(GameContainer gc, int dt) throws SlickException {
        if (dt > 25) {
            dt = 25;
        }
        dt *= i < 20 ? 10 : 3;
        t += dt;

        if (i == 5) {
            File file = new File("population.pop");
            try {
                ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(file));
                ois.writeObject(p);
                ois.close();
            } catch (IOException ex) {
            }
        }
        
        if (seeker == null) {
            t = 0;
            if (genomes.hasNext()) {
                g = genomes.next();
            } else {
                Iterator<Genome> ggg = p.getGenomes();
                Genome gg = ggg.next();
                while (ggg.hasNext()) {
                    Genome next = ggg.next();
                    if (next.getFitness() > gg.getFitness()) {
                        gg = next;
                    }
                }
                print(gg);
                p = p.evolve();
                genomes = p.getGenomes();
                g = genomes.next();
                i++;
            }
            NeuralNetwork nn = new NeuralNetwork(g);
            Point2D spawn = randSpawn();
            seeker = new Seeker(spawn.getX(), spawn.getY(), nn, this);
        } else {
            seeker.update(dt);
        }

        Point2D center = new Point2D.Double(centerX, centerY);
        if (seeker.getHitbox().contains(center)) {
            double T = t / 1000 * 5;
            double fitness = 200 - T;
            g.setFitness(fitness);
            seeker = null;
        } else if (t > 20000) {
            double d = Math.hypot(seeker.x - centerX, seeker.y - centerY);
            double fitness = 100 - d / 9.18;
            g.setFitness(fitness);
            seeker = null;
        } else if (t > 2000 && seeker.x == 100 && seeker.y == 100) {
            g.setFitness(0.01);
            seeker = null;
        }
    }

    @Override
    public void render(GameContainer gc, Graphics g) throws SlickException {
        g.setColor(Color.white);
        g.fillRect(0, 0, SCREEN.width, SCREEN.height);

        target.draw(g);
        if (seeker != null) {
            seeker.draw(g);
        }
    }

}

abstract class Entity {

    double x, y;

    Entity(double x, double y) {
        this.x = x;
        this.y = y;
    }

    abstract void draw(Graphics g);

    abstract void update(int dt);

    abstract Rectangle2D getHitbox();

}

class Seeker extends Entity {

    private static final Dimension size = new Dimension(30, 30);

    private final NeuralNetwork brain;
    private final Simulator sim;

    private double fov;
    private double angle;

    Seeker(double x, double y, NeuralNetwork brain, Simulator sim) {
        super(x, y);
        this.brain = brain;
        this.sim = sim;
        angle = new Bound(0, 2 * PI).rand();
        fov = PI / 8;
    }

    @Override
    void draw(Graphics g) {
        float tx = (float) x - size.width / 2;
        float ty = (float) y - size.height / 2;
        g.setColor(Color.blue);
        g.fillRect(tx, ty, size.width, size.height);

        Ray r1 = new Ray(new Point((float) x, (float) y), angle - fov);
        Ray r2 = new Ray(new Point((float) x, (float) y), angle + fov);
        r1.draw(g);
        r2.draw(g);
    }

    @Override
    Rectangle2D.Double getHitbox() {
        double tx = x - size.width / 2;
        double ty = y - size.height / 2;
        return new Rectangle2D.Double(tx, ty, size.width, size.height);
    }

    @Override
    void update(int dt) {
        angle += 2 * PI;
        angle %= 2 * PI;

        if (x < 0) {
            x = 0;
        } else if (x > SCREEN.width) {
            x = SCREEN.width;
        }
        if (y < 0) {
            y = 0;
        } else if (y > SCREEN.height) {
            y = SCREEN.height;
        }

        Rectangle2D.Double target = sim.target.getHitbox();
        Point2D.Double p = new Point2D.Double(target.x, target.y);
        final boolean detected = inFOV(p);

        double scaleFOV = 16 * fov / PI - 1;
        double scaleCos = Math.cos(angle);
        double scaleSin = Math.sin(angle);
        double scaleDet = detected ? 1 : -1;

        double[] X = {scaleFOV, scaleCos, scaleSin, scaleDet};
        double[] Y = brain.push(X);

        move(dt, Y[0], 1);
        turn(dt, Y[1], 1);
        alter(dt, Y[2], 1);
    }

    private void move(int dt, double o, int D) {
        double v = dt * 0.2 * o * D;
        x += v * Math.cos(angle);
        y += v * -Math.sin(angle);
    }

    private void turn(int dt, double o, int D) {
        angle += dt * 0.001 * o * D;
    }

    private void alter(int dt, double o, int D) {
        fov += dt * 0.001 * o * D;
        if (fov < 0.001) {
            fov = 0.001;
        } else if (fov > PI / 4) {
            fov = PI / 4;
        }
    }

    private boolean inFOV(Point2D.Double p) {
        double tx = p.getX();
        double ty = p.getY();
        double dx = abs(tx - x);
        double dy = abs(ty - y);
        double d0;
        double theta1 = angle - fov;
        double theta2 = angle + fov;
        if (tx < x) {
            if (ty < y) {
                d0 = PI - atan(dy / dx);
            } else if (ty > y) {
                d0 = PI + atan(dy / dx);
            } else {
                d0 = PI;
            }
        } else if (tx > x) {
            if (ty < y) {
                d0 = atan(dy / dx);
            } else if (ty > y) {
                d0 = 2 * PI - atan(dy / dx);
            } else {
                d0 = 0;
            }
        } else {
            if (ty < y) {
                d0 = PI / 2;
            } else if (ty > y) {
                d0 = 3 * PI / 2;
            } else {
                d0 = 0;
            }
        }
        if (theta1 > theta2) {
            theta1 -= 2 * PI;
        }
        return (theta1 <= d0 && d0 <= theta2);
    }

    private static class Ray {

        private final float x;
        private final float y;
        private final float theta;

        private Ray(Point p, double theta) {
            this.theta = (float) ((theta + 2 * PI) % (2 * PI));
            x = p.getX();
            y = p.getY();
        }

        private void draw(Graphics g) {
            float tx;
            float ty;
            float d = (float) (PI / 4);
            if (-d <= theta && theta < d) {
                float m = (float) -tan(theta);
                tx = SCREEN.width;
                ty = m * tx + y - x * m;
            } else if (d <= theta && theta < 3 * d) {
                float m = 1 / (float) -tan(theta);
                ty = 0;
                tx = x - y * m;
            } else if (3 * d <= theta && theta < 5 * d) {
                float m = (float) -tan(theta);
                tx = 0;
                ty = y - x * m;
            } else {
                float m = 1 / (float) -tan(theta);
                ty = SCREEN.height;
                tx = m * ty + x - y * m;
            }
            g.drawLine(x, y, tx, ty);
        }
    }

}

class Target extends Entity {

    private static final Dimension size = new Dimension(30, 30);

    Target() {
        super(SCREEN.width / 2, SCREEN.height / 2);
    }

    @Override
    void draw(Graphics g) {
        float tx = (float) x - size.width / 2;
        float ty = (float) y - size.height / 2;
        g.setColor(Color.red);
        g.fillRect(tx, ty, size.width, size.height);
    }

    @Override
    void update(int dt) {
    }

    @Override
    Rectangle2D.Double getHitbox() {
        // We care only about the center
        return new Rectangle2D.Double(x, y, 0, 0);
    }

}
