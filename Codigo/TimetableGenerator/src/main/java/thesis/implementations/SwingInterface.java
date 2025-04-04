package thesis.implementations;

import thesis.interfaces.GraphicalInterface;

import javax.swing.JFrame;

public class SwingInterface implements GraphicalInterface {
    JFrame frameInicial;

    public SwingInterface(String tituloApp){
        // Creating instance of JFrame
        frameInicial = new JFrame(tituloApp);

        frameInicial.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 400 width and 500 height
        frameInicial.setSize(500, 600);

        // using no layout managers
        frameInicial.setLayout(null);

        // making the frame visible
        frameInicial.setVisible(true);
    }
}
