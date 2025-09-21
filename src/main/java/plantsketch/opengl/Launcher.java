package plantsketch.opengl;

public class Launcher {
    public static void main(String[] args)
    {

        WindowManager window = new WindowManager("The Forest", 1600, 900, false);
        window.init();

        while (!window.windowShouldClose())
        {
            window.update();
        }

        window.cleanup();
    }
}
