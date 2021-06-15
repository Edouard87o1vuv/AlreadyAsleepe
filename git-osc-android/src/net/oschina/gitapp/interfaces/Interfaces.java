package net.oschina.gitapp.interfaces;

public class Interfaces {
    private Interfaces() {
        super();
    }

    public interface SlidingMenuListener {
        void onShowAbove();
    }

    public interface NavigationDrawerListener {
        void onDrawerClosed();

        void onDrawerOpened();
    }
}
