package guipanels;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;

import modules.Environment;
import edu.memphis.ccrg.lida.framework.ModuleName;
import edu.memphis.ccrg.lida.framework.gui.panels.GuiPanelImpl;

public class EnvironmentPanel extends GuiPanelImpl {

    private static final Logger logger = Logger.getLogger(EnvironmentPanel.class.getCanonicalName());
    private Environment environment;
    private BufferedImage img = new BufferedImage(Environment.ENVIRONMENT_WIDTH,
            Environment.ENVIRONMENT_HEIGHT, BufferedImage.TYPE_INT_RGB);

    private javax.swing.JPanel imgPanel;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JButton refreshButton;

    public EnvironmentPanel() {
        initComponents();
    }

    private void initComponents() {
        jToolBar1 = new javax.swing.JToolBar();
        refreshButton = new javax.swing.JButton();
        imgPanel = new ImagePanel();

        jToolBar1.setRollover(true);

        refreshButton.setText("Refresh");
        refreshButton.setFocusable(false);
        refreshButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refreshButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(refreshButton);

        javax.swing.GroupLayout imgPanelLayout = new javax.swing.GroupLayout(imgPanel);
        imgPanel.setLayout(imgPanelLayout);
        imgPanelLayout.setHorizontalGroup(
            imgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 380, Short.MAX_VALUE)
        );
        imgPanelLayout.setVerticalGroup(
            imgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 236, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(imgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(275, Short.MAX_VALUE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(31, 31, 31)
                    .addComponent(imgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGap(31, 31, 31)))
        );
    }

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {
        refresh();
    }

    @Override
    public void initPanel(String[] param) {
        environment = (Environment) agent.getSubmodule(ModuleName.Environment);
        if (environment != null) {
            refresh();
        } else {
            logger.log(Level.WARNING,
                    "Unable to parse module {1} Panel not initialized.",
                    new Object[]{0L, param[0]});
        }
    }

    @Override
    public void refresh() {
        img = (BufferedImage) environment.getModuleContent();
        this.imgPanel.repaint();
    }

    private class ImagePanel extends JPanel {
        private Dimension dimension = new Dimension();
        double scalingFactor = 1.0;

        public ImagePanel() {
            dimension.setSize(img.getWidth(), img.getHeight());
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.clearRect(0,0, getWidth(), getHeight());
            Image scaledImage = img.getScaledInstance((int)(img.getWidth()*scalingFactor), (int)(img.getHeight()*scalingFactor), Image.SCALE_SMOOTH);
            int xCentered = (getWidth() - scaledImage.getWidth(this)) / 2;
            int yCentered = (getHeight() - scaledImage.getHeight(this)) / 2;
            g.drawImage(scaledImage, xCentered, yCentered, this);
        }
    }
}