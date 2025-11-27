package edu.univ.erp.ui;

import edu.univ.erp.domain.User;
import edu.univ.erp.service.NotificationService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.List;


public class NotificationPanel extends JPanel {

    private final User currentUser;
    private final NotificationService notificationService = new NotificationService();

    private final DefaultListModel<NotificationService.NotificationRow> model = new DefaultListModel<>();
    private final JList<NotificationService.NotificationRow> list = new JList<>(model);
    private final JLabel lblHeader = new JLabel();

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM HH:mm");

    public NotificationPanel(User user) {
        this.currentUser = user;
        initUI();
        reload();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, 0));
        setBorder(BorderFactory.createTitledBorder("Notifications"));

        lblHeader.setHorizontalAlignment(SwingConstants.CENTER);
        lblHeader.setFont(lblHeader.getFont().deriveFont(Font.PLAIN, 12f));
        add(lblHeader, BorderLayout.NORTH);

        
        list.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JPanel p = new JPanel(new BorderLayout());
            p.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

            String msg = value.message;
            String time = value.createdAt == null ? "" : sdf.format(value.createdAt);

            JLabel lblMsg = new JLabel("<html>" + escapeHtml(msg) + "</html>");
            lblMsg.setFont(lblMsg.getFont().deriveFont(value.isRead ? Font.PLAIN : Font.BOLD));
            JLabel lblTime = new JLabel(time);
            lblTime.setFont(lblTime.getFont().deriveFont(10f));
            lblTime.setForeground(Color.GRAY);

            p.add(lblMsg, BorderLayout.CENTER);
            p.add(lblTime, BorderLayout.EAST);

            if (isSelected) p.setBackground(new Color(220, 235, 255));
            return p;
        });

        JScrollPane sp = new JScrollPane(list);
        add(sp, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnMarkAll = new JButton("Mark all read");

        bottom.add(btnRefresh);
        bottom.add(btnMarkAll);
        add(bottom, BorderLayout.SOUTH);

        
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int idx = list.locationToIndex(e.getPoint());
                    if (idx >= 0) {
                        NotificationService.NotificationRow row = model.get(idx);
                        showNotification(row);
                    }
                }
            }
        });

        btnRefresh.addActionListener(a -> reload());
        btnMarkAll.addActionListener(a -> {
            notificationService.markAllAsRead(currentUser.getId(), currentUser.getRole());
            reload();
        });
    }

    private void showNotification(NotificationService.NotificationRow row) {
        
        if (!row.isRead) {
            notificationService.markAsRead(row.id);
        }
        
        String info = row.message + (row.link != null ? ("\n\nLink: " + row.link) : "");
        JOptionPane.showMessageDialog(this, info, "Notification", JOptionPane.INFORMATION_MESSAGE);
        reload();
    }

    private void reload() {
        model.clear();
        List<NotificationService.NotificationRow> rows =
                notificationService.getNotificationsForUser(currentUser.getId(), currentUser.getRole());

        int unread = 0;
        for (var r : rows) {
            model.addElement(r);
            if (!r.isRead) unread++;
        }

        lblHeader.setText("Unread: " + unread + " — Total: " + rows.size());
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br/>");
    }
}
