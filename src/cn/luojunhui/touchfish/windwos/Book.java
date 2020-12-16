package cn.luojunhui.touchfish.windwos;

import com.intellij.a.f.Log;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindow;
import cn.luojunhui.touchfish.config.Config;
import cn.luojunhui.touchfish.config.ConfigService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Book.class
 *
 * @author junhui
 */
public class Book {
    private final int PREV = 0;
    private final int NEXT = 1;
    private final int CURRENT = 2;

    private int NEXT_KEY_CODE = 0x28;
    private int PREV_KEY_CODE = 0x26;

    private JPanel book;
    private JTextPane text;

    public Book(ToolWindow toolWindow) {
        this.init();
    }


    private void init() {
        Config config = ConfigService.getInstance().getState();
        if (StringUtil.isNotEmpty(config.getBookPath())) {
            this.readText(CURRENT);
        } else {
            this.setText("没有文本文件路径...");
        }

        // 设置键盘监听
        this.text.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                switch (keyEvent.getKeyCode()){
                    case KeyEvent.VK_DOWN:
                        readText(NEXT);
                        break;
                    case KeyEvent.VK_UP:
                        readText(PREV);
                        break;
                    default:break;
                }
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
            }
        });
        this.text.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switch (e.getButton()){
                    case MouseEvent.BUTTON1:
                        readText(NEXT);
                        break;
                    case MouseEvent.BUTTON3:
                        readText(PREV);
                        break;
                    case MouseEvent.BUTTON2:

                        break;
                    default:break;
                }
            }

            @Override
            public void mousePressed(MouseEvent e) { }
            @Override
            public void mouseReleased(MouseEvent e) { }
            @Override
            public void mouseEntered(MouseEvent e) { }
            @Override
            public void mouseExited(MouseEvent e) { }
        });
    }

    /**
     * 按页读取内容
     *
     * @param op
     */
    private void readText(int op) {
        Config config = ConfigService.getInstance().getState();
        int curPage = config.getPage();
        List list = null;
        switch (op) {
            case PREV:
                int prevPage = curPage == 1 ? curPage : curPage - 1;
                list = this.readFromPage(config.getLines(), prevPage, config.getPageSize());
                if (prevPage == curPage) {
                    Notifications.Bus.notify(new Notification("", "tip", "不能再往前翻页了...", NotificationType.INFORMATION));
                    config.setPage(1);
                } else {
                    config.setPage(prevPage);
                }
                break;
            case NEXT:
                int nextPage = curPage == config.getTotalPage() ? curPage : curPage + 1;
                list = this.readFromPage(config.getLines(), nextPage, config.getPageSize());
                if (nextPage == curPage) {
                    Notifications.Bus.notify(new Notification("", "tip", "不能再后前翻页了...", NotificationType.INFORMATION));
                    config.setPage(curPage);
                } else {
                    config.setPage(nextPage);
                }
                break;
            case CURRENT:
                list = this.readFromPage(config.getLines(), curPage, config.getPageSize());
                break;
            default:
                break;
        }
        Font fnt=this.text.getFont();
        this.text.setFont(new Font(fnt.getFamily(),fnt.getStyle(),config.getFontSize()));
//        System.out.printf("字体大小"+config.getFontSize());
        this.setText(list);
        ConfigService.getInstance().setState(config);
    }

    /**
     * 使用java8 stream分页读取
     *
     * @param list
     * @param page     页码
     * @param pageSize 每页行数
     * @return
     */
    private List<String> readFromPage(List<String> list, int page, int pageSize) {
        ArrayList<String> prevPageList = list.stream()
                .skip((page - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toCollection(ArrayList::new));
        return prevPageList;
    }

    private void setText(List list) {
        if (list != null && list.size() > 0) {
            StringBuffer sb = new StringBuffer();
            list.forEach(s -> sb.append(s).append("\n"));
            this.text.setText(sb.toString());
        }
    }

    private void setText(String text) {
        this.text.setText(text);
    }

    public JComponent getContent() {
        return this.book;
    }
}
