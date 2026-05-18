package com.drimsys.modbus.desktop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URI;

@Component
public class DesktopLauncher implements ApplicationListener<WebServerInitializedEvent> {

    private static final Logger log = LoggerFactory.getLogger(DesktopLauncher.class);

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        if (!Boolean.getBoolean("modbus.desktop")) return;

        String url = "http://localhost:" + event.getWebServer().getPort();
        openBrowser(url);
        setupTray(url);
    }

    private void openBrowser(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            log.warn("브라우저 자동 실행 실패: {}", e.getMessage());
        }
    }

    private void setupTray(String url) {
        if (!SystemTray.isSupported()) return;
        try {
            TrayIcon trayIcon = new TrayIcon(createIcon(), "Modbus Simulator");
            trayIcon.setImageAutoSize(true);

            PopupMenu menu = new PopupMenu();

            MenuItem openItem = new MenuItem("브라우저에서 열기");
            openItem.addActionListener(e -> openBrowser(url));
            menu.add(openItem);

            menu.addSeparator();

            MenuItem exitItem = new MenuItem("종료");
            exitItem.addActionListener(e -> {
                SystemTray.getSystemTray().remove(trayIcon);
                System.exit(0);
            });
            menu.add(exitItem);

            trayIcon.setPopupMenu(menu);
            trayIcon.addActionListener(e -> openBrowser(url));

            SystemTray.getSystemTray().add(trayIcon);
            trayIcon.displayMessage("Modbus Simulator", url + " 에서 접속하세요", TrayIcon.MessageType.INFO);

        } catch (Exception e) {
            log.warn("시스템 트레이 설정 실패: {}", e.getMessage());
        }
    }

    private Image createIcon() {
        int size = 32;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(37, 99, 235));
        g.fillRoundRect(0, 0, size, size, 6, 6);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        FontMetrics fm = g.getFontMetrics();
        String text = "M";
        int x = (size - fm.stringWidth(text)) / 2;
        int y = (size - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, x, y);
        g.dispose();
        return img;
    }
}