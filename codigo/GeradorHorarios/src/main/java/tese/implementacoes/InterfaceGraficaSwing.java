package tese.implementacoes;

import tese.interfaces.GestorInterfaceGrafica;

import javax.swing.JFrame;
import javax.swing.JButton;

public class InterfaceGraficaSwing implements GestorInterfaceGrafica {
    JFrame frameInicial;

    public InterfaceGraficaSwing(String tituloApp){
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
