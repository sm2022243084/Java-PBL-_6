// AlarmExpirationDate.java
package pbl_6;

import javax.swing.*;
import java.util.ArrayList;
import java.awt.*;
import java.time.format.DateTimeFormatter; // 날짜 포맷팅을 위해 추가

public class AlarmExpirationDate extends JFrame {

    private FoodManage.MainFrame foodManage;
    private ArrayList<Integer> alarmIndex;

    // 날짜 포맷을 깔끔하게 출력하기 위한 포맷터 (yyyy-MM-dd)
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public AlarmExpirationDate(FoodManage.MainFrame _foodManage, ArrayList<Integer> _alarmIndex) {
        this.foodManage = _foodManage;
        this.alarmIndex = _alarmIndex;

        // --- JFrame 기본 설정 ---
        setTitle("⚠️ 유통기한 임박 알림");
        setSize(450, 350); // 창 크기 조금 확대
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); 

        // --- 텍스트 영역 설정 ---
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        
        // 내용 채우기 (오류 지점 확인)
        fillAlarmContent(textArea);
        
        // --- 컴포넌트 추가 ---
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);
        
        JButton closeButton = new JButton("확인했습니다");
        closeButton.addActionListener(e -> dispose());
        add(closeButton, BorderLayout.SOUTH);

        // --- 화면 표시 ---
        setLocationRelativeTo(foodManage);
        setVisible(true);
    }

    private void fillAlarmContent(JTextArea textArea) {
        textArea.append("------------------------------------------------------------------\n");
        textArea.append("⚠️ [중요] 유통기한이 3일 이내로 남은 식품 목록 (" + alarmIndex.size() + "개)\n");
        textArea.append("------------------------------------------------------------------\n\n");
        
        ArrayList<Food> foodList = foodManage.getFoodList();
        
        // 오류가 발생했던 Food 필드 접근 부분을 확인합니다.
        for (int index : alarmIndex) {
            Food food = foodList.get(index);
            
            textArea.append("▶︎ 식품명: " + food.name + "\n");
            textArea.append("  종류: " + food.type + "\n");
            // 날짜를 정의한 포맷으로 출력
            textArea.append("  만료일: " + food.expirationDate.format(DATE_FORMAT) + "\n"); 
            textArea.append("  --------------------------------------\n");
        }
    }
}