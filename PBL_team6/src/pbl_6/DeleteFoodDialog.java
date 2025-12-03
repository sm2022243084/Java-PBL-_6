package pbl_6;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.time.format.DateTimeFormatter;

public class DeleteFoodDialog extends JDialog {
    private final FoodDAO foodDAO;
    private final Runnable onSuccess;
    private final int foodId;

    private JLabel nameL, typeL, expL;
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public DeleteFoodDialog(JFrame parent, FoodDAO foodDAO, int foodId, Runnable onSuccess) {
        super(parent, "식품 삭제", true);
        this.foodDAO = foodDAO;
        this.foodId = foodId;
        this.onSuccess = onSuccess;
        initUI();
        load();
    }

    private void initUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(360, 220);
        setLocationRelativeTo(getParent());

        JPanel root = new JPanel(new BorderLayout(10,10));
        root.setBorder(new EmptyBorder(12,12,12,12));
        setContentPane(root);

        JPanel info = new JPanel(new GridLayout(3,2,6,6));
        info.add(new JLabel("식품명:")); nameL = new JLabel("-"); info.add(nameL);
        info.add(new JLabel("종류:"));   typeL = new JLabel("-");  info.add(typeL);
        info.add(new JLabel("유통기한:")); expL = new JLabel("-"); info.add(expL);

        JLabel warn = new JLabel("정말로 삭제하시겠습니까?");
        warn.setHorizontalAlignment(SwingConstants.CENTER);
        warn.setForeground(new Color(180,30,30));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,8));
        JButton del = new JButton("삭제");
        JButton cancel = new JButton("취소");
        btns.add(del); btns.add(cancel);

        del.addActionListener(e -> doDelete());
        cancel.addActionListener(e -> dispose());

        getRootPane().setDefaultButton(del);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        root.add(info, BorderLayout.NORTH);
        root.add(warn, BorderLayout.CENTER);
        root.add(btns, BorderLayout.SOUTH);
    }

    private void load() {
        Food f = foodDAO.getFoodById(foodId);
        if (f == null) {
            JOptionPane.showMessageDialog(this, "데이터가 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            dispose(); return;
        }
        nameL.setText(nvl(f.name));
        typeL.setText(nvl(f.type));
        expL.setText(f.expirationDate == null ? "-" : DF.format(f.expirationDate));
    }

    private void doDelete() {
        int pick = JOptionPane.showConfirmDialog(this,
                "삭제 후 복구할 수 없습니다. 계속하시겠습니까?",
                "삭제 확인", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (pick != JOptionPane.YES_OPTION) return;

        boolean ok = foodDAO.deleteFood(foodId);
        if (ok) {
            JOptionPane.showMessageDialog(this, "삭제 완료", "성공", JOptionPane.INFORMATION_MESSAGE);
            if (onSuccess != null) onSuccess.run();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "삭제 실패", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String nvl(String s){ return (s==null || s.isBlank()) ? "-" : s; }
}
