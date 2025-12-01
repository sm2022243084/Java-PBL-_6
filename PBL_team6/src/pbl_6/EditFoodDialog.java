package pbl_6;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class EditFoodDialog extends JDialog {
    private final FoodDAO foodDAO;
    private final Runnable onSuccess;
    private final int foodId;

    private JTextField nameField, volumeField;
    private JComboBox<String> typeCombo;
    private JSpinner expirySpinner;

    public EditFoodDialog(JFrame parent, FoodDAO foodDAO, int foodId, Runnable onSuccess) {
        super(parent, "식품 수정", true);
        this.foodDAO = foodDAO;
        this.foodId = foodId;
        this.onSuccess = onSuccess;
        initUI();
        load();
    }

    private void initUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(380, 260);
        setLocationRelativeTo(getParent());

        JPanel main = new JPanel(new GridBagLayout());
        main.setBorder(new EmptyBorder(16,16,12,16));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        int y=0;
        c.gridx=0; c.gridy=y; main.add(new JLabel("식품명"), c);
        nameField = new JTextField();
        c.gridx=1; c.weightx=1.0; main.add(nameField, c);

        c.gridx=0; c.gridy=++y; c.weightx=0; main.add(new JLabel("종류(Type)"), c);
        typeCombo = new JComboBox<>(new String[]{"채소","과일","유제품","육류","생선","기타"});
        c.gridx=1; c.weightx=1.0; main.add(typeCombo, c);

        c.gridx=0; c.gridy=++y; c.weightx=0; main.add(new JLabel("수량(volume)"), c);
        volumeField = new JTextField();
        c.gridx=1; c.weightx=1.0; main.add(volumeField, c);

        c.gridx=0; c.gridy=++y; c.weightx=0; main.add(new JLabel("유통기한"), c);
        expirySpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor ed = new JSpinner.DateEditor(expirySpinner, "yyyy-MM-dd");
        expirySpinner.setEditor(ed);
        c.gridx=1; c.weightx=1.0; main.add(expirySpinner, c);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save = new JButton("저장");
        JButton cancel = new JButton("취소");
        btns.add(save); btns.add(cancel);

        save.addActionListener(e -> update());
        cancel.addActionListener(e -> dispose());

        getRootPane().setDefaultButton(save);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        add(main, BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);
    }

    private void load() {
        Food f = foodDAO.getFoodById(foodId);
        if (f == null) {
            JOptionPane.showMessageDialog(this, "데이터가 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            dispose(); return;
        }
        nameField.setText(f.name);
        typeCombo.setSelectedItem(f.type);
        volumeField.setText(String.valueOf(f.volume));
        if (f.expirationDate != null) {
            ((SpinnerDateModel)expirySpinner.getModel()).setValue(java.sql.Date.valueOf(f.expirationDate));
        }
    }

    private void update() {
        try {
            String name = nameField.getText().trim();
            if (name.isEmpty()) { warn("식품명을 입력하세요.", nameField); return; }

            String type = (String) typeCombo.getSelectedItem();
            int volume;
            try { volume = Integer.parseInt(volumeField.getText().trim()); }
            catch (NumberFormatException ex) { warn("수량은 숫자여야 합니다.", volumeField); return; }
            if (volume < 0) { warn("수량은 0 이상이어야 합니다.", volumeField); return; }

            LocalDate exp = toLocalDate((Date) expirySpinner.getValue());

            Food f = new Food();
            f.id = String.valueOf(foodId);
            f.name = name;
            f.type = type;
            f.volume = volume;
            f.expirationDate = exp;

            if (foodDAO.updateFood(f)) {
                JOptionPane.showMessageDialog(this, "수정 완료", "성공", JOptionPane.INFORMATION_MESSAGE);
                if (onSuccess != null) onSuccess.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "수정 실패", "오류", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private LocalDate toLocalDate(Date d){
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
    private void warn(String m, Component f){ JOptionPane.showMessageDialog(this, m, "알림", JOptionPane.WARNING_MESSAGE); if(f!=null) f.requestFocus(); }
}
