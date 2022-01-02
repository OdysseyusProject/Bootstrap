package fr.odysseyus.launcher.bootstrap;
import java.io.File;

import fr.theshark34.openlauncherlib.LaunchException;
import fr.theshark34.openlauncherlib.external.ClasspathConstructor;
import fr.theshark34.openlauncherlib.external.ExternalLaunchProfile;
import fr.theshark34.openlauncherlib.external.ExternalLauncher;
import fr.theshark34.openlauncherlib.minecraft.util.GameDirGenerator;
import fr.theshark34.openlauncherlib.util.CrashReporter;
import fr.theshark34.openlauncherlib.util.SplashScreen;
import fr.theshark34.openlauncherlib.util.explorer.ExploredDirectory;
import fr.theshark34.openlauncherlib.util.explorer.Explorer;
import fr.theshark34.supdate.BarAPI;
import fr.theshark34.supdate.SUpdate;
import fr.theshark34.swinger.Swinger;
import fr.theshark34.swinger.animation.Animator;
import fr.theshark34.swinger.colored.SColoredBar;

public class Main {

    private static SplashScreen splash;
    private static SColoredBar bar;
    private static Thread barThread;

    public static final File OD_B_DIR = new File(GameDirGenerator.createGameDir("OdysseyusV2"), "Launcher");
    private static CrashReporter crashReporter = new CrashReporter("Bootstrap-Crash", new File(OD_B_DIR, "crashes/"));

    public static void main(String[] args) {
        Swinger.setResourcePath("/fr/odysseyus/launcher/bootstrap/ressources/");
        displaySplash();
        try {
            doUpdate();
        } catch (Exception e) {
            crashReporter.catchError(e, "Impossible de mettre à jour le Launcher !");
            barThread.interrupt();
        }

        try {
            launchLauncher();
        } catch (LaunchException e) {
            crashReporter.catchError(e, "Impossible de lancer le Launcher !");
        }

    }

    public static void displaySplash() {
        splash = new SplashScreen("Odysseyus", Swinger.getResource("background.png"));
        splash.setIconImage(Swinger.getResource("icon.png"));
        splash.setTransparent();
        splash.setLayout(null);
        Animator.fadeInFrame(splash, Animator.FAST);

        bar = new SColoredBar(Swinger.getTransparentWhite(120), Swinger.getTransparentWhite(75));
        bar.setBounds(10, 340, 235, 10);
                     //X    Y


        splash.add(bar);

        splash.setVisible(true);

    }

    public static void doUpdate() throws Exception {

        SUpdate su = new SUpdate("http://bootstrap.odysseyus.fr/", OD_B_DIR);
        su.getServerRequester().setRewriteEnabled(true);

        barThread = new Thread() {
            @Override
            public void run() {
                while (!this.isInterrupted()) {
                    bar.setValue((int) (BarAPI.getNumberOfTotalDownloadedBytes() / 1000));
                    bar.setMaximum((int) (BarAPI.getNumberOfTotalBytesToDownload() / 1000));
                }
            }
        };
        barThread.start();

        su.start();
        barThread.interrupt();

    }

    private static void launchLauncher() throws LaunchException {
        ClasspathConstructor constructor = new ClasspathConstructor();
        ExploredDirectory gameDir = Explorer.dir(OD_B_DIR);
        constructor.add(gameDir.get("launcher.jar"));

        ExternalLaunchProfile profile = new ExternalLaunchProfile("com.github.EthanCosta.odysseyus.main", constructor.make());
        ExternalLauncher launcher = new ExternalLauncher(profile);

        Process p = launcher.launch();
        splash.setVisible(false);
        try {
            p.waitFor();
        } catch (InterruptedException localInterruptedException) {
        }
        System.exit(0);
    }

}